import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.*;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import org.bson.*;
import org.bson.conversions.Bson;

import java.util.Date;

/**
 * Created by mty on 2/6/17.
 */
public class TinyUrlVerticle extends AbstractVerticle {
    private final static char[] base58Chars = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static MongoCollection<Document> countersCollection;
    private static MongoCollection<Document> urlsCollection;

    @Override
    public void start() throws Exception {
        Vertx vertx = getVertx();
        HttpServer httpServer = vertx.createHttpServer();

        RedisClient redis = RedisClient.create(vertx);

        if (countersCollection == null) {
            synchronized (TinyUrlVerticle.class) {
                if (countersCollection == null) {
                    MongoClient mongoClient = MongoClients.create("mongodb://localhost");
                    MongoDatabase mongoDatabase = mongoClient.getDatabase("tiny_url");
                    countersCollection = mongoDatabase.getCollection("counters");
                    urlsCollection = mongoDatabase.getCollection("urls");
                }
            }
        }

        Router router = Router.router(vertx);

        Route longUrlRoute = router.route(HttpMethod.GET, "/:short_url");
        longUrlRoute.handler((RoutingContext routingContext) -> {
            HttpServerRequest request = routingContext.request();
            String shortUrl = request.getParam("short_url");
            HttpServerResponse response = request.response();
            redis.get(shortUrl, (AsyncResult<String> stringAsyncResult) -> {
                if (stringAsyncResult.succeeded()) {
                    response.setStatusCode(302).putHeader("Location", addPrefixIfNotExists(stringAsyncResult.result())).end();
                } else {
                    findUrlDocumentByShortUrl(shortUrl).first((document, thr) -> {
                        if (thr == null) {
                            String longUrl = document.getString("long_url");
                            response.setStatusCode(302).putHeader("Location", addPrefixIfNotExists(longUrl));
                            redis.set(shortUrl, longUrl, null);
                        } else {
                            thr.printStackTrace();
                            response.setStatusCode(500).end();
                        }
                    });
                }
            });
        });

        Route urlShortenRoute = router.route(HttpMethod.POST, "/url/shorten");
        urlShortenRoute.handler((RoutingContext routingContext) -> {
            HttpServerRequest request = routingContext.request();
            String longUrl = request.getParam("url");
            HttpServerResponse response = request.response();
            getNextShortUrlId((shortUrlIdDocument, thr1) -> {
                if (thr1 == null) {
                    int shortUrlId = 0;
                    if (shortUrlIdDocument != null) {
                        shortUrlId = shortUrlIdDocument.getInteger("value", 0);
                    }
                    String shortUrl = base58Encode(shortUrlId);
                    insertIntoUrlsCollection(longUrl, shortUrl, (result, thr2) -> {
                        if (thr2 == null) {
                            response.end(new JsonObject().put("short_url", shortUrl).toString());
                            redis.set(shortUrl, longUrl, null);
                        } else {
                            thr2.printStackTrace();
                            response.setStatusCode(500).end();
                        }
                    });
                } else {
                    thr1.printStackTrace();
                    response.setStatusCode(500).end();
                }
            });
        });

        httpServer.requestHandler(router::accept).listen(8080);
    }

    private void getNextShortUrlId(SingleResultCallback<Document> callback) {
        Bson filter = new BsonDocument().append("key", new BsonString("short_url_id"));
        Bson update = new BsonDocument().append("$inc", new BsonDocument().append("value", new BsonInt32(1)));
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true);
        countersCollection.findOneAndUpdate(filter, update, options, callback);
    }

    private void insertIntoUrlsCollection(String longUrl, String shortUrl, SingleResultCallback<Void> callback) {
        Document document = new Document()
                .append("long_url", new BsonString(longUrl))
                .append("short_url", new BsonString(shortUrl))
                .append("create_date", new BsonDateTime(new Date().getTime()));
        urlsCollection.insertOne(document, callback);
    }

    private FindIterable<Document> findUrlDocumentByShortUrl(String shortUrl) {
        Bson filter = new BsonDocument().append("short_url", new BsonString(shortUrl));
        return urlsCollection.find(filter);
    }

    private String addPrefixIfNotExists(String url) {
        if (url.indexOf("http://") == 0 || url.indexOf("https://") == 0 || url.indexOf("ftp://") == 0) {
            return url;
        }
        return "http://" + url;
    }

    private String base58Encode(int number) {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder stringBuilder = new StringBuilder(12);
        while (number >= 58) {
            int remains = number % 58;
            stringBuilder.append(base58Chars[remains]);
            number /= 58;
        }
        stringBuilder.append(base58Chars[number]);
        return stringBuilder.toString();
    }
}
