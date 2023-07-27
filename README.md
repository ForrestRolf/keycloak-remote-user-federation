# Keycloak Remote User Provider

# Deploy

* Copy target/remote-user-federation-jar-with-dependencies.jar to /opt/keycloak/providers//opt/keycloak/providers/remote-user-federation-jar-with-dependencies.jar
* Restart Keycloak

# Usage

## Configuration

| Name                                | Default  | Description                                  |
|:------------------------------------|:---------|:---------------------------------------------|
| Remote User Information Url         | https:// | Rest API endpoint providing users            |
| Define endpoint for find user       | /find    | Rest API subpath for find user by name,email |
| Define endpoint for verify password | /verify  | Rest API subpath for verify user password    |
| Define endpoint for search users    | /search  | Rest API subpath for seach users by name     |
| Enable detail logs                  | false    | Print detail logs                            |

## API Response Schema

### /find
```json
{
  "firstName": "foo",
  "lastName": "bar",
  "userName": "foobar",
  "email": "keycloak@foobar.com",
  "emailVerified": true,
  "enabled": true,
  "attributes": {
    "attr1": "val"
  }
}
```
### /verify
```json
{
  "valid": true
}
```
### /search
```json
[
  {
    "firstName": "foo",
    "lastName": "bar",
    "userName": "foobar",
    "email": "keycloak@foobar.com",
    "emailVerified": true,
    "enabled": true,
    "attributes": {
      "attr1": "val"
    }
  }
]
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
