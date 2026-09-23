package com.seu.seuquestionbank.controller;

import com.seu.seuquestionbank.model.Course;
import com.seu.seuquestionbank.model.CourseOffering;
import com.seu.seuquestionbank.model.Department;
import com.seu.seuquestionbank.model.Faculty;
import com.seu.seuquestionbank.model.Program;
import com.seu.seuquestionbank.service.CourseOfferingService;
import com.seu.seuquestionbank.service.CourseService;
import com.seu.seuquestionbank.service.DepartmentService;
import com.seu.seuquestionbank.service.FacultyService;
import com.seu.seuquestionbank.service.ProgramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AcademicApiController {

    private final ProgramService programService;
    private final DepartmentService departmentService;
    private final CourseService courseService;
    private final FacultyService facultyService;
    private final CourseOfferingService courseOfferingService;

    public AcademicApiController(ProgramService programService,
                                 DepartmentService departmentService,
                                 CourseService courseService,
                                 FacultyService facultyService,
                                 CourseOfferingService courseOfferingService) {
        this.programService = programService;
        this.departmentService = departmentService;
        this.courseService = courseService;
        this.facultyService = facultyService;
        this.courseOfferingService = courseOfferingService;
    }

    @GetMapping("/api/programs")
    public List<Program> programs() {
        return programService.findAll();
    }

    // Also support view spec: GET /programs (returns JSON if Accept JSON, but spec says page - handled by view controller fallback)
    // Provide REST alias required by spec
    @GetMapping("/programs")
    public ResponseEntity<?> programsView() {
        // If client expects HTML, HomeController/Course flow handles browser; for API JSON fallback return JSON
        // This keeps REST simple - return JSON. Thymeleaf view is via separate controller if needed but spec allows GET /programs for browsing
        // To avoid collision with HTML, we return JSON and let content negotiation decide - simplest: return JSON list
        return ResponseEntity.ok(programService.findAll());
    }

    @GetMapping("/api/departments")
    public List<Department> departments() {
        return departmentService.findAll();
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> departmentsView() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @GetMapping("/api/courses")
    public List<Course> courses() {
        return courseService.findAll();
    }

    @GetMapping("/api/courses/{courseCode}")
    public ResponseEntity<Course> courseByCode(@PathVariable String courseCode) {
        return courseService.findByCourseCode(courseCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/faculty")
    public List<Faculty> faculty() {
        return facultyService.findAll();
    }

    @GetMapping("/faculty")
    public ResponseEntity<List<Faculty>> facultyView() {
        return ResponseEntity.ok(facultyService.findAll());
    }

    @GetMapping("/api/course-offerings")
    public List<CourseOffering> offerings() {
        return courseOfferingService.findAll();
    }

    @GetMapping("/course-offerings")
    public ResponseEntity<List<CourseOffering>> offeringsView() {
        return ResponseEntity.ok(courseOfferingService.findAll());
    }
}
