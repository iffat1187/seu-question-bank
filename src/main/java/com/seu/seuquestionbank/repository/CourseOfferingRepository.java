package com.seu.seuquestionbank.repository;

import com.seu.seuquestionbank.model.CourseOffering;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseOfferingRepository extends MongoRepository<CourseOffering, String> {
    List<CourseOffering> findByCourseCode(String courseCode);
    List<CourseOffering> findBySemesterAndAcademicYear(String semester, Integer academicYear);
    List<CourseOffering> findByFacultyId(String facultyId);
}
