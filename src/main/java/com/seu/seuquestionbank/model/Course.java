package com.seu.seuquestionbank.model;

import com.seu.seuquestionbank.enums.CourseCategory;
import com.seu.seuquestionbank.enums.CourseType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @NotBlank(message = "Course code must not be blank")
    @Indexed(unique = true)
    private String courseCode;

    @NotBlank(message = "Course title must not be blank")
    private String title;

    @Min(value = 0, message = "Credit cannot be negative")
    private double credit;

    private CourseCategory category;

    private CourseType courseType;

    @Builder.Default
    private List<String> prerequisiteCodes = new ArrayList<>();

    private Integer minimumSemester;

    private Integer requiredCredits;

    private String alternateCourseCode;

    private String programCode;

    @Builder.Default
    private boolean active = true;
}
