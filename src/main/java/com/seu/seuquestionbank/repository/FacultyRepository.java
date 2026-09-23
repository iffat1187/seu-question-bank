package com.seu.seuquestionbank.repository;

import com.seu.seuquestionbank.model.Faculty;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FacultyRepository extends MongoRepository<Faculty, String> {
    List<Faculty> findByDepartmentCode(String departmentCode);
    List<Faculty> findByActiveTrue();
}
