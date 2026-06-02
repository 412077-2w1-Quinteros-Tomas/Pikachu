package ar.edu.utn.frc.tup.piii.exceptions;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException of(String entityName, Object id) {
        return new EntityNotFoundException(entityName + " not found with id: " + id);
    }
}
