package com.seu.seuquestionbank.repository;

import com.seu.seuquestionbank.enums.PaperStatus;
import com.seu.seuquestionbank.model.QuestionPaper;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionPaperRepository extends MongoRepository<QuestionPaper, String> {
    List<QuestionPaper> findByStatus(PaperStatus status);
    List<QuestionPaper> findByUploadedBy(String uploadedBy);
    List<QuestionPaper> findByCourseCode(String courseCode);
    List<QuestionPaper> findByStatusAndUploadedBy(PaperStatus status, String uploadedBy);
}
