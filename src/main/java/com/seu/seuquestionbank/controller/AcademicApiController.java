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

    @GetMapping("/api/departments")
    public List<Department> departments() {
        return departmentService.findAll();
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

    @GetMapping("/api/course-offerings")
    public List<CourseOffering> offerings() {
        return courseOfferingService.findAll();
    }
}
