package hello;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Handles the multiple-query database test using MongoDB.
 */
final class QueriesMongoHandler implements HttpHandler {
  private final MongoCollection<Document> worldCollection;

  QueriesMongoHandler(MongoDatabase db) {
    worldCollection = db.getCollection("world");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    int queries = Helper.getQueries(exchange);
    World[] worlds = new World[queries];
    for (int i = 0; i < worlds.length; i++) {
      Bson filter = Filters.eq(Helper.randomWorld());
      Document document = worldCollection.find(filter).first();
      int id = Helper.mongoGetInt(document, "_id");
      int randomNumber = Helper.mongoGetInt(document, "randomNumber");
      worlds[i] = new World(id, randomNumber);
    }
    Helper.sendJson(exchange, worlds);
  }
}
