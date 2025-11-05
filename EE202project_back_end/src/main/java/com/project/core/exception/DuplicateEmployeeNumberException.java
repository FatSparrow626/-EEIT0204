package com.project.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmployeeNumberException extends RuntimeException {
    public DuplicateEmployeeNumberException(String message) {
        super(message);
    }
}
