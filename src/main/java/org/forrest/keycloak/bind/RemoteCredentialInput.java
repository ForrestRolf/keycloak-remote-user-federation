package org.forrest.keycloak.bind;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RemoteCredentialInput {
    String username;
    String password;
}
