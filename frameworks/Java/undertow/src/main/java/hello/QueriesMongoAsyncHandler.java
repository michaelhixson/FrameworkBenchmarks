package hello;

import static hello.Helper.getQueries;
import static hello.Helper.randomWorld;
import static hello.Helper.sendException;
import static hello.Helper.sendJson;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.bson.Document;

/**
 * Handles the multiple-query database test using MongoDB asynchronously.
 */
final class QueriesMongoAsyncHandler implements HttpHandler {
  private final MongoCollection<Document> worldCollection;

  QueriesMongoAsyncHandler(MongoDatabase db) {
    worldCollection = db.getCollection("world");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
    int queries = getQueries(exchange);
    @SuppressWarnings("unchecked")
    CompletableFuture<World>[] futureWorlds = new CompletableFuture[queries];
    Arrays.setAll(futureWorlds, i -> new CompletableFuture<>());
    for (CompletableFuture<World> future : futureWorlds) {
      worldCollection
          .find(Filters.eq(randomWorld()))
          .map(Helper::mongoDocumentToWorld)
          .first(
              (world, exception) -> {
                if (exception != null) {
                  future.completeExceptionally(exception);
                } else {
                  future.complete(world);
                }
              });
    }
    CompletableFuture
        .allOf(futureWorlds)
        .thenApply(
            nothing -> {
              World[] worlds = new World[futureWorlds.length];
              Arrays.setAll(worlds, i -> futureWorlds[i].join());
              return worlds;
            })
        .whenComplete(
            (worlds, exception) -> {
              if (exception != null) {
                sendException(exchange, exception);
              } else {
                sendJson(exchange, worlds);
              }
            });
  }
}
