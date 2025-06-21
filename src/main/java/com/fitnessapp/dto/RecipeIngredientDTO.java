package com.fitnessapp.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientDTO {
    private Integer id;
    private String  name;
    private Double  quantityGrams;
}