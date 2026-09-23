package com.seu.seuquestionbank.service;

import com.seu.seuquestionbank.enums.PaperStatus;
import com.seu.seuquestionbank.model.QuestionPaper;

import java.util.List;
import java.util.Optional;

public interface QuestionPaperService {
    QuestionPaper create(QuestionPaper paper);
    List<QuestionPaper> findAll();
    List<QuestionPaper> findApproved();
    List<QuestionPaper> findByUploadedBy(String email);
    Optional<QuestionPaper> findById(String id);
    QuestionPaper update(String id, QuestionPaper updated);
    void delete(String id);
    QuestionPaper updateStatus(String id, PaperStatus status);
    long count();
}
