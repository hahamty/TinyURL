package com.hahamty.tinyurl.gateway;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.ext.asyncsql.MySQLClient;
import io.vertx.rxjava.ext.sql.SQLClient;

public final class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
    SQLClient sqlClient = MySQLClient.createShared(
        vertx, new JsonObject().put("username", "root").put("password", "123").put("database", "tinyurl"));
    EventBus eventBus = vertx.eventBus();

    eventBus.<JsonObject>consumer("get-machine-id", message -> {
      byte[] macAddress = message.body().getBinary("mac-address");
      sqlClient.rxGetConnection().subscribe(
          connection -> connection.rxQueryWithParams(
              "SELECT id FROM Machine WHERE mac=?", new JsonArray().add(macAddress)).subscribe(
              queryResult -> {
                if (queryResult.getNumRows() == 0) {
                  connection.rxUpdateWithParams(
                      "INSERT INTO Machine(mac) VALUE (?)", new JsonArray().add(macAddress)).subscribe(
                      insertResult -> {
                        System.out.println(insertResult.getKeys());
                        int id = insertResult.getKeys().getInteger(0);
                        message.reply(new JsonObject().put("machine-id", id));
                      },
                      e -> {
                        // TODO
                      }
                  );
                  return;
                }
                int id = queryResult.getResults().get(0).getInteger(0);
                message.reply(new JsonObject().put("machine-id", id));
              },
              e -> {
                // TODO
              }
          ),
          e -> {
            // TODO
          }
      );
    });
  }
}
