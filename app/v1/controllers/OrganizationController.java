package v1.controllers;

import play.libs.Json;
import play.libs.ws.*;
import play.mvc.Result;
import v1.entities.Contributor;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static play.mvc.Results.ok;

public class OrganizationController implements WSBodyReadables, WSBodyWritables {
    private final WSClient ws;
    private final String token = System.getenv("GH_TOKEN");

    @Inject
    public OrganizationController(WSClient ws) {
        this.ws = ws;
    }

    private WSRequest getWSRequest(String uri) {
        return ws.url(format("https://api.github.com/{0}", uri))
                .addHeader("Authorization", format("token {0}", token));
    }

    public CompletionStage<Result> getContributions(String orgName) {
        return getRepositories(orgName)
                .thenApply(repositoriesResponse -> {
                    List<Contributor> listContributions = new ArrayList<>();
                    List<List<Contributor>> contributors = new ArrayList<>();
                    repositoriesResponse.asJson().forEach(json -> {
                        List<Contributor> listContributors = new ArrayList<>();
                        String name = json.get("name").toString().replaceAll("\"", "");
                        try {
                            List<Contributor> contributorsByRepo = getWSRequest(format("repos/{0}/{1}/contributors", orgName, name)).get()
                                    .thenApply(contributorsResponse -> {
                                        contributorsResponse.asJson().forEach(res2 -> {
                                            Contributor contributor = new Contributor(
                                                    res2.get("login").toString().replaceAll("\"", ""),
                                                    res2.get("contributions").asInt()
                                            );
                                            listContributors.add(contributor);
                                        });
                                        return listContributors;
                                    }).toCompletableFuture().get();
                            contributors.add(contributorsByRepo);
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });

                    List<Contributor> flat =
                            contributors.stream()
                                    .flatMap(List::stream)
                                    .collect(Collectors.toList());

                    Map<String, Integer> mapContributors = flat.stream().collect(Collectors.groupingBy(Contributor::getName, Collectors.summingInt(Contributor::getContributions)));

                    mapContributors.forEach((key, v) ->
                            listContributions.add(
                                    new Contributor(key,v)
                            )
                    );

                    listContributions.sort(Comparator.comparingInt(Contributor::getContributions).reversed());

                    return listContributions;

                }).thenApply(a -> ok(Json.toJson(a)));
    }

    public CompletionStage<WSResponse> getRepositories(String orgName) {
        return getWSRequest(format("orgs/{0}/repos", orgName)).get();
    }
}