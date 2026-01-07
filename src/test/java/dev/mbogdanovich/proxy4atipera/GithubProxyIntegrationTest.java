package dev.mbogdanovich.proxy4atipera;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubProxyIntegrationTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("github.base-url", () -> wiremock.getRuntimeInfo().getHttpBaseUrl());
    }

    @LocalServerPort
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void shouldReturnNonForkReposWithBranches() throws Exception {
        wiremock.stubFor(get(urlEqualTo("/users/john/repos"))
                .willReturn(okJson("""
                        [
                          {
                            "name": "fork-repo",
                            "fork": true,
                            "owner": { "login": "john" }
                          },
                          {
                            "name": "main-repo",
                            "fork": false,
                            "owner": { "login": "john" }
                          }
                        ]
                        """)));

        wiremock.stubFor(get(urlEqualTo("/repos/john/main-repo/branches"))
                .willReturn(okJson("""
                        [
                          { "name": "main", "commit": { "sha": "aaa111" } },
                          { "name": "dev",  "commit": { "sha": "bbb222" } }
                        ]
                        """)));

        var req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/users/john/repositories"))
                .GET()
                .build();

        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        assertThat(resp.statusCode()).isEqualTo(200);

        assertThat(resp.body()).contains("\"repositoryName\":\"main-repo\"");
        assertThat(resp.body()).contains("\"ownerLogin\":\"john\"");
        assertThat(resp.body()).contains("\"name\":\"main\"");
        assertThat(resp.body()).contains("\"lastCommitSha\":\"aaa111\"");
        assertThat(resp.body()).doesNotContain("fork-repo");
    }

    @Test
    void shouldReturn404WhenGithubUserDoesNotExist() throws Exception {
        wiremock.stubFor(get(urlEqualTo("/users/ghost/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"message\":\"Not Found\"}")));

        var req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/users/ghost/repositories"))
                .GET()
                .build();

        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        assertThat(resp.statusCode()).isEqualTo(404);
        assertThat(resp.body()).contains("\"status\":404");
        assertThat(resp.body()).contains("ghost");
    }
}
