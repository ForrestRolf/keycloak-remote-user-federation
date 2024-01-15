# Keycloak Remote User Provider

# Deploy

* Copy `target/remote-user-federation-jar-with-dependencies.jar` to `/opt/keycloak/providers/remote-user-federation-jar-with-dependencies.jar`
* Restart Keycloak

> [!NOTE]
> When user federation is enabled, the user list will not load data initially and needs to be searched manually.
> Tip: To search for all users, use * in the search.

# Usage

## Configuration

| Name                                | Default  | Description                                                                                                                                    |
|:------------------------------------|:---------|:-----------------------------------------------------------------------------------------------------------------------------------------------|
| Remote server                       | https:// | Rest API endpoint providing users                                                                                                              |
| Define endpoint for find user       | /find    | Rest API subpath for find user by id,name,email                                                                                                |
| Define endpoint for verify password | /verify  | Rest API subpath for verify user password                                                                                                      |
| Define endpoint for search users    | /search  | Rest API subpath for seach users                                                                                                               |
| Define endpoint for count users     | /count   | Rest API subpath for count users                                                                                                               |
| Authorization                       |          | HTTP Authorization request header                                                                                                              |
| Add roles to token                  | true     | If this option is enabled, a realm role will be automatically created and returned in the token.                                               |
| Enable detail logs                  | false    | Print detail logs                                                                                                                              |

## API Response Schema

### `GET` /find

**Params**

| Name       | Description                              | Example                                      |
|:-----------|:-----------------------------------------|:---------------------------------------------|
| `type`     | Valid values are id, username, or email. |                                              |
| `id`       |                                          | `/find?type=id&id=1`                         |
| `username` |                                          | `/find?type=username&username=foobar`        |
| `email`    |                                          | `/find?type=email&email=keycloak@foobar.com` |

**Response**

```json
{
  "id": "1",
  "firstName": "foo",
  "lastName": "bar",
  "userName": "remotefoobar",
  "email": "keycloak@foobar.com",
  "emailVerified": true,
  "enabled": true,
  "attributes": {
    "public_email": "public@foobar.com",
    "private_email": "private@foobar.com"
  },
  "createdAt": "2024-01-15T12:34:13+08:00",
  "roles": ["role1", "role2"]
}
```


### `POST` /verify

**Params**

| Name       | Description |
|:-----------|:------------|
| `username` |             |
| `password` |             |


**Response**
```json
{
  "valid": true
}
```
### `GET` /search

**Params**

| Name                                                         | Description                                                               |
|:-------------------------------------------------------------|:--------------------------------------------------------------------------|
| `method`                                                     | Search by role or search by user information. Valid values are user, role |
| `role`                                                       | If you search by role name, the value exists.                             |
| `skip`                                                       | Offset used for pagination                                                |
| `take`                                                       | Limit used for pagination                                                 |
| `keycloak.session.realm.users.query.include_service_account` |                                                                           |
| `keycloak.session.realm.users.query.search`                  | Keywords entered in keycloak console                                      |

**Response**
```json
[
  {
    "id": "1",
    "firstName": "foo",
    "lastName": "bar",
    "userName": "remotefoobar",
    "email": "keycloak@foobar.com",
    "emailVerified": true,
    "enabled": true,
    "attributes": {
      "public_email": "public@foobar.com",
      "private_email": "private@foobar.com"
    },
    "createdAt": "2024-01-15T12:34:13+08:00",
    "roles": ["role1", "role2"]
  }
]
```

### `GET` /count

**Params**

| Name                                                         | Description                                                               |
|:-------------------------------------------------------------|:--------------------------------------------------------------------------|
| `keycloak.session.realm.users.query.include_service_account` |                                                                           |
| `keycloak.session.realm.users.query.search`                  | Keywords entered in keycloak console                                      |

**Response**
```json
{
  "total": 100
}
```

# Development

## Prerequisites

* Docker Compose
* JDK 17
* maven > 3.3 installed on your computer

## Debugging

```shell
mvn clean package
docker compose up -d
```
