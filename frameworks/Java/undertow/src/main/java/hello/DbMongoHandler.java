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
    int id = Helper.mongoGetInt(document, "_id");
    int randomNumber = Helper.mongoGetInt(document, "randomNumber");
    World world = new World(id, randomNumber);
    Helper.sendJson(exchange, world);
  }
}
