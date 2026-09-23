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
@Document(collection = "faculties")
public class Faculty {

    @Id
    private String id;

    private String name;

    private String designation;

    private String departmentCode;

    private String email;

    private boolean active;
}
