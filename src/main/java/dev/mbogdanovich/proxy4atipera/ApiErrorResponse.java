package dev.mbogdanovich.proxy4atipera;

public record ApiErrorResponse(
        int status,
        String message
) {}
