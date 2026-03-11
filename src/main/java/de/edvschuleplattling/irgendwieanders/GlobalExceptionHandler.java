package de.edvschuleplattling.irgendwieanders;

import de.edvschuleplattling.irgendwieanders.Exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AlreadyCreatedException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyCreated(AlreadyCreatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DepositLimitMonthlyCounterException.class)
    public ResponseEntity<Map<String, String>> handleDepositLimitMonthlyCounter(DepositLimitMonthlyCounterException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ExpiredIdException.class)
    public ResponseEntity<Map<String, String>> handleExpiredId(ExpiredIdException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(FalseTypeException.class)
    public ResponseEntity<Map<String, String>> handleFalseType(FalseTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IsNullException.class)
    public ResponseEntity<Map<String, String>> handleIsNull(IsNullException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NegativeValueException.class)
    public ResponseEntity<Map<String, String>> handleNegativeValue(NegativeValueException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(StatusAlreadySetException.class)
    public ResponseEntity<Map<String, String>> handleStatusAlreadySet(StatusAlreadySetException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnderageException.class)
    public ResponseEntity<Map<String, String>> handleUnderage(UnderageException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ZeroOrNegativeValueException.class)
    public ResponseEntity<Map<String, String>> handleZeroOrNegativeValue(ZeroOrNegativeValueException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    //Falls falscher Datentyp hinterlegt wird. Z.B.: String statt Integer
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Es wurde ein falscher Datentyp hinterlegt."));
    }

    //DTO-Validierungsfehler (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {

        String error = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", error));
    }

    //Entity-Validierungsfehler (JPA-Annotations) --> prüft "Form" z.b. Min/Max
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {

        String error = ex.getConstraintViolations().iterator().next().getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", error));
    }

    //Datenbankfehler --> prüft z.B. unique
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body
                (Map.of("error", "Der Datensatz verstößt gegen Datenbankregeln wie z.B. UNIQUE."));
    }

    //Alle anderen Fehler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOtherException(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ein unerwarteter Fehler ist aufgetreten: " + ex.getMessage()));
        }

    }

