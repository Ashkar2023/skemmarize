package skemmarize.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {

        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({BadRequestException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> BadRequestException(Exception ex){
        String message = ex.getMessage();

        if(ex instanceof MissingServletRequestPartException){
            MissingServletRequestPartException missingEx = (MissingServletRequestPartException) ex;
            message = "param '" + missingEx.getRequestPartName() +"' is missing";
        }

        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), message);
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JwtValidationException.class)
    public ResponseEntity<ApiError> handleJwtValidation(JwtValidationException ex) {
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ DataIntegrityViolationException.class, DuplicateKeyException.class })
    public ResponseEntity<ApiError> handleDataConflict(DataIntegrityViolationException ex) {
        String message = (ex instanceof DuplicateKeyException)
                ? "Data conflict: A record with this unique value already exists."
                : "Database constraint violation.";

        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    // basic
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception E) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occured: " + E.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
