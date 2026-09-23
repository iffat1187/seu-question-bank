package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.model.Program;
import com.seu.seuquestionbank.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public List<Program> findAll() {
        return programRepository.findAll();
    }

    public long count() {
        return programRepository.count();
    }
}
