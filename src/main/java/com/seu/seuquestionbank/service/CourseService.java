package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.model.Course;
import com.seu.seuquestionbank.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    public List<Course> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String trimmed = keyword.trim();
        return courseRepository.findByCourseCodeContainingIgnoreCaseOrTitleContainingIgnoreCase(trimmed, trimmed);
    }

    public List<Course> findByProgramCode(String programCode) {
        if (programCode == null || programCode.isBlank()) {
            return Collections.emptyList();
        }
        return courseRepository.findByProgramCode(programCode);
    }

    public long count() {
        return courseRepository.count();
    }
}
