package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.enums.ExamType;
import com.seu.seuquestionbank.enums.PaperStatus;
import com.seu.seuquestionbank.model.Course;
import com.seu.seuquestionbank.model.QuestionPaper;
import com.seu.seuquestionbank.service.CourseService;
import com.seu.seuquestionbank.service.QuestionPaperService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/question-papers")
public class QuestionPaperController {

    private final QuestionPaperService questionPaperService;
    private final CourseService courseService;

    public QuestionPaperController(QuestionPaperService questionPaperService, CourseService courseService) {
        this.questionPaperService = questionPaperService;
        this.courseService = courseService;
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
        // student can update/delete own PENDING
        return isOwner(paper, auth) && paper.getStatus() == PaperStatus.PENDING;
    }

    @GetMapping
    public String list(Model model, HttpServletRequest request, Authentication auth) {
        List<QuestionPaper> papers;
        boolean admin = isAdmin(auth);
        if (admin) {
            papers = questionPaperService.findAll();
        } else if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // authenticated student: approved + own (including pending)
            List<QuestionPaper> approved = questionPaperService.findApproved();
            List<QuestionPaper> own = questionPaperService.findByUploadedBy(auth.getName());
            // merge without duplicates
            Map<String, QuestionPaper> map = new LinkedHashMap<>();
            for (QuestionPaper p : approved) map.put(p.getId(), p);
            for (QuestionPaper p : own) map.put(p.getId(), p);
            papers = new ArrayList<>(map.values());
            // sort by createdAt desc
            papers.sort(Comparator.comparing(QuestionPaper::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            papers = questionPaperService.findApproved();
            papers.sort(Comparator.comparing(QuestionPaper::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        model.addAttribute("papers", papers);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("currentPath", request.getRequestURI());
        // for filtering in template if needed
        model.addAttribute("currentUser", auth != null ? auth.getName() : null);
        return "question-papers";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model model, HttpServletRequest request, Authentication auth) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/404";
        }
        QuestionPaper paper = opt.get();
        if (!canView(paper, auth)) {
            // hide existence: return 404 for public trying to view pending of others
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/404";
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
        // for imageUrls textarea
        model.addAttribute("imageUrlsText", "");
        return "question-paper-create";
    }

    @PostMapping("/create")
    public String createSubmit(@Valid @ModelAttribute("questionPaper") QuestionPaper questionPaper,
                               BindingResult bindingResult,
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

        // parse imageUrls
        List<String> urls = parseImageUrls(imageUrlsText);
        questionPaper.setImageUrls(urls);

        // set courseTitle from course if exists
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
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/404";
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
        return "question-paper-edit";
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable String id,
                             @Valid @ModelAttribute("questionPaper") QuestionPaper formPaper,
                             BindingResult bindingResult,
                             @RequestParam(value = "imageUrlsText", required = false) String imageUrlsText,
                             @RequestParam(value = "status", required = false) String statusParam,
                             Model model,
                             HttpServletRequest request,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/404";
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
            // keep id for form
            formPaper.setId(id);
            return "question-paper-edit";
        }

        // parse urls
        List<String> urls = parseImageUrls(imageUrlsText);
        formPaper.setImageUrls(urls);

        // courseTitle
        Optional<Course> courseOpt = courseService.findByCourseCode(formPaper.getCourseCode().trim());
        if (courseOpt.isPresent()) {
            formPaper.setCourseTitle(courseOpt.get().getTitle());
        } else if (formPaper.getCourseTitle() == null || formPaper.getCourseTitle().isBlank()) {
            formPaper.setCourseTitle(formPaper.getCourseCode());
        }

        // handle status change only for admin
        if (isAdmin(auth) && statusParam != null && !statusParam.isBlank()) {
            try {
                PaperStatus newStatus = PaperStatus.valueOf(statusParam);
                // admin can change status; if student tries to set status we ignore
                existing.setStatus(newStatus);
                questionPaperService.updateStatus(id, newStatus);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // update other fields (service will keep uploadedBy/status if not admin)
        // we must not overwrite uploadedBy
        formPaper.setUploadedBy(existing.getUploadedBy());
        // keep original status if not admin changed above
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
        // if admin changed status, ensure it persists
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
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/404";
        }
        QuestionPaper paper = opt.get();
        if (!canEditOrDelete(paper, auth)) {
            model.addAttribute("currentPath", request.getRequestURI());
            return "error/403";
        }
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
        // split by newline or comma
        String[] parts = text.split("[\\r\\n,]+");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isBlank()) result.add(t);
        }
        return result;
    }
}
