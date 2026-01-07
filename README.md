# proxy4atipera

```md
Minimal Spring Boot (WebMVC) service that proxies GitHub REST API v3 to return (branch details are fetched in parallel):
 - Only non-fork repositories
 - Repository name + owner login
 - Branches with the last commit SHA

## Quick start

```bash
./gradlew bootRun
````

```bash
curl http://localhost:8080/users/4m4terasu/repositories
```

## Endpoint

### GET /users/{username}/repositories

Returns an array of repositories with:

* repositoryName
* ownerLogin
* branches: name, lastCommitSha

Note: if the user exists but has no public non-fork repositories, the response is 200 with [].

#### Example (200, when user exists and has public non-fork repositories)

```json
[
  {
    "repositoryName": "Chess-app",
    "ownerLogin": "4m4terasu",
    "branches": [
      {
        "name": "gh-pages",
        "lastCommitSha": "9464ed3fd8d6bf83dbf582a592f37c407b340cb0"
      },
      {
        "name": "main",
        "lastCommitSha": "9b112f42ffe772c4918a51df6d0b76f0ad83a0b0"
      }
    ]
  },
  {
    "repositoryName": "proxy4atipera",
    "ownerLogin": "4m4terasu",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "d11f81a4fd6e30287cd0b12d610d56c8a15b10a0"
      }
    ]
  },
  {
    "repositoryName": "ToDo_List",
    "ownerLogin": "4m4terasu",
    "branches": [
      {
        "name": "gh-pages",
        "lastCommitSha": "8865299eb32082a8b5408665787dd9239044e442"
      },
      {
        "name": "main",
        "lastCommitSha": "60da054c9e8c28281ae7c6733967100431901fbb"
      }
    ]
  }
]
```

#### Example (404)

```json
{
  "status": 404,
  "message": "GitHub user 'CristianoRonaldo777dontexist' not found"
}
```

## Tests

Integration tests use WireMock (fixed 1000ms delay per mocked GitHub request) and verify:
- total number of GitHub calls (3)
- total processing time in the 2000–3000ms range (branches are fetched in parallel)

```bash
./gradlew clean test
```

Stack: Java 25, Spring Boot 4.0.1 (WebMVC), Spring RestClient, Gradle Kotlin DSL, JUnit 5 + WireMock.