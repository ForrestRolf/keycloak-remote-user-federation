package org.forrest.keycloak.provider;

import org.forrest.keycloak.http.UserService;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.jboss.logging.Logger;

import java.util.List;

import static org.forrest.keycloak.bind.RemoteUserStorageProviderConstants.*;

public class RemoteUserFederationProviderFactory implements UserStorageProviderFactory<RemoteUserFederationProvider> {
    private static final Logger log = Logger.getLogger(RemoteUserFederationProviderFactory.class);
    protected final List<ProviderConfigProperty> configMetadata;

    public RemoteUserFederationProviderFactory() {
        log.info("Remote user federation provider created");
        configMetadata = ProviderConfigurationBuilder.create()
                .property().name(REMOTE_PROVIDER_URL).label("Remote server").type(ProviderConfigProperty.STRING_TYPE).defaultValue("https://").add()
                .property().name(AUTHORIZATION).label("Authorization").type(ProviderConfigProperty.STRING_TYPE).defaultValue("").add()
                .property().name(FIND_USER_ENDPOINT).label("Define endpoint for find user").type(ProviderConfigProperty.STRING_TYPE).defaultValue("/user").add()
                .property().name(VERIFY_USER_ENDPOINT).label("Define endpoint for verify password").type(ProviderConfigProperty.STRING_TYPE).defaultValue("/verify").add()
                .property().name(SEARCH_USER_ENDPOINT).label("Define endpoint for search users").type(ProviderConfigProperty.STRING_TYPE).defaultValue("/search").add()
                .property().name(COUNT_USER_ENDPOINT).label("Define endpoint for count users").type(ProviderConfigProperty.STRING_TYPE).defaultValue("/count").add()
                .property().name(ADD_ROLES_TO_TOKEN).label("Add roles to token").type(ProviderConfigProperty.BOOLEAN_TYPE).defaultValue(true).add()
                .property().name(DEBUG_ENABLED).label("Enable detail logs").type(ProviderConfigProperty.BOOLEAN_TYPE).defaultValue("false").add()
                .build();
    }

    @Override
    public RemoteUserFederationProvider create(KeycloakSession session, ComponentModel model) {
        return new RemoteUserFederationProvider(session, model, new UserService(model));
    }

    @Override
    public UserStorageProvider create(KeycloakSession session) {
        return UserStorageProviderFactory.super.create(session);
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public int order() {
        return UserStorageProviderFactory.super.order();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return UserStorageProviderFactory.super.getConfigMetadata();
    }

    @Override
    public void init(Config.Scope config) {
        UserStorageProviderFactory.super.init(config);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        UserStorageProviderFactory.super.postInit(factory);
    }

    @Override
    public void close() {
        UserStorageProviderFactory.super.close();
    }

    @Override
    public String getHelpText() {
        return UserStorageProviderFactory.super.getHelpText();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public <C> C getConfig() {
        return UserStorageProviderFactory.super.getConfig();
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        boolean valid = true;
        String comment = "";

        final String url = config.get(REMOTE_PROVIDER_URL);
        if (url != null && url.length() < URL_MIN_LENGTH) {
            valid = false;
            comment = "Please check the url.";
        }
        if (!valid) {
            throw new ComponentValidationException("Unable to validate configuration. Err: " + comment);
        }
    }

    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {

    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {

    }
}
