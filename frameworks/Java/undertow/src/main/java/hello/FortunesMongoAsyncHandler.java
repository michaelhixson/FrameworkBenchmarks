package hello;

import static hello.Helper.mongoGetInt;
import static hello.Helper.sendException;
import static hello.Helper.sendHtml;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.async.client.MongoIterable;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.SameThreadExecutor;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Handles the fortunes test using MongoDB asynchronously.
 */
final class FortunesMongoAsyncHandler implements HttpHandler {
  private final MongoCollection<Document> fortuneCollection;

  FortunesMongoAsyncHandler(MongoDatabase db) {
    fortuneCollection = db.getCollection("fortune");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
    MongoIterable<Fortune> query =
        fortuneCollection
            .find()
            .map(document -> {
              int id = mongoGetInt(document, "_id");
              String message = document.getString("message");
              return new Fortune(id, message);
            });
    SingleResultCallback<List<Fortune>> onQueryComplete =
        (fortunes, exception) -> {
          if (exception != null) {
            sendException(exchange, exception);
          } else {
            fortunes.add(new Fortune(0, "Additional fortune added at request time."));
            fortunes.sort(null);
            sendHtml(exchange, fortunes, "hello/fortunes.mustache");
          }
        };
    exchange.dispatch(
        SameThreadExecutor.INSTANCE,
        () -> query.into(new ArrayList<>(), onQueryComplete));
  }
}
