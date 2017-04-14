package hello;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Handles the single-query database test using MongoDB.
 */
final class DbMongoHandler implements HttpHandler {
  private final MongoCollection<Document> worldCollection;

  DbMongoHandler(MongoDatabase db) {
    worldCollection = db.getCollection("world");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Bson filter = Filters.eq(Helper.randomWorld());
    Document document = worldCollection.find(filter).first();
    // The creation script for the Mongo database inserts these values as
    // JavaScript numbers, which resolve to Doubles in Java.  However, we might
    // later replace them with Integers.  To make this code compatible with
    // both, we cast the values to Number.
    int id = ((Number) document.get("_id")).intValue();
    int randomNumber = ((Number) document.get("randomNumber")).intValue();
    World world = new World(id, randomNumber);
    Helper.sendJson(exchange, world);
  }
}
