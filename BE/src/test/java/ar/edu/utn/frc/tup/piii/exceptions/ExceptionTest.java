package ar.edu.utn.frc.tup.piii.exceptions;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void entityNotFoundException_of_containsEntityAndId() {
        UUID id = UUID.randomUUID();
        EntityNotFoundException ex = EntityNotFoundException.of("Card", id);

        assertThat(ex.getMessage()).contains("Card");
        assertThat(ex.getMessage()).contains(id.toString());
    }

    @Test
    void entityNotFoundException_message() {
        EntityNotFoundException ex = new EntityNotFoundException("not found");
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    void deckValidationException_containsErrors() {
        DeckValidationException ex = new DeckValidationException(java.util.List.of("error1", "error2"));
        assertThat(ex.getErrors()).containsExactly("error1", "error2");
        assertThat(ex.getMessage()).contains("error1");
    }

    @Test
    void invalidActionException_containsMessage() {
        InvalidActionException ex = new InvalidActionException("bad action");
        assertThat(ex.getMessage()).isEqualTo("bad action");
    }

    @Test
    void gameRuleViolationException_containsMessage() {
        GameRuleViolationException ex = new GameRuleViolationException("rule violated");
        assertThat(ex.getMessage()).isEqualTo("rule violated");
    }
}
