package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.model.Course;
import com.seu.seuquestionbank.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public String courses(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Course> courses;
        if (q != null && !q.isBlank()) {
            courses = courseService.search(q);
        } else {
            courses = courseService.findAll();
        }
        model.addAttribute("courses", courses);
        model.addAttribute("q", q);
        model.addAttribute("totalCourses", courses.size());
        return "courses";
    }

    @GetMapping("/courses/{courseCode}")
    public String courseDetails(@PathVariable String courseCode, Model model) {
        Optional<Course> courseOpt = courseService.findByCourseCode(courseCode);
        if (courseOpt.isEmpty()) {
            return "error/404";
        }
        model.addAttribute("course", courseOpt.get());
        return "course-details";
    }
}
