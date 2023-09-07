package com.example.client.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {
    private String field;
    private String fieldMatch;

    @Override
    public void initialize(FieldsValueMatch constraintAnnotation) {
        field = constraintAnnotation.field();
        fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(object).getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(object).getPropertyValue(fieldMatch);
        return Objects.equals(fieldValue, fieldMatchValue);
    }
}
