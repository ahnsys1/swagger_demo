package com.swagger.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Represents an item in the system.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the item.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private long id;

    @NotBlank(message = "Item name cannot be blank.")
    @Size(min = 2, max = 50)
    @Schema(description = "Name of the item.", example = "Laptop", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description of the item.", example = "A powerful laptop for development.")
    private String description;
}
