package hello.handler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ajax.JSON;


public class ExternalRequestHandler extends AbstractHandler
{
    private JSON json = new JSON();
    HttpField contentType = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE,"application/json");

    private final HttpClient client = HttpClient.newHttpClient();
    private final HttpRequest clientRequest = HttpRequest.newBuilder(URI.create("http://host.docker.internal:9000/")).build();

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        HttpResponse<String> clientResponse;
        try {
            clientResponse = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        baseRequest.setHandled(true);
        baseRequest.getResponse().getHttpFields().add(contentType);
        baseRequest.getResponse().getHttpOutput().print(clientResponse.body());
    }

}
