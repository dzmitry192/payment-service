package com.innowise.sivachenko.model.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionListDto {
    public List<ExceptionErrorDto> errors;
}
