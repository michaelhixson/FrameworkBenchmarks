package hello.servlet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.ajax.JSON;


public class ExternalRequestServlet extends GenericServlet
{
    private JSON json = new JSON();

    private final HttpClient client = HttpClient.newHttpClient();
    private final HttpRequest clientRequest = HttpRequest.newBuilder(URI.create("http://host.docker.internal:9000/")).build();

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        var async = req.startAsync();
        client.sendAsync(clientRequest, HttpResponse.BodyHandlers.ofString()).thenAccept(clientResponse -> {
            async.start(() -> {
                HttpServletResponse response= (HttpServletResponse)res;
                response.setContentType("application/json");
                try {
                    response.getWriter().print(clientResponse.body());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                async.complete();

            });
        });
    }

}
