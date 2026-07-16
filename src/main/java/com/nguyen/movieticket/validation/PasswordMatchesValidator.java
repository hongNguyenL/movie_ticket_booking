package com.nguyen.movieticket.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            PropertyDescriptor passwordDesc = BeanUtils.getPropertyDescriptor(value.getClass(), "password");
            PropertyDescriptor confirmPasswordDesc = BeanUtils.getPropertyDescriptor(value.getClass(), "confirmPassword");

            if (passwordDesc == null || confirmPasswordDesc == null) {
                return true;
            }

            Object password = passwordDesc.getReadMethod().invoke(value);
            Object confirmPassword = confirmPasswordDesc.getReadMethod().invoke(value);

            return password != null && password.equals(confirmPassword);
        } catch (Exception e) {
            return true;
        }
    }
}
