package org.forrest.keycloak.provider;

import java.util.*;
import java.util.stream.Stream;

import org.forrest.keycloak.bind.RemoteUserEntity;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import static org.forrest.keycloak.bind.RemoteUserStorageProviderConstants.ADD_ROLES_TO_TOKEN;
import static org.forrest.keycloak.bind.RemoteUserStorageProviderConstants.DEBUG_ENABLED;

class RemoteUserAdapter extends AbstractUserAdapterFederatedStorage {
    private static final Logger logger = Logger.getLogger(RemoteUserFederationProvider.class);
    private final ComponentModel model;
    private final RemoteUserEntity user;
    private final String keycloakId;
    private final String username;

    RemoteUserAdapter(ComponentModel model, KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, RemoteUserEntity user) {
        super(session, realm, storageProviderModel);
        this.user = user;
        this.keycloakId = StorageId.keycloakId(storageProviderModel, user.getId());
        this.username = user.getUserName();
        this.model = model;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        log("[credentialManager] new LegacyUserCredentialManager...");
        return new LegacyUserCredentialManager(session, realm, this);
    }

    @Override
    public String getId() {
        return keycloakId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String s) {
        this.user.setUserName(s);
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

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        Stream<RoleModel> roleMappings = super.getRoleMappingsStream();
        boolean addFederationRoles = Boolean.parseBoolean(model.get(ADD_ROLES_TO_TOKEN));
        if (!addFederationRoles) {
            return roleMappings;
        }

        for (String role : user.getRoles()) {
            RoleModel roleModel = realm.getRole(role);
            if (roleModel == null) {
                roleModel = realm.addRole(role);
                log("Adding role %s", role);
            }
            //log.infof("Granting role %s to user %s during user import from Remote", role, username);
            //this.grantRole(roleModel);
            roleMappings = Stream.concat(roleMappings, Stream.of(roleModel));
        }
        return roleMappings;
    }

    private void log(String message, Object... params) {
        if (Boolean.parseBoolean(model.get(DEBUG_ENABLED))) {
            logger.infof(message, params);
        }
    }
}
