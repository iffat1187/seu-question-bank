package com.seu.seuquestionbank.service.impl;

import com.seu.seuquestionbank.enums.PaperStatus;
import com.seu.seuquestionbank.model.QuestionPaper;
import com.seu.seuquestionbank.repository.QuestionPaperRepository;
import com.seu.seuquestionbank.service.QuestionPaperService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionPaperServiceImpl implements QuestionPaperService {

    private final QuestionPaperRepository repository;

    public QuestionPaperServiceImpl(QuestionPaperRepository repository) {
        this.repository = repository;
    }

    @Override
    public QuestionPaper create(QuestionPaper paper) {
        if (paper.getStatus() == null) {
            paper.setStatus(PaperStatus.PENDING);
        }
        if (paper.getCreatedAt() == null) {
            paper.setCreatedAt(Instant.now());
        }
        // Clean imageUrls: trim, remove empty, filter blank
        if (paper.getImageUrls() != null) {
            paper.setImageUrls(paper.getImageUrls().stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList());
        }
        return repository.save(paper);
    }

    @Override
    public List<QuestionPaper> findAll() {
        return repository.findAll();
    }

    @Override
    public List<QuestionPaper> findApproved() {
        return repository.findByStatus(PaperStatus.APPROVED);
    }

    @Override
    public List<QuestionPaper> findByUploadedBy(String email) {
        return repository.findByUploadedBy(email);
    }

    @Override
    public Optional<QuestionPaper> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public QuestionPaper update(String id, QuestionPaper updated) {
        QuestionPaper existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question paper not found: " + id));

        existing.setCourseCode(updated.getCourseCode());
        existing.setCourseTitle(updated.getCourseTitle());
        existing.setFacultyName(updated.getFacultyName());
        existing.setSemester(updated.getSemester());
        existing.setAcademicYear(updated.getAcademicYear());
        existing.setExamType(updated.getExamType());
        if (updated.getImageUrls() != null) {
            existing.setImageUrls(updated.getImageUrls().stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList());
        }
        // status and uploadedBy should be handled separately via updateStatus / not overwritten here
        // but admin may change status via updateStatus, not via generic update
        return repository.save(existing);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public QuestionPaper updateStatus(String id, PaperStatus status) {
        QuestionPaper existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question paper not found: " + id));
        existing.setStatus(status);
        return repository.save(existing);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public List<QuestionPaper> filter(List<QuestionPaper> source, String search, String courseCode,
                                      String semester, Integer academicYear, String examType) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        String q = search == null ? "" : search.trim().toLowerCase();
        String cc = courseCode == null ? "" : courseCode.trim().toLowerCase();
        String sem = semester == null ? "" : semester.trim().toLowerCase();
        String et = examType == null ? "" : examType.trim().toLowerCase();

        List<QuestionPaper> result = new ArrayList<>();
        for (QuestionPaper p : source) {
            if (!q.isEmpty()) {
                boolean matches = contains(p.getCourseCode(), q)
                        || contains(p.getCourseTitle(), q)
                        || contains(p.getFacultyName(), q);
                if (!matches) {
                    continue;
                }
            }
            if (!cc.isEmpty() && !cc.equalsIgnoreCase(trim(p.getCourseCode()))) {
                continue;
            }
            if (!sem.isEmpty() && !sem.equalsIgnoreCase(trim(p.getSemester()))) {
                continue;
            }
            if (academicYear != null && !academicYear.equals(p.getAcademicYear())) {
                continue;
            }
            if (!et.isEmpty()) {
                if (p.getExamType() == null || !et.equals(p.getExamType().name().toLowerCase())) {
                    continue;
                }
            }
            result.add(p);
        }
        return result;
    }

    private boolean contains(String value, String lowerCaseQuery) {
        return value != null && value.toLowerCase().contains(lowerCaseQuery);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
