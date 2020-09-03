package hello.handler;

import java.io.IOException;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.ThreadPool;


/**
 * An implementation of the TechEmpower benchmark tests using the Jetty web
 * server.  
 */
public final class HelloWebServer 
{
    public static void main(String[] args) throws Exception
    {
        Server server;
        if (USE_VIRTUAL_THREADS) {
          System.out.println("Creating virtual thread (Loom) server");
          server = createLoomServer();
        } else {
          System.out.println("Creating default (non-Loom) server");
          server = createDefaultServer();
        }

        if (ADD_RESPONSE_DELAY) {
          System.out.println("Adding artificial delay to all responses");
        } else {
          System.out.println("Not adding artificial delay to responses");
        }

        ServerConnector connector = server.getBean(ServerConnector.class);
        HttpConfiguration config = connector.getBean(HttpConnectionFactory.class).getHttpConfiguration();
        config.setSendDateHeader(true);
        config.setSendServerVersion(true);

        PathHandler pathHandler = new PathHandler();
        server.setHandler(pathHandler);

        server.start();
        server.join();
    }

    private static final boolean USE_VIRTUAL_THREADS = "true".equals(System.getenv("USE_VIRTUAL_THREADS"));
    public static final boolean ADD_RESPONSE_DELAY = "true".equals(System.getenv("ADD_RESPONSE_DELAY"));

    public static void delayResponse() {
      if (ADD_RESPONSE_DELAY) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }

    private static Server createDefaultServer()
    {
        return new Server(8080);
    }

    private static Server createLoomServer()
    {
        ThreadPool threadPool =
            new ThreadPool() {
              // Invoke new Executors.newVirtualThreadExecutor() method using
              // reflection so as to still compile on non-loom JDK.
              final ExecutorService executorService;
              {
                try {
                  Method method = Executors.class.getMethod("newVirtualThreadExecutor");
                  executorService = (ExecutorService) method.invoke(null);
                } catch (Exception e) {
                  throw new Error(e);
                }
              }

                @Override
                public void execute(Runnable command) {
                    executorService.execute(command);
                }

                @Override
                public void join() throws InterruptedException {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                }

                @Override
                public int getThreads() {
                    return 1;
                }

                @Override
                public int getIdleThreads() {
                    return 1;
                }

                @Override
                public boolean isLowOnThreads() {
                    return false;
                }
            };

        Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});

        return server;
    }
    
    public static class PathHandler extends AbstractHandler
    {
        JsonHandler _jsonHandler=new JsonHandler();
        PlainTextHandler _plainHandler=new PlainTextHandler();
        
        public PathHandler()
        {
            addBean(_jsonHandler);
            addBean(_plainHandler);
        }

        @Override
        public void setServer(Server server)
        {
            super.setServer(server);
            _jsonHandler.setServer(server);
            _plainHandler.setServer(server);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            if ("/plaintext".equals(target))
                _plainHandler.handle(target,baseRequest,request,response);
            else if ("/json".equals(target))
                _jsonHandler.handle(target,baseRequest,request,response);
        }
        
    }
}
