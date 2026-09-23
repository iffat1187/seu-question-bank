package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.service.CourseService;
import com.seu.seuquestionbank.service.DepartmentService;
import com.seu.seuquestionbank.service.ProgramService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CourseService courseService;
    private final ProgramService programService;
    private final DepartmentService departmentService;

    public HomeController(CourseService courseService,
                          ProgramService programService,
                          DepartmentService departmentService) {
        this.courseService = courseService;
        this.programService = programService;
        this.departmentService = departmentService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("courseCount", courseService.count());
        model.addAttribute("programCount", programService.count());
        model.addAttribute("departmentCount", departmentService.count());
        return "home";
    }
}
