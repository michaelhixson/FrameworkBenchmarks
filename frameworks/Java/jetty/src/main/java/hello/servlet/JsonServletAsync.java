package hello.servlet;

import hello.handler.HelloWebServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.util.ajax.JSON;


public class JsonServletAsync extends GenericServlet
{
    private JSON json = new JSON();
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        var async = req.startAsync();
        async.start(() -> {
            HelloWebServer.delayResponse();
            HttpServletResponse response= (HttpServletResponse)res;
            response.setContentType("application/json");
            Map<String,String> map = Collections.singletonMap("message","Hello, World!");

            try {
                json.append(response.getWriter(), map);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            async.complete();

        });
    }

}
