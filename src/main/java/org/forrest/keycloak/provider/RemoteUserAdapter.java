package org.forrest.keycloak.provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.forrest.keycloak.bind.RemoteUserEntity;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

class RemoteUserAdapter extends AbstractUserAdapter {
    private static final Logger log = Logger.getLogger(RemoteUserFederationProvider.class);
    private final RemoteUserEntity user;

    RemoteUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, RemoteUserEntity user) {
        super(session, realm, storageProviderModel);
        this.user = user;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        log.infof("[credentialManager] do...");
        return new LegacyUserCredentialManager(session, realm, this);
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isEmailVerified() {
        return user.isEmailVerified();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();
    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        for (Map.Entry<String, String> param : user.getAttributes().entrySet()) {
            attributes.add(param.getKey(), param.getValue());
        }
        return attributes;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        Map<String, List<String>> attributes = getAttributes();
        return (attributes.containsKey(name)) ? attributes.get(name).stream() : Stream.empty();
    }

    @Override
    public String getFirstAttribute(String name) {
        List<String> list = getAttributes().getOrDefault(name, List.of());
        return list.isEmpty() ? null : list.get(0);
    }
}
