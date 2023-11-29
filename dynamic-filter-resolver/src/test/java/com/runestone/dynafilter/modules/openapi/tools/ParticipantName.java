package com.runestone.dynafilter.modules.openapi.tools;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, PARAMETER})
@NotBlank
@Size(max = 150)
@Pattern(regexp = "\\w*\\W*")
public @interface ParticipantName {
}
