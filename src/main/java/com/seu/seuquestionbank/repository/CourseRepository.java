package com.seu.seuquestionbank.repository;

import com.seu.seuquestionbank.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends MongoRepository<Course, String> {

    Optional<Course> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    List<Course> findByCourseCodeContainingIgnoreCaseOrTitleContainingIgnoreCase(String code, String title);

    List<Course> findByProgramCode(String programCode);
}
