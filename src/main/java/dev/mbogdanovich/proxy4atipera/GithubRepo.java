package dev.mbogdanovich.proxy4atipera;

public record GithubRepo(
        String name,
        boolean fork,
        GithubOwner owner
) {}
