package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.dto.RegisterDto;
import com.seu.seuquestionbank.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterDto());
        }
        model.addAttribute("currentPath", request.getRequestURI());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        model.addAttribute("currentPath", request.getRequestURI());

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "register";
        }

        try {
            userService.register(registerDto);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.toLowerCase().contains("email")) {
                bindingResult.rejectValue("email", "error.email", msg);
            } else {
                bindingResult.rejectValue("confirmPassword", "error.confirmPassword", msg);
            }
            return "register";
        }

        redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        model.addAttribute("currentPath", request.getRequestURI());
        return "login";
    }
}
