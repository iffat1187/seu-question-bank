package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.model.User;
import com.seu.seuquestionbank.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request, Authentication authentication) {
        model.addAttribute("currentPath", request.getRequestURI());
        if (authentication == null) {
            return "redirect:/login";
        }
        String email = authentication.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        model.addAttribute("user", user);
        return "profile";
    }
}
