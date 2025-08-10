package com.gardrops.shared.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

class NotFoundException(msg: String): RuntimeException(msg)
class ValidationException(msg: String): RuntimeException(msg)
class RateLimitException(msg: String): RuntimeException(msg)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException) =
        problem(HttpStatus.NOT_FOUND, "Resource not found", "NOT_FOUND", ex.message)

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException) =
        problem(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION", ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBind(ex: MethodArgumentNotValidException): ProblemDetail {
        val pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION", null)
        val errors = ex.bindingResult.fieldErrors.map { it.toMap() }
        pd.setProperty("errors", errors)
        return pd
    }

    @ExceptionHandler(RateLimitException::class)
    fun handle429(ex: RateLimitException) =
        problem(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded", "RATE_LIMIT", ex.message)

    @ExceptionHandler(Exception::class)
    fun handleGeneric(@Suppress("UNUSED_PARAMETER") ex: Exception) =
        problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "INTERNAL", null)

    private fun FieldError.toMap() = mapOf(
        "field" to (field),
        "rejectedValue" to (rejectedValue?.toString() ?: ""),
        "message" to (defaultMessage ?: "invalid")
    )

    private fun problem(status: HttpStatus, title: String, code: String, detail: String?): ProblemDetail {
        val pd = ProblemDetail.forStatusAndDetail(status, detail ?: title)
        pd.title = title
        pd.type = URI.create("about:blank#$code")
        pd.setProperty("code", code)
        return pd
    }
}
