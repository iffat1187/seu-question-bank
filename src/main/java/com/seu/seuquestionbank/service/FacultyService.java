package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.model.Faculty;
import com.seu.seuquestionbank.repository.FacultyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<Faculty> findAll() {
        return facultyRepository.findAll();
    }

    public long count() {
        return facultyRepository.count();
    }
}
