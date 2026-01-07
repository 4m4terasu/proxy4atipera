# proxy4atipera

A small Spring Boot proxy service that lists a GitHub user’s **non-fork** repositories together with their branches and last commit SHA

## Tech stack

- Java 25
- Spring Boot 4.0.1 (WebMVC)
- Spring RestClient
- Gradle Kotlin DSL
- Integration tests: JUnit 5 + WireMock

## API

### `GET /users/{username}/repositories`

Returns only repositories where `fork = false`.

Response fields:
- `repositoryName`
- `ownerLogin`
- `branches[]`:
  - `name`
  - `lastCommitSha`

#### 200 example

Example response for `GET /users/4m4terasu/repositories`:

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

#### 404 example

Example response for `GET /users/CristianoRonaldo777dontexist/repositories`:

```json
{
  "status": 404,
  "message": "GitHub user 'CristianoRonaldo777dontexist' not found"
}

