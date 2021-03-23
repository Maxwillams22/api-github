package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.nashorn.internal.ir.RuntimeNode;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import javax.print.attribute.standard.RequestingUserName;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final AssetsFinder assetsFinder;

    @Inject
    public HomeController(AssetsFinder assetsFinder) {
        this.assetsFinder = assetsFinder;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(
            form.render(
                "Digite o nome da organização.","",
                    assetsFinder
            ));
    }

    public Result show(String name) {
//        return ok(request.toString());

        return ok(
                listContributors.render(
                        "Organização escolhida " + name,
                        assetsFinder
                ));
    }

}
