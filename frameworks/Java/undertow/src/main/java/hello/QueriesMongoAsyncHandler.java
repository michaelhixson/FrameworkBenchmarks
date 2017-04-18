package hello;

import static hello.Helper.allComplete;
import static hello.Helper.getQueries;
import static hello.Helper.randomWorld;
import static hello.Helper.sendException;
import static hello.Helper.sendJson;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
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
    IntStream
        .range(0, getQueries(exchange))
        .mapToObj(i -> {
          CompletableFuture<World> future = new CompletableFuture<>();
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
          return future;
        })
        .collect(allComplete())
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
