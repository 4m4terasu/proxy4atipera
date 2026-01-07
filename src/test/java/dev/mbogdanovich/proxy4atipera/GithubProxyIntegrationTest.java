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

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.util.StopWatch;

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
void shouldFetchBranchesInParallel_andMeetTimingAndRequestCount() throws Exception {
    WireMock.configureFor("localhost", wiremock.getRuntimeInfo().getHttpPort());

    wiremock.stubFor(get(urlEqualTo("/users/john/repos"))
            .willReturn(okJson("""
                    [
                      { "name": "repo-a", "fork": false, "owner": { "login": "john" } },
                      { "name": "repo-b", "fork": false, "owner": { "login": "john" } },
                      { "name": "repo-fork", "fork": true,  "owner": { "login": "john" } }
                    ]
                    """).withFixedDelay(1000)));

    wiremock.stubFor(get(urlEqualTo("/repos/john/repo-a/branches"))
            .willReturn(okJson("""
                    [
                      { "name": "main", "commit": { "sha": "aaa111" } },
                      { "name": "dev",  "commit": { "sha": "bbb222" } },
                      { "name": "hotfix", "commit": { "sha": "ccc333" } }
                    ]
                    """).withFixedDelay(1000)));

    wiremock.stubFor(get(urlEqualTo("/repos/john/repo-b/branches"))
            .willReturn(okJson("""
                    [
                      { "name": "main", "commit": { "sha": "ddd444" } },
                      { "name": "dev",  "commit": { "sha": "eee555" } },
                      { "name": "release", "commit": { "sha": "fff666" } }
                    ]
                    """).withFixedDelay(1000)));

    var req = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/users/john/repositories"))
            .GET()
            .build();

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    stopWatch.stop();

    assertThat(resp.statusCode()).isEqualTo(200);

    // fork repo must be filtered
    assertThat(resp.body()).contains("\"repositoryName\":\"repo-a\"");
    assertThat(resp.body()).contains("\"repositoryName\":\"repo-b\"");
    assertThat(resp.body()).doesNotContain("repo-fork");

    // total requests to GH (WireMock): 1 (repos) + 2 (branches) = 3
    WireMock.verify(3, getRequestedFor(urlMatching(".*")));

    long ms = stopWatch.getTotalTimeMillis();
    assertThat(ms).isBetween(2000L, 3000L);
}

    @Test
    void shouldReturn404WhenGithubUserDoesNotExist() throws Exception {
        wiremock.stubFor(get(urlEqualTo("/users/ghost/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"message\":\"Not Found\"}")
                        .withFixedDelay(1000)));

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
