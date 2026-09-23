package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.service.DepartmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/departments")
    public String departments(Model model, HttpServletRequest request) {
        var departments = departmentService.findAll();
        long activeCount = departments.stream().filter(d -> d.isActive()).count();
        model.addAttribute("departments", departments);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("currentPath", request.getRequestURI());
        return "departments";
    }
}
