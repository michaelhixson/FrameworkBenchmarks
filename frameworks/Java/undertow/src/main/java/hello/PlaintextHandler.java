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
  @Override
  public void handleRequest(HttpServerExchange exchange) {
    exchange.getResponseHeaders().put(CONTENT_TYPE, "text/plain");
    exchange.getResponseSender().send(buffer.duplicate());
  }

  // We get a very small performance boost from reusing a byte buffer across
  // requests instead of using the string "Hello, World!" directly (which
  // Undertow would, internally, dump into a newly-allocated ByteBuffer on each
  // request).  The plaintext test requirements explicitly permit this
  // optimization (the intent of this test type is to exercise request-routing
  // fundamentals only), so that's why this code is written this way.

  private static final ByteBuffer buffer;
  static {
    String message = "Hello, World!";
    byte[] messageBytes = message.getBytes(US_ASCII);
    buffer = ByteBuffer.allocateDirect(messageBytes.length);
    buffer.put(messageBytes);
    buffer.flip();
  }
}
