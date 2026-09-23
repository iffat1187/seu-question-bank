package com.seu.seuquestionbank.model;

import com.seu.seuquestionbank.enums.ExamType;
import com.seu.seuquestionbank.enums.PaperStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "question_papers")
public class QuestionPaper {

    @Id
    private String id;

    @NotBlank(message = "Course code is required")
    private String courseCode;

    private String courseTitle;

    private String facultyName;

    private String semester;

    private Integer academicYear;

    private ExamType examType;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    private String uploadedBy;

    @Builder.Default
    private PaperStatus status = PaperStatus.PENDING;

    @CreatedDate
    private Instant createdAt;
}
