package dev.mbogdanovich.proxy4atipera;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GithubUserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleGithubUserNotFound(GithubUserNotFoundException ex) {
        var body = new ApiErrorResponse(404, "GitHub user '" + ex.username() + "' not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
