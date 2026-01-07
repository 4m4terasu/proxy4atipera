package dev.mbogdanovich.proxy4atipera;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
// import java.util.Objects;

@Component
public class GithubClient {

    private static final String DEFAULT_BASE_URL = "https://api.github.com";
    private static final String USER_REPOS_PATH = "/users/{username}/repos";
    private static final String REPO_BRANCHES_PATH = "/repos/{owner}/{repo}/branches";

    private final RestClient restClient;

    public GithubClient(RestClient.Builder builder,
                        @Value("${github.base-url:" + DEFAULT_BASE_URL + "}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<GithubRepo> getUserRepos(String username) {
        try {
            GithubRepo[] repos = restClient.get()
                    .uri(USER_REPOS_PATH, username)
                    .retrieve()
                    .body(GithubRepo[].class);

            return repos == null ? List.of() : List.of(repos);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new GithubUserNotFoundException(username);
            }
            throw e;
        }
    }

    public List<GithubBranch> getRepoBranches(String owner, String repo) {
        GithubBranch[] branches = restClient.get()
                .uri(REPO_BRANCHES_PATH, owner, repo)
                .retrieve()
                .body(GithubBranch[].class);

        return branches == null ? List.of() : List.of(branches);
    }
}
