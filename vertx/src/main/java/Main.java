import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

/**
 * Created by mty on 2/5/17.
 */
public class Main {
    public static void main(String[] args) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        Vertx vertx = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(availableProcessors);
        vertx.deployVerticle("TinyUrlVerticle", deploymentOptions, deployAsyncResult -> {
            System.out.println("Main started at port 8080");
        });
    }
}
