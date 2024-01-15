package org.forrest.keycloak.provider;

import org.forrest.keycloak.bind.RemoteUserEntity;
import org.forrest.keycloak.bind.VerifyPasswordResponse;
import org.forrest.keycloak.http.UserService;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;


import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import static org.forrest.keycloak.bind.RemoteUserStorageProviderConstants.DEBUG_ENABLED;

public class RemoteUserFederationProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider {
    private static final Logger logger = Logger.getLogger(RemoteUserFederationProvider.class);
    private final KeycloakSession session;
    private final ComponentModel model;

    private final UserService userService;

    public RemoteUserFederationProvider(KeycloakSession session, ComponentModel model, UserService userService) {
        this.session = session;
        this.model = model;
        this.userService = userService;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.endsWith(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        log("isConfiguredFor(realm=%s,user=%s,credentialType=%s)", realm.getName(), user.getUsername(), credentialType);
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        log("isValid(realm=%s,user=%s,credentialInput.type=%s)", realm.getName(), user.getUsername(), credentialInput.getType());
        try {
            VerifyPasswordResponse response = userService.verifyPassword(user.getUsername(), credentialInput.getChallengeResponse());
            if (response == null) {
                log("Remote verify service response empty");
                return false;
            }
            log("Verify result %s", response.isValid());
            return response.isValid();
        } catch (Exception e) {
            throw new RuntimeException("Remote server error:" + e.getMessage(), e);
        }
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        log("Get users count");
        return userService.getUserCount(null).getTotal();
    }

    @Override
    public int getUsersCount(RealmModel realm, Set<String> groupIds) {
        log("Get users count by group %s", groupIds);
        return userService.getUserCount(null).getTotal();
    }

    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params) {
        log("Get users count by params %s", params);
        return userService.getUserCount(params).getTotal();
    }

    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params, Set<String> groupIds) {
        log("Get users count by params and group %s, %s", params, groupIds);
        return userService.getUserCount(params).getTotal();
    }

    @Override
    public int getUsersCount(RealmModel realm, boolean includeServiceAccount) {
        log("Get users count by includeServiceAccount");
        return userService.getUserCount(null).getTotal();
    }

    @Override
    public void close() {
        log("closing provider");
    }


    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        log("Get user by id: %s", id);
        StorageId sid = new StorageId(id);
        return getUser(realm, "id", sid.getExternalId());
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        log("Get user by username %s", username);
        return getUser(realm, "username", username);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log("Get user by email %s", email);
        return getUser(realm, "email", email);
    }

    private UserModel getUser(RealmModel realm, String type, String search) {
        try {
            RemoteUserEntity userEntity = null;
            switch (type) {
                case "id": {
                    userEntity = userService.getUserById(search);
                    break;
                }
                case "username": {
                    userEntity = userService.getUserByUsername(search);
                    break;
                }
                case "email": {
                    userEntity = userService.getUserByEmail(search);
                    break;
                }
            }
            if (userEntity == null) {
                log("Remote user not found");
                return null;
            }
            log("Remote user %s", userEntity.toString());
            return mapUser(realm, userEntity);
        } catch (Exception e) {
            log("Remote server error %s", e.getMessage());
            return null;
        }
    }

    @Override
    public CredentialValidationOutput getUserByCredential(RealmModel realm, CredentialInput input) {
        return UserLookupProvider.super.getUserByCredential(realm, input);
    }

    private UserModel mapUser(RealmModel realm, RemoteUserEntity userEntity) {
        return new RemoteUserAdapter(model, session, realm, model, userEntity);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params, Integer firstResult, Integer maxResults) {
        log("Search users: param=%s, firstResult=%d, maxResults=%d", params, firstResult, maxResults);
        try {
            if (!params.containsKey("method")) {
                params.put("method", "user");
            }
            List<RemoteUserEntity> userEntities = userService.searchUsers(params, firstResult, maxResults);
            return userEntities.stream().map(entity -> mapUser(realmModel, entity));
        } catch (IOException e) {
            log(e.getMessage());
            return Stream.empty();
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        return searchForUserStream(realm, params, null, null);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role) {
        Map<String, String> params = new HashMap<>() {{
            put("method", "role");
            put("role", role.getName());
        }};
        return searchForUserStream(realm, params, null, null);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role, Integer firstResult, Integer maxResults) {
        Map<String, String> params = new HashMap<>() {{
            put("method", "role");
            put("role", role.getName());
        }};
        return searchForUserStream(realm, params, firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return Stream.empty();
    }

    @Override
    public void preRemove(RealmModel realm) {
        log("pre-remove realm");
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        log("pre-remove group");
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        log("pre-remove role");
    }

    private void log(String message, Object... params) {
        if (Boolean.parseBoolean(model.get(DEBUG_ENABLED))) {
            logger.infof(message, params);
        }
    }
}
