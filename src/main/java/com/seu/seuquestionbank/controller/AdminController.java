package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.enums.PaperStatus;
import com.seu.seuquestionbank.model.QuestionPaper;
import com.seu.seuquestionbank.service.CloudinaryService;
import com.seu.seuquestionbank.service.QuestionPaperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final QuestionPaperService questionPaperService;
    private final CloudinaryService cloudinaryService;

    public AdminController(QuestionPaperService questionPaperService, CloudinaryService cloudinaryService) {
        this.questionPaperService = questionPaperService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<QuestionPaper> papers = questionPaperService.findAll();
        papers.sort(Comparator.comparing(QuestionPaper::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        long total = papers.size();
        long pending = papers.stream().filter(p -> p.getStatus() == PaperStatus.PENDING).count();
        long approved = papers.stream().filter(p -> p.getStatus() == PaperStatus.APPROVED).count();
        long rejected = papers.stream().filter(p -> p.getStatus() == PaperStatus.REJECTED).count();

        model.addAttribute("papers", papers);
        model.addAttribute("totalCount", total);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("approvedCount", approved);
        model.addAttribute("rejectedCount", rejected);
        model.addAttribute("currentPath", "/admin");
        return "admin/dashboard";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            questionPaperService.updateStatus(id, PaperStatus.APPROVED);
            redirectAttributes.addFlashAttribute("success", "Question paper approved and is now public.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            questionPaperService.updateStatus(id, PaperStatus.REJECTED);
            redirectAttributes.addFlashAttribute("success", "Question paper rejected. It is no longer public.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<QuestionPaper> opt = questionPaperService.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Question paper not found: " + id);
            return "redirect:/admin";
        }
        QuestionPaper paper = opt.get();
        if (paper.getImageUrls() != null) {
            for (String url : paper.getImageUrls()) {
                try {
                    cloudinaryService.deleteImage(url);
                } catch (Exception e) {
                    log.warn("Failed to delete Cloudinary image {}: {}", url, e.getMessage());
                }
            }
        }
        questionPaperService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Question paper deleted.");
        return "redirect:/admin";
    }
}