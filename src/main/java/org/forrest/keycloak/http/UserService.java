package org.forrest.keycloak.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.forrest.keycloak.bind.RemoteCredentialInput;
import org.forrest.keycloak.bind.RemoteUserEntity;
import org.forrest.keycloak.bind.VerifyPasswordResponse;
import org.keycloak.component.ComponentModel;

import java.io.IOException;
import java.util.*;

import static org.forrest.keycloak.bind.RemoteUserStorageProviderConstants.*;

public class UserService {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient;
    private final String findUserUrl;
    private final String verifyUserUrl;
    private final String searchUserUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(ComponentModel model) {
        this.httpClient = new OkHttpClient();
        this.findUserUrl = model.get(REMOTE_PROVIDER_URL) + model.get(FIND_USER_ENDPOINT);
        this.verifyUserUrl = model.get(REMOTE_PROVIDER_URL) + model.get(VERIFY_USER_ENDPOINT);
        this.searchUserUrl = model.get(REMOTE_PROVIDER_URL) + model.get(SEARCH_USER_ENDPOINT);
    }

    public List<RemoteUserEntity> getUsers(String username) throws IOException {
        Response response = queryUsers(searchUserUrl, username);
        if (response.body() == null ) {
            return null;
        }
        return objectMapper.readValue(response.body().string(), new TypeReference<List<RemoteUserEntity>>() {
        });
    }

    public RemoteUserEntity getUser(String username) throws IOException {
        Response response = queryUsers(findUserUrl, username);
        if (response.body() == null ) {
            return null;
        }
        return objectMapper.readValue(response.body().string(), RemoteUserEntity.class);
    }

    public int getUserCount() throws IOException {
        return 0;
    }

    public VerifyPasswordResponse verifyPassword(String username, String password) throws IOException {
        RequestBody formBody = RequestBody.create(objectMapper.writeValueAsString(new RemoteCredentialInput(username, password)), JSON);
        okhttp3.Request.Builder builder = new Request.Builder()
                .url(verifyUserUrl)
                .addHeader("User-Agent", "Keycloak SPI");
        Request request = builder.post(formBody).build();
        Response response = httpClient.newCall(request).execute();
        if (response.body() == null ) {
            return new VerifyPasswordResponse(false);
        }
        return objectMapper.readValue(response.body().string(), VerifyPasswordResponse.class);
    }

    private Response queryUsers(String url, String username) throws IOException {
        Map<String, String> params = new HashMap<>() {{
            put("username", username);
        }};
        HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            httpBuilder.addQueryParameter(param.getKey(), param.getValue());
        }
        Request request = new Request.Builder().url(httpBuilder.build()).build();
        return httpClient.newCall(request).execute();
    }
}
