package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.model.CourseOffering;
import com.seu.seuquestionbank.repository.CourseOfferingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseOfferingService {

    private final CourseOfferingRepository courseOfferingRepository;

    public CourseOfferingService(CourseOfferingRepository courseOfferingRepository) {
        this.courseOfferingRepository = courseOfferingRepository;
    }

    public List<CourseOffering> findAll() {
        return courseOfferingRepository.findAll();
    }

    public long count() {
        return courseOfferingRepository.count();
    }
}
