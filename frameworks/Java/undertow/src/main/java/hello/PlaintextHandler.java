package hello;

import static io.undertow.util.Headers.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.US_ASCII;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.nio.ByteBuffer;

/**
 * Handles the plaintext test.
 */
final class PlaintextHandler implements HttpHandler {
  private static final ByteBuffer buffer;
  static {
    String message = "Hello, World!";
    buffer = ByteBuffer.allocateDirect(message.length());
    buffer.put(message.getBytes(US_ASCII));
    buffer.flip();
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    exchange.getResponseHeaders().put(CONTENT_TYPE, "text/plain");
    exchange.getResponseSender().send(buffer.duplicate());
  }
}
