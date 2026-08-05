package za.ac.cput.domain;

import jakarta.persistence.*;

public class Role {

    @Id
    private String roleId;
    private String name;
    private String description;

}
