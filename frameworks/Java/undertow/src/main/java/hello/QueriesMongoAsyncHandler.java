package hello;

import static hello.Helper.getQueries;
import static hello.Helper.mongoGetInt;
import static hello.Helper.randomWorld;
import static hello.Helper.sendException;
import static hello.Helper.sendJson;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.async.client.MongoIterable;
import com.mongodb.client.model.Filters;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.SameThreadExecutor;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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
      MongoIterable<World> findRandomWorld =
          worldCollection
              .find(Filters.eq(randomWorld()))
              .map(document -> {
                int id = mongoGetInt(document, "_id");
                int randomNumber = mongoGetInt(document, "randomNumber");
                return new World(id, randomNumber);
              });
      SingleResultCallback<World> onFindFirst =
          (world, exception) -> {
            if (exception != null) {
              future.completeExceptionally(exception);
            } else {
              future.complete(world);
            }
          };
      findRandomWorld.first(onFindFirst);
    }
    CompletableFuture<World[]> allFutureWorlds =
        CompletableFuture.allOf(futureWorlds).thenApply(
            nothing -> {
              World[] worlds = new World[futureWorlds.length];
              Arrays.setAll(worlds, i -> futureWorlds[i].join());
              return worlds;
            });
    BiConsumer<World[], Throwable> onFuturesComplete =
        (worlds, exception) -> {
          if (exception != null) {
            sendException(exchange, exception);
          } else {
            sendJson(exchange, worlds);
          }
        };
    exchange.dispatch(
        SameThreadExecutor.INSTANCE,
        () -> allFutureWorlds.whenComplete(onFuturesComplete));
  }
}
