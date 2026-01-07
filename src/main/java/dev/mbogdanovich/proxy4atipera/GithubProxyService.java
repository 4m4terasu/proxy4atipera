package dev.mbogdanovich.proxy4atipera;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GithubProxyService {

    private final GithubClient githubClient;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public GithubProxyService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    public List<RepositoryResponse> listNonForkRepositories(String username) {
        var nonForkRepos = githubClient.getUserRepos(username).stream()
                .filter(repo -> !repo.fork())
                .toList();

        var futures = nonForkRepos.stream()
                .map(repo -> CompletableFuture.supplyAsync(() -> toRepositoryResponse(repo), executor))
                .toList();

        return futures.stream().map(CompletableFuture::join).toList();
    }

    private RepositoryResponse toRepositoryResponse(GithubRepo repo) {
        var ownerLogin = repo.owner().login();
        var branches = githubClient.getRepoBranches(ownerLogin, repo.name()).stream()
                .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                .toList();

        return new RepositoryResponse(repo.name(), ownerLogin, branches);
    }
}
