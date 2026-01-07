package dev.mbogdanovich.proxy4atipera;

public class GithubUserNotFoundException extends RuntimeException {
    private final String username;

    public GithubUserNotFoundException(String username) {
        super("GitHub user '" + username + "' not found");
        this.username = username;
    }

    public String username() {
        return username;
    }
}
