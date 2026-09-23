package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.model.Department;
import com.seu.seuquestionbank.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public long count() {
        return departmentRepository.count();
    }
}
