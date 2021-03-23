package v1.get;

import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static play.mvc.Results.ok;

public class OrganizationController implements WSBodyReadables, WSBodyWritables {
    private final WSClient ws;
    String token = System.getenv("GH_TOKEN");

    @Inject
    public OrganizationController(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<Result> getContributions(String orgName) {

        return getRepositories(orgName).thenApply(r -> {
            ArrayList listFuture = new ArrayList();

            r.asJson().forEach(res -> {
                String repository = res.get("name").toString().replaceAll("\"", "");
                System.out.println(repository);

                listFuture.add(ws.url("https://api.github.com/repos/" + orgName + "/" + repository + "/contributors")
                                .addHeader("Authorization", "token " + token)
                                .get()
                                .thenApply(r5 -> {

                                    ArrayList<Contributor> listContributors = new ArrayList<Contributor>();

                                    r5.asJson().forEach(res2 -> {
                                        listContributors.add(new Contributor(
                                                res2.get("login").toString().replaceAll("\"", ""),
                                                res2.get("contributions").asInt()
                                        ));
                                    });

                                    Map<String, Integer> mapContributors = listContributors.stream().collect(Collectors.groupingBy(Contributor::getName, Collectors.summingInt(Contributor::getContributions)));
//
                                    mapContributors.forEach((key, v) -> System.out.println(key + ":" + v)
                                    );

                                    return listContributors;

                                })
                );
            });

            return listFuture;
        }).thenApply(r ->  ok());


//        return ws.url("https://api.github.com/repos/" + orgName + "/" + orgName + "/contributors")
//                .addHeader("Authorization", "token " + token)
//                .get()
//                .thenApply(r -> ok(r.asJson()));

    }

    public CompletionStage<WSResponse> getRepositories(String orgName) {
        return ws.url("https://api.github.com/orgs/" + orgName + "/repos")
                .addHeader("Authorization", "token " + token)
                .get();
    }
}
