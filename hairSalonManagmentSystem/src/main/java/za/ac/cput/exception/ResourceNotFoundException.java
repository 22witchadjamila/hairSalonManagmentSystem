package za.ac.cput.exception;

/** Thrown when a requested entity can't be found by its ID. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forEntity(String entityName, String id) {
        return new ResourceNotFoundException(entityName + " not found with id: " + id);
    }
}
