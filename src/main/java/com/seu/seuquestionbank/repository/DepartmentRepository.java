package com.seu.seuquestionbank.repository;

import com.seu.seuquestionbank.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByCode(String code);
    boolean existsByCode(String code);
}
