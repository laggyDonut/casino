package de.edvschuleplattling.irgendwieanders;

import de.edvschuleplattling.irgendwieanders.Exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AlreadyCreatedException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyCreated(AlreadyCreatedException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DepositLimitMonthlyCounterException.class)
    public ResponseEntity<Map<String, Object>> handleDepositLimitMonthlyCounter(DepositLimitMonthlyCounterException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ExpiredIdException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredId(ExpiredIdException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(FalseTypeException.class)
    public ResponseEntity<Map<String, Object>> handleFalseType(FalseTypeException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IsNullException.class)
    public ResponseEntity<Map<String, Object>> handleIsNull(IsNullException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(NegativeValueException.class)
    public ResponseEntity<Map<String, Object>> handleNegativeValue(NegativeValueException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(StatusAlreadySetException.class)
    public ResponseEntity<Map<String, Object>> handleStatusAlreadySet(StatusAlreadySetException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(UnderageException.class)
    public ResponseEntity<Map<String, Object>> handleUnderage(UnderageException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ZeroOrNegativeValueException.class)
    public ResponseEntity<Map<String, Object>> handleZeroOrNegativeValue(ZeroOrNegativeValueException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    //Falls falscher Datentyp hinterlegt wird. Z.B.: String statt Integer
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, "Es wurde ein falscher Datentyp hinterlegt.", request);
    }

    //DTO-Validierungsfehler (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String error = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, error, request);
    }

    //Entity-Validierungsfehler (JPA-Annotations) --> prüft "Form" z.b. Min/Max
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {

        String error = ex.getConstraintViolations().iterator().next().getMessage();

        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, error, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    //Datenbankfehler --> prüft z.B. unique
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.BAD_REQUEST, "Der Datensatz verstößt gegen Datenbankregeln wie z.B. UNIQUE.", request);
    }

    //Alle anderen Fehler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherException(Exception ex, HttpServletRequest request) {
        return ApiErrorResponse.entity(HttpStatus.INTERNAL_SERVER_ERROR, "Ein unerwarteter Fehler ist aufgetreten: " + ex.getMessage(), request);
    }

}
