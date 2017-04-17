package hello;

import static hello.Helper.getQueries;
import static hello.Helper.mongoGetInt;
import static hello.Helper.randomWorld;
import static hello.Helper.sendJson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Handles the updates test using MongoDB.
 */
final class UpdatesMongoHandler implements HttpHandler {
  private final MongoCollection<Document> worldCollection;

  UpdatesMongoHandler(MongoDatabase db) {
    worldCollection = db.getCollection("world");
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    int queries = getQueries(exchange);
    World[] worlds = new World[queries];
    for (int i = 0; i < worlds.length; i++) {
      Bson filter = Filters.eq(randomWorld());
      Document document = worldCollection.find(filter).first();
      int id = mongoGetInt(document, "_id");
      int randomNumber = mongoGetInt(document, "randomNumber");
      worlds[i] = new World(id, randomNumber);
    }
    List<WriteModel<Document>> writes = new ArrayList<>(worlds.length);
    for (World world : worlds) {
      world.randomNumber = randomWorld();
      Bson filter = Filters.eq(world.id);
      Bson update = Updates.set("randomNumber", world.randomNumber);
      writes.add(new UpdateOneModel<>(filter, update));
    }
    worldCollection.bulkWrite(writes);
    sendJson(exchange, worlds);
  }
}
