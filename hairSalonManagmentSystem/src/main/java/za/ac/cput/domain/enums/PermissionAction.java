package za.ac.cput.domain.enums;

/** What a role may do inside a module. VIEW is implied by any other action on the same module. */
public enum PermissionAction {
    VIEW, CREATE, EDIT, DELETE
}
