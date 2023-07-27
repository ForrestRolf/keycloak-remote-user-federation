package org.forrest.keycloak.bind;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteUserEntity {
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private boolean emailVerified;
    private boolean enabled;
    private Map<String, String> attributes;
}
