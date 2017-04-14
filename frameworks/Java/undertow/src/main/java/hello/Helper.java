package hello;

import static io.undertow.util.Headers.CONTENT_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.undertow.server.HttpServerExchange;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provides utility methods for the benchmark tests.
 */
final class Helper {
  private Helper() {
    throw new AssertionError();
  }

  /**
   * Returns the value of the "queries" request parameter, which is an integer
   * bound between 1 and 500 with a default value of 1.
   *
   * @param exchange the current HTTP exchange
   * @return the value of the "queries" request parameter
   */
  static int getQueries(HttpServerExchange exchange) {
    Deque<String> values = exchange.getQueryParameters().get("queries");
    if (values == null) {
      return 1;
    }
    String textValue = values.peekFirst();
    if (textValue == null) {
      return 1;
    }
    int parsedValue;
    try {
      parsedValue = Integer.parseInt(textValue);
    } catch (NumberFormatException e) {
      return 1;
    }
    return Math.min(500, Math.max(1, parsedValue));
  }

  /**
   * Returns a random integer that is a suitable value for both the {@code id}
   * and {@code randomNumber} properties of a world object.
   *
   * @return a random world number
   */
  static int randomWorld() {
    return 1 + ThreadLocalRandom.current().nextInt(10000);
  }

  /**
   * Ends the HTTP exchange by encoding the given value as JSON and writing
   * that JSON to the response.
   *
   * @param exchange the current HTTP exchange
   * @param value the value to be encoded as JSON
   * @throws JsonProcessingException if the value cannot be encoded as JSON
   */
  static void sendJson(HttpServerExchange exchange, Object value)
      throws JsonProcessingException {
    byte[] json = objectMapper.writeValueAsBytes(value);
    ByteBuffer buffer = ByteBuffer.wrap(json);
    exchange.getResponseHeaders().put(CONTENT_TYPE, "application/json");
    exchange.getResponseSender().send(buffer);
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Ends the HTTP exchange by supplying the given value to a Mustache template
   * and writing the HTML output of the template to the response.
   *
   * @param exchange the current HTTP exchange
   * @param value the value to be supplied to the Mustache template
   * @param template the path to the Mustache template
   */
  static void sendHtml(HttpServerExchange exchange,
                       Object value,
                       String template) {
    Mustache mustache = mustacheFactory.compile(template);
    StringWriter writer = new StringWriter();
    mustache.execute(writer, value);
    String html = writer.toString();
    exchange.getResponseHeaders().put(CONTENT_TYPE, "text/html;charset=utf-8");
    exchange.getResponseSender().send(html);
  }

  private static final MustacheFactory mustacheFactory =
      new DefaultMustacheFactory();
}
