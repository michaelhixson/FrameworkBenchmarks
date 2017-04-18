package hello;

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
import org.bson.Document;

/**
 * Handles the single-query database test using MongoDB asynchronously.
 */
final class DbMongoAsyncHandler implements HttpHandler {
  private final MongoCollection<Document> worldCollection;

  DbMongoAsyncHandler(MongoDatabase db) {
    worldCollection = db.getCollection("world");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
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
            sendException(exchange, exception);
          } else {
            sendJson(exchange, world);
          }
        };
    exchange.dispatch(
        SameThreadExecutor.INSTANCE,
        () -> findRandomWorld.first(onFindFirst));
  }
}
