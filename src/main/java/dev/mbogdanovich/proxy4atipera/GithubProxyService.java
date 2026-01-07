package dev.mbogdanovich.proxy4atipera;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubProxyService {

    private final GithubClient githubClient;

    public GithubProxyService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    public List<RepositoryResponse> listNonForkRepositories(String username) {
        var repos = githubClient.getUserRepos(username);

        return repos.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> toRepositoryResponse(repo))
                .toList();
    }

    private RepositoryResponse toRepositoryResponse(GithubRepo repo) {
        var ownerLogin = repo.owner().login();
        var branches = githubClient.getRepoBranches(ownerLogin, repo.name()).stream()
                .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                .toList();

        return new RepositoryResponse(repo.name(), ownerLogin, branches);
    }
}
