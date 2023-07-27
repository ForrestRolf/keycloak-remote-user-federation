package org.forrest.keycloak.provider;

import org.forrest.keycloak.bind.RemoteUserEntity;
import org.forrest.keycloak.bind.VerifyPasswordResponse;
import org.forrest.keycloak.http.UserService;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
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

    public int getUsersCount(RealmModel realm) {
        log("getUsersCount: realm=%s", realm.getName());
        try {
            return userService.getUserCount();
        } catch (Exception e) {
            throw new RuntimeException("Remote server error:" + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log("closing provider");
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        log("getUserById(%s)", id);
        StorageId sid = new StorageId(id);
        return getUserByUsername(realm, sid.getExternalId());
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        log("getUserByUsername(%s)", username);
        try {
            RemoteUserEntity userEntity = userService.getUser(username);
            if (userEntity == null) {
                log("Remote user not found");
                return null;
            }
            log("Remote user %s", userEntity.toString());
            return mapUser(realm, userEntity);
        } catch (Exception e) {
            log("Remote server response with error %s", e.getMessage());
            return null;
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log("getUserByEmail(%s)", email);
        try {
            RemoteUserEntity userEntity = userService.getUser(email);
            if (userEntity == null) {
                log("Remote user not found");
                return null;
            }
            log("Remote user %s", userEntity.toString());
            return mapUser(realm, userEntity);
        } catch (Exception e) {
            log("Remote server response with error %s", e.getMessage());
            return null;
        }
    }

    private UserModel mapUser(RealmModel realm, RemoteUserEntity userEntity) {
        return new RemoteUserAdapter(session, realm, model, userEntity);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params, Integer firstResult, Integer maxResults) {
        String username = params.get(UserModel.SEARCH);
        log("Search with username %s", username);
        try {
            List<RemoteUserEntity> userEntities = userService.getUsers(username);
            return userEntities.stream().map(entity -> mapUser(realmModel, entity));
        } catch (IOException e) {
            log(e.getMessage());
            return Stream.empty();
        }
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return Stream.empty();
    }

    private void log(String message, Object... params) {
        if (Boolean.parseBoolean(model.get(DEBUG_ENABLED))) {
            logger.infof("[RemoteUserFederationProvider] " + message, params);
        }
    }
}
