package dev.mbogdanovich.proxy4atipera;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GithubProxyController {

    private final GithubProxyService service;

    public GithubProxyController(GithubProxyService service) {
        this.service = service;
    }

    @GetMapping("/users/{username}/repositories")
    public List<RepositoryResponse> listUserRepositories(@PathVariable String username) {
        return service.listNonForkRepositories(username);
    }
}
