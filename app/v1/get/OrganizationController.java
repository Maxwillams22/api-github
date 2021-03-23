package v1.get;
import play.libs.Json;
import play.libs.ws.*;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

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
                    return contributors;

                }).thenApply(a -> ok(Json.toJson(a)));
    }

    public CompletionStage<WSResponse> getRepositories(String orgName) {
        return getWSRequest(format("orgs/{0}/repos", orgName)).get();
    }
}