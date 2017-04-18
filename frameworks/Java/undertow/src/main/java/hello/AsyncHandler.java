package hello;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.SameThreadExecutor;
import java.util.Objects;

/**
 * An HTTP handler that <em>does not</em> complete the exchange when the call
 * stack of its {@link HttpHandler#handleRequest(HttpServerExchange)} returns.
 * The handler must ensure that every exchange is completed through other means.
 */
final class AsyncHandler implements HttpHandler {
  private final HttpHandler handler;

  AsyncHandler(HttpHandler handler) {
    this.handler = Objects.requireNonNull(handler);
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) {
    Runnable asyncTask =
        () -> {
          try {
            handler.handleRequest(exchange);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        };
    exchange.dispatch(SameThreadExecutor.INSTANCE, asyncTask);
  }
}
