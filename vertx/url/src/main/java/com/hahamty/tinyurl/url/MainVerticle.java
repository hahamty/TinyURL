package com.hahamty.tinyurl.url;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.asyncsql.MySQLClient;
import io.vertx.rxjava.ext.sql.SQLClient;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.redis.RedisClient;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainVerticle extends AbstractVerticle {
  private int machineId;
  private RedisClient redisClient;
  private SQLClient mySQLClient;

  /**
   * Everything is initialized after machineId is initialized
   */
  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    // init machineId
    EventBus eventBus = vertx.eventBus();

    byte[] macAddress = null;
    Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
    while (interfaceEnumeration.hasMoreElements()) {
      NetworkInterface networkInterface = interfaceEnumeration.nextElement();
      if (!networkInterface.isLoopback()
          && networkInterface.isUp()
          && !networkInterface.isVirtual()
          && !networkInterface.isPointToPoint()) {
        macAddress = networkInterface.getHardwareAddress();
        break;
      }
    }
    if (macAddress == null) {
      startFuture.fail("No valid network interface");
    }

    eventBus.<JsonObject>rxRequest("get-machine-id", new JsonObject().put("mac-address", macAddress))
        .map(Message::body)
        .subscribe(
            messageBody -> {
              machineId = messageBody.getInteger("machine-id");

              // init redisClient
              redisClient = RedisClient.create(vertx);

              // init mySQLClient
              mySQLClient = MySQLClient.createShared(
                  vertx, new JsonObject().put("username", "root").put("password", "123").put("database", "tinyurl"));

              // init httpServer
              Router router = Router.router(vertx);
              router.get("/url/s/:shortUrl").handler(this::shortUrlToLongUrl);
              router.post("/url").handler(this::longUrlToShortUrl);

              vertx.createHttpServer().requestHandler(router).listen(8080);

              startFuture.complete();
            },
            e -> {
              e.printStackTrace();
              startFuture.fail(e);
            }
        );
  }

  private void shortUrlToLongUrl(RoutingContext routingContext) {
    String shortUrl = routingContext.pathParam("shortUrl");
    HttpServerResponse response = routingContext.response();
    List<String> fields = new ArrayList<>(1);
    fields.add(shortUrl);
    redisClient.rxHmget("url", fields).subscribe(
        array -> {
          System.out.println(array.getValue(0));
          if (array.size() == 0 || array.getString(0) == null) {
            mySQLClient.rxGetConnection().subscribe(
                connection -> {
                  connection.rxQueryWithParams(
                      "SELECT longUrl FROM Url WHERE shortUrl=?", new JsonArray().add(shortUrl)).subscribe(
                      resultSet -> {
                        if (resultSet.getNumRows() == 0) {
                          // TODO
                          return;
                        }
                        String longUrl = resultSet.getRows().get(0).getString("longUrl");
                        response.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                            .end(new JsonObject().put("result", longUrl).toString());
                        JsonObject json = new JsonObject().put("shortUrl", shortUrl).put("longUrl", longUrl);
                        redisClient.rxHmset("url", new JsonObject().put(shortUrl, json.toString())).subscribe();
                      },
                      e -> {
                        // TODO
                      }
                  );
                },
                e -> {
                  // TODO
                }
            );
            return;
          }
          JsonObject json = new JsonObject(array.getString(0));
          response.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
              .end(new JsonObject().put("result", json.getString("longUrl")).toString());
        },
        e -> {
          // TODO
        }
    );
  }

  private void longUrlToShortUrl(RoutingContext routingContext) {
    String longUrl = routingContext.request().getParam("longUrl");
    if (longUrl == null) {
      // TODO
      return;
    }
    HttpServerResponse response = routingContext.response();
    long urlId = TwitterSnowflake.getNextLongId(machineId);
    if (urlId < 0) {
      // TODO
      return;
    }
    String shortUrl = Base58Conversion.encode(urlId);
    mySQLClient.rxGetConnection().subscribe(
        connection -> {
          connection.rxUpdateWithParams(
              "INSERT INTO Url(shortUrl, longUrl) VALUE (?, ?)", new JsonArray().add(shortUrl).add(longUrl))
              .subscribe(
                  updateResult -> {
                    if (updateResult.getKeys().size() != 1) {
                      // TODO
                    }
                    response.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                        .end(new JsonObject().put("result", shortUrl).toString());
                    JsonObject json = new JsonObject().put("shortUrl", shortUrl).put("longUrl", longUrl);
                    redisClient.rxHmset("url", new JsonObject().put(shortUrl, json.toString())).subscribe();
                  },
                  e -> {
                    // TODO
                  }
              );
        },
        e -> {
          // TODO
        }
    );
  }
}
