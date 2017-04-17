package hello;

import static hello.Helper.mongoGetInt;
import static hello.Helper.sendHtml;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 * Handles the fortunes test using MongoDB.
 */
final class FortunesMongoHandler implements HttpHandler {
  private final MongoCollection<Document> fortuneCollection;

  FortunesMongoHandler(MongoDatabase db) {
    fortuneCollection = db.getCollection("fortune");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    List<Fortune> fortunes = new ArrayList<>();
    for (Document document : fortuneCollection.find()) {
      int id = mongoGetInt(document, "_id");
      String message = document.getString("message");
      fortunes.add(new Fortune(id, message));
    }
    fortunes.add(new Fortune(0, "Additional fortune added at request time."));
    fortunes.sort(null);
    sendHtml(exchange, fortunes, "hello/fortunes.mustache");
  }
}
