package com.seu.seuquestionbank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "programs")
public class Program {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String code;

    private String degree;

    private String description;

    private boolean active;
}
