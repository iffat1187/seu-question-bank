package com.seu.seuquestionbank.repository;

import com.seu.seuquestionbank.model.Program;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProgramRepository extends MongoRepository<Program, String> {
    Optional<Program> findByCode(String code);
    boolean existsByCode(String code);
}
