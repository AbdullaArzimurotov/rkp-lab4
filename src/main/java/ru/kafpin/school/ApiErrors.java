package ru.kafpin.school;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail onInvalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return invalidFields(fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail onInvalidPatch(ConstraintViolationException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(error ->
                fields.putIfAbsent(error.getPropertyPath().toString(), error.getMessage()));
        return invalidFields(fields);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail onInvalidSelection(IllegalArgumentException exception) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail onMalformedBody() {
        return badRequest("Проверьте структуру JSON, названия и типы полей.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail onInvalidId() {
        return badRequest("Идентификатор должен быть целым числом.");
    }

    private ProblemDetail invalidFields(Map<String, String> fields) {
        ProblemDetail problem = badRequest("Проверьте значения полей запроса.");
        problem.setProperty("fields", fields);
        return problem;
    }

    private ProblemDetail badRequest(String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problem.setTitle("Некорректный запрос");
        return problem;
    }
}
