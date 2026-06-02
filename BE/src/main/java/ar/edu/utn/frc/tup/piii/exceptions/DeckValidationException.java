package ar.edu.utn.frc.tup.piii.exceptions;

import java.util.List;

public class DeckValidationException extends RuntimeException {

    private final List<String> errors;

    public DeckValidationException(List<String> errors) {
        super("Deck validation failed: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
