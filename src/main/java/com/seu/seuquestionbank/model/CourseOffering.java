package com.seu.seuquestionbank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "course_offerings")
public class CourseOffering {

    @Id
    private String id;

    private String courseCode;

    private String facultyId;

    private String semester;

    private Integer academicYear;

    private Integer batch;

    private String section;

    @Builder.Default
    private boolean active = true;
}
