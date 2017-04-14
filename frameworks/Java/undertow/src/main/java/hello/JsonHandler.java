package hello;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Collections;

/**
 * Handles the JSON test.
 */
final class JsonHandler implements HttpHandler {
  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Helper.sendJson(
        exchange,
        Collections.singletonMap("message", "Hello, World!"));
  }
}
