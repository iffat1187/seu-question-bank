package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.enums.ExamType;
import com.seu.seuquestionbank.enums.PaperStatus;
import com.seu.seuquestionbank.model.Course;
import com.seu.seuquestionbank.model.QuestionPaper;
import com.seu.seuquestionbank.service.CloudinaryService;
import com.seu.seuquestionbank.service.CourseService;
import com.seu.seuquestionbank.service.QuestionPaperService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/question-papers")
public class QuestionPaperController {

    private static final Logger log = LoggerFactory.getLogger(QuestionPaperController.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_IMAGES = 5;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final QuestionPaperService questionPaperService;
    private final CourseService courseService;
    private final CloudinaryService cloudinaryService;

    public QuestionPaperController(QuestionPaperService questionPaperService, CourseService courseService, CloudinaryService cloudinaryService) {
        this.questionPaperService = questionPaperService;
        this.courseService = courseService;
        this.cloudinaryService = cloudinaryService;
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isOwner(QuestionPaper paper, Authentication auth) {
        if (auth == null || paper.getUploadedBy() == null) return false;
        return paper.getUploadedBy().equalsIgnoreCase(auth.getName());
    }

    private boolean canView(QuestionPaper paper, Authentication auth) {
        if (paper.getStatus() == PaperStatus.APPROVED) return true;
        if (isAdmin(auth)) return true;
        return isOwner(paper, auth);
    }

    private boolean canEditOrDelete(QuestionPaper paper, Authentication auth) {
        if (isAdmin(auth)) return true;
        return isOwner(paper, auth) && paper.getStatus() == PaperStatus.PENDING;
    }

    private void validateFiles(MultipartFile[] files, BindingResult bindingResult, String field) {
        if (files == null) return;
        List<MultipartFile> nonEmpty = Arrays.stream(files).filter(f -> f != null && !f.isEmpty()).toList();
        if (nonEmpty.size() > MAX_IMAGES) {
            bindingResult.rejectValue(field, "error." + field, "Maximum " + MAX_IMAGES + " images allowed");
            return;
        }
        for (MultipartFile file : nonEmpty) {
            if (file.getSize() > MAX_FILE_SIZE) {
                bindingResult.rejectValue(field, "error." + field, "File " + file.getOriginalFilename() + " exceeds 5MB limit");
            }
            String contentType = file.getContentType();
            String filename = file.getOriginalFilename();
            String ext = "";
            if (filename != null && filename.contains(".")) {
                ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            }
            boolean typeOk = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
            boolean extOk = ALLOWED_EXTENSIONS.contains(ext);
            if (!typeOk && !extOk) {
                bindingResult.rejectValue(field, "error." + field, "File " + file.getOriginalFilename() + " has unsupported type. Allowed: JPG, JPEG, PNG, WEBP");
            }
        }
    }

    @GetMapping
    public String list(@RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "course", required = false) String course,
                       @RequestParam(value = "semester", required = false) String semester,
                       @RequestParam(value = "year", required = false) String year,
                       @RequestParam(value = "examType", required = false) String examType,
                       Model model, HttpServletRequest request, Authentication auth) {
        Integer academicYear = parseYear(year);

        List<QuestionPaper> base;
        boolean admin = isAdmin(auth);
        if (admin) {
            base = questionPaperService.findAll();
        } else if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            List<QuestionPaper> approved = questionPaperService.findApproved();
            List<QuestionPaper> own = questionPaperService.findByUploadedBy(auth.getName());
            Map<String, QuestionPaper> map = new LinkedHashMap<>();
            for (QuestionPaper p : approved) map.put(p.getId(), p);
            for (QuestionPaper p : own) map.put(p.getId(), p);
            base = new ArrayList<>(map.values());
        } else {
            base = questionPaperService.findApproved();
        }

        // Distinct filter options derived from the papers this user may see
        Map<String, String> courseOptions = new TreeMap<>();
        TreeSet<Integer> yearOptions = new TreeSet<>(Comparator.reverseOrder());
        for (QuestionPaper p : base) {
            if (p.getCourseCode() != null && !p.getCourseCode().isBlank()) {
                courseOptions.putIfAbsent(p.getCourseCode().trim(),
                        p.getCourseTitle() != null ? p.getCourseTitle() : p.getCourseCode().trim());
            }
            if (p.getAcademicYear() != null) {
                yearOptions.add(p.getAcademicYear());
            }
        }

        List<QuestionPaper> papers =
                questionPaperService.filter(base, search, course, semester, academicYear, examType);
        papers = new ArrayList<>(papers);
        papers.sort(Comparator.comparing(QuestionPaper::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        boolean hasFilters = notBlank(search) || notBlank(course) || notBlank(semester)
                || notBlank(examType) || academicYear != null;

        model.addAttribute("papers", papers);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("currentUser", auth != null ? auth.getName() : null);

        model.addAttribute("search", trimToEmpty(search));
        model.addAttribute("courseCode", trimToEmpty(course));
        model.addAttribute("semester", trimToEmpty(semester));
        model.addAttribute("examType", trimToEmpty(examType));
        model.addAttribute("year", academicYear);
        model.addAttribute("hasFilters", hasFilters);
        model.addAttribute("courseOptions", courseOptions);
        model.addAttribute("yearOptions", new ArrayList<>(yearOptions));
        model.addAttribute("semesters", List.of("Spring", "Fall", "Summer"));
        model.addAttribute("examTypes", ExamType.values());
        return "question-papers";
    }

    private Integer parseYear(String year) {
        if (year == null || year.isBlank()) return null;
        try {
            return Integer.parseInt(year.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model model, HttpServletRequest request, Authentication auth) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        QuestionPaper paper = opt.get();
        if (!canView(paper, auth)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("paper", paper);
        model.addAttribute("isAdmin", isAdmin(auth));
        model.addAttribute("isOwner", isOwner(paper, auth));
        model.addAttribute("canEdit", canEditOrDelete(paper, auth));
        model.addAttribute("canDelete", canEditOrDelete(paper, auth));
        model.addAttribute("currentPath", request.getRequestURI());
        return "question-paper-details";
    }

    @GetMapping("/create")
    public String createForm(Model model, HttpServletRequest request) {
        model.addAttribute("questionPaper", new QuestionPaper());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("examTypes", ExamType.values());
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("imageUrlsText", "");
        return "question-paper-create";
    }

    @PostMapping("/create")
    public String createSubmit(@Valid @ModelAttribute("questionPaper") QuestionPaper questionPaper,
                               BindingResult bindingResult,
                               @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                               @RequestParam(value = "imageUrlsText", required = false) String imageUrlsText,
                               Model model,
                               HttpServletRequest request,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("examTypes", ExamType.values());

        if (bindingResult.hasErrors()) {
            model.addAttribute("imageUrlsText", imageUrlsText);
            return "question-paper-create";
        }
        if (questionPaper.getCourseCode() == null || questionPaper.getCourseCode().isBlank()) {
            bindingResult.rejectValue("courseCode", "error.courseCode", "Course code is required");
            model.addAttribute("imageUrlsText", imageUrlsText);
            return "question-paper-create";
        }

        // Validate files
        validateFiles(imageFiles, bindingResult, "imageUrls");
        if (bindingResult.hasErrors()) {
            model.addAttribute("imageUrlsText", imageUrlsText);
            return "question-paper-create";
        }

        List<String> urls = new ArrayList<>();
        // Prefer uploaded files; fallback to text URLs for backward compat
        boolean hasFiles = imageFiles != null && Arrays.stream(imageFiles).anyMatch(f -> f != null && !f.isEmpty());
        if (hasFiles) {
            try {
                for (MultipartFile file : imageFiles) {
                    if (file == null || file.isEmpty()) continue;
                    String url = cloudinaryService.uploadFile(file);
                    if (url != null) urls.add(url);
                }
            } catch (Exception e) {
                log.error("Cloudinary upload failed", e);
                bindingResult.rejectValue("imageUrls", "error.imageUrls", "Failed to upload images: " + e.getMessage());
                model.addAttribute("imageUrlsText", imageUrlsText);
                return "question-paper-create";
            }
        } else {
            urls = parseImageUrls(imageUrlsText);
        }

        if (urls.isEmpty()) {
            bindingResult.rejectValue("imageUrls", "error.imageUrls", "At least one image is required");
            model.addAttribute("imageUrlsText", imageUrlsText);
            return "question-paper-create";
        }
        if (urls.size() > MAX_IMAGES) {
            bindingResult.rejectValue("imageUrls", "error.imageUrls", "Maximum " + MAX_IMAGES + " images allowed");
            model.addAttribute("imageUrlsText", imageUrlsText);
            return "question-paper-create";
        }

        questionPaper.setImageUrls(urls);
        Optional<Course> courseOpt = courseService.findByCourseCode(questionPaper.getCourseCode().trim());
        if (courseOpt.isPresent()) {
            questionPaper.setCourseTitle(courseOpt.get().getTitle());
        } else if (questionPaper.getCourseTitle() == null || questionPaper.getCourseTitle().isBlank()) {
            questionPaper.setCourseTitle(questionPaper.getCourseCode());
        }
        questionPaper.setUploadedBy(auth.getName());
        questionPaper.setStatus(PaperStatus.PENDING);
        QuestionPaper saved = questionPaperService.create(questionPaper);
        redirectAttributes.addFlashAttribute("success", "Question paper created (PENDING approval). ID: " + saved.getId());
        return "redirect:/question-papers/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model, HttpServletRequest request, Authentication auth) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        QuestionPaper paper = opt.get();
        if (!canEditOrDelete(paper, auth)) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/403";
        }
        model.addAttribute("questionPaper", paper);
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("examTypes", ExamType.values());
        model.addAttribute("paperStatuses", PaperStatus.values());
        model.addAttribute("isAdmin", isAdmin(auth));
        model.addAttribute("currentPath", request.getRequestURI());
        String urlsText = paper.getImageUrls() != null ? String.join("\n", paper.getImageUrls()) : "";
        model.addAttribute("imageUrlsText", urlsText);
        // for edit preview, pass existing urls
        model.addAttribute("existingImageUrls", paper.getImageUrls() != null ? paper.getImageUrls() : List.of());
        return "question-paper-edit";
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable String id,
                             @Valid @ModelAttribute("questionPaper") QuestionPaper formPaper,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                             @RequestParam(value = "existingImageUrls", required = false) List<String> existingImageUrls,
                             @RequestParam(value = "imageUrlsText", required = false) String imageUrlsText,
                             @RequestParam(value = "status", required = false) String statusParam,
                             Model model,
                             HttpServletRequest request,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        QuestionPaper existing = opt.get();
        if (!canEditOrDelete(existing, auth)) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/403";
        }
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("examTypes", ExamType.values());
        model.addAttribute("paperStatuses", PaperStatus.values());
        model.addAttribute("isAdmin", isAdmin(auth));
        model.addAttribute("currentPath", request.getRequestURI());

        if (bindingResult.hasErrors()) {
            model.addAttribute("imageUrlsText", imageUrlsText);
            model.addAttribute("existingImageUrls", existing.getImageUrls());
            formPaper.setId(id);
            return "question-paper-edit";
        }

        // Validate new files
        validateFiles(imageFiles, bindingResult, "imageUrls");
        if (bindingResult.hasErrors()) {
            model.addAttribute("imageUrlsText", imageUrlsText);
            model.addAttribute("existingImageUrls", existing.getImageUrls());
            formPaper.setId(id);
            return "question-paper-edit";
        }

        List<String> finalUrls = new ArrayList<>();
        // Keep existing URLs that are checked to keep (if provided)
        if (existingImageUrls != null && !existingImageUrls.isEmpty()) {
            finalUrls.addAll(existingImageUrls.stream().map(String::trim).filter(s -> !s.isBlank()).toList());
        } else {
            // if no explicit existing list, keep all existing (for backward compat when no checkbox)
            // but if new files are provided, we will merge
            if (existing.getImageUrls() != null) {
                // keep existing unless new upload replaces? For simplicity keep existing
                finalUrls.addAll(existing.getImageUrls());
            }
        }
        // Also handle fallback text URLs if no files and no existing
        boolean hasNewFiles = imageFiles != null && Arrays.stream(imageFiles).anyMatch(f -> f != null && !f.isEmpty());
        if (hasNewFiles) {
            try {
                for (MultipartFile file : imageFiles) {
                    if (file == null || file.isEmpty()) continue;
                    String url = cloudinaryService.uploadFile(file);
                    if (url != null) finalUrls.add(url);
                }
            } catch (Exception e) {
                log.error("Cloudinary upload failed on edit", e);
                bindingResult.rejectValue("imageUrls", "error.imageUrls", "Failed to upload images: " + e.getMessage());
                model.addAttribute("imageUrlsText", imageUrlsText);
                model.addAttribute("existingImageUrls", existing.getImageUrls());
                formPaper.setId(id);
                return "question-paper-edit";
            }
        } else if (finalUrls.isEmpty()) {
            // fallback to text
            List<String> textUrls = parseImageUrls(imageUrlsText);
            finalUrls.addAll(textUrls);
        }

        // Remove duplicates
        finalUrls = finalUrls.stream().distinct().toList();

        if (finalUrls.isEmpty()) {
            bindingResult.rejectValue("imageUrls", "error.imageUrls", "At least one image is required");
            model.addAttribute("imageUrlsText", imageUrlsText);
            model.addAttribute("existingImageUrls", existing.getImageUrls());
            formPaper.setId(id);
            return "question-paper-edit";
        }
        if (finalUrls.size() > MAX_IMAGES) {
            bindingResult.rejectValue("imageUrls", "error.imageUrls", "Maximum " + MAX_IMAGES + " images allowed (currently " + finalUrls.size() + ")");
            model.addAttribute("imageUrlsText", imageUrlsText);
            model.addAttribute("existingImageUrls", existing.getImageUrls());
            formPaper.setId(id);
            return "question-paper-edit";
        }

        formPaper.setImageUrls(finalUrls);
        Optional<Course> courseOpt = courseService.findByCourseCode(formPaper.getCourseCode().trim());
        if (courseOpt.isPresent()) {
            formPaper.setCourseTitle(courseOpt.get().getTitle());
        } else if (formPaper.getCourseTitle() == null || formPaper.getCourseTitle().isBlank()) {
            formPaper.setCourseTitle(formPaper.getCourseCode());
        }

        if (isAdmin(auth) && statusParam != null && !statusParam.isBlank()) {
            try {
                PaperStatus newStatus = PaperStatus.valueOf(statusParam);
                existing.setStatus(newStatus);
                questionPaperService.updateStatus(id, newStatus);
            } catch (IllegalArgumentException ignored) {}
        }
        formPaper.setUploadedBy(existing.getUploadedBy());
        if (!isAdmin(auth)) {
            formPaper.setStatus(existing.getStatus());
        } else if (statusParam == null) {
            formPaper.setStatus(existing.getStatus());
        } else {
            try {
                formPaper.setStatus(PaperStatus.valueOf(statusParam));
            } catch (Exception e) {
                formPaper.setStatus(existing.getStatus());
            }
        }

        questionPaperService.update(id, formPaper);
        if (isAdmin(auth) && statusParam != null) {
            try {
                questionPaperService.updateStatus(id, PaperStatus.valueOf(statusParam));
            } catch (Exception ignored) {}
        }
        redirectAttributes.addFlashAttribute("success", "Question paper updated.");
        return "redirect:/question-papers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, Authentication auth, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        QuestionPaper paper = opt.get();
        if (!canEditOrDelete(paper, auth)) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/403";
        }
        // If ADMIN, delete Cloudinary images safely
        if (isAdmin(auth) && paper.getImageUrls() != null) {
            for (String url : paper.getImageUrls()) {
                try {
                    cloudinaryService.deleteImage(url);
                } catch (Exception e) {
                    log.warn("Failed to delete Cloudinary image on admin delete {}: {}", url, e.getMessage());
                }
            }
        }
        // Also handle student delete: optionally delete images but spec says ADMIN, so we only do for admin
        // For safety, if student deletes own pending, we could also try to delete but not required
        questionPaperService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Question paper deleted.");
        return "redirect:/question-papers";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable String id,
                               @RequestParam("status") String status,
                               Authentication auth,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(auth)) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/403";
        }
        try {
            PaperStatus newStatus = PaperStatus.valueOf(status);
            questionPaperService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", "Status updated to " + newStatus);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid status");
        }
        return "redirect:/question-papers/" + id;
    }

    private List<String> parseImageUrls(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        String[] parts = text.split("[\\r\\n,]+");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isBlank()) result.add(t);
        }
        return result;
    }
}
