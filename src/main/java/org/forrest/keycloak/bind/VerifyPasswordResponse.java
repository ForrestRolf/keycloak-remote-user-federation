package org.forrest.keycloak.bind;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VerifyPasswordResponse {
    private boolean valid;
}
