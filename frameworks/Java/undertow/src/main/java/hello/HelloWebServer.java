package hello;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.SetHeaderHandler;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * Provides the {@link #main(String[])} method, which launches the application.
 */
public final class HelloWebServer {
  private HelloWebServer() {
    throw new AssertionError();
  }

  public static void main(String[] args) throws Exception {
    Mode mode = Mode.valueOf(args[0]);
    Properties props = new Properties();
    try (InputStream in =
             Thread.currentThread()
                   .getContextClassLoader()
                   .getResourceAsStream("hello/server.properties")) {
      props.load(in);
    }
    int port = Integer.parseInt(props.getProperty("undertow.port"));
    String host = props.getProperty("undertow.host");
    HttpHandler paths = mode.paths(props);
    HttpHandler rootHandler = new SetHeaderHandler(paths, "Server", "U-tow");
    Undertow.builder()
            .addHttpListener(port, host)
            // In HTTP/1.1, connections are persistent unless declared
            // otherwise.  Adding a "Connection: keep-alive" header to every
            // response would only add useless bytes.
            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
            .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
            .setHandler(rootHandler)
            .build()
            .start();
  }

  enum Mode {
    /**
     * The server will only implement the test types that do not require a
     * database.
     */
    NO_DATABASE() {
      @Override
      HttpHandler paths(Properties props) {
        return new PathHandler()
            .addExactPath("/plaintext", new PlaintextHandler())
            .addExactPath("/json",      new JsonHandler());
      }
    },

    /**
     * The server will use a MySQL database and will only implement the test
     * types that require a database.
     */
    MYSQL() {
      @Override
      HttpHandler paths(Properties props) {
        DataSource db =
            newSqlDataSource(
                props.getProperty("mysql.jdbcUrl"),
                props.getProperty("mysql.username"),
                props.getProperty("mysql.password"),
                Integer.parseInt(props.getProperty("mysql.connections")));
        return new PathHandler()
            .addExactPath("/db",       new BlockingHandler(new DbSqlHandler(db)))
            .addExactPath("/queries",  new BlockingHandler(new QueriesSqlHandler(db)))
            .addExactPath("/fortunes", new BlockingHandler(new FortunesSqlHandler(db)))
            .addExactPath("/updates",  new BlockingHandler(new UpdatesSqlHandler(db)));
      }
    },

    /**
     * The server will use a PostgreSQL database and will only implement the
     * test types that require a database.
     */
    POSTGRESQL() {
      @Override
      HttpHandler paths(Properties props) {
        DataSource db =
            newSqlDataSource(
                props.getProperty("postgresql.jdbcUrl"),
                props.getProperty("postgresql.username"),
                props.getProperty("postgresql.password"),
                Integer.parseInt(props.getProperty("postgresql.connections")));
        return new PathHandler()
            .addExactPath("/db",       new BlockingHandler(new DbSqlHandler(db)))
            .addExactPath("/queries",  new BlockingHandler(new QueriesSqlHandler(db)))
            .addExactPath("/fortunes", new BlockingHandler(new FortunesSqlHandler(db)))
            .addExactPath("/updates",  new BlockingHandler(new UpdatesSqlHandler(db)));
      }
    },

    /**
     * The server will use a MongoDB database and will only implement the test
     * types that require a database.
     */
    MONGODB() {
      @Override
      HttpHandler paths(Properties props) {
        MongoDatabase db =
            newMongoDatabase(
                props.getProperty("mongodb.host"),
                props.getProperty("mongodb.databaseName"),
                Integer.parseInt(props.getProperty("mongodb.connections")));
        return new PathHandler()
            .addExactPath("/db",       new BlockingHandler(new DbMongoHandler(db)))
            .addExactPath("/queries",  new BlockingHandler(new QueriesMongoHandler(db)))
            .addExactPath("/fortunes", new BlockingHandler(new FortunesMongoHandler(db)))
            .addExactPath("/updates",  new BlockingHandler(new UpdatesMongoHandler(db)));
      }
    };

    /**
     * Returns an HTTP handler that provides routing for all the
     * test-type-specific endpoints of the server.
     *
     * @param props the server configuration
     */
    abstract HttpHandler paths(Properties props);

    /**
     * Provides a source of connections to a SQL database.
     */
    static DataSource newSqlDataSource(String jdbcUrl,
                                       String username,
                                       String password,
                                       int connections) {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(jdbcUrl);
      config.setUsername(username);
      config.setPassword(password);
      config.setMinimumIdle(connections);
      config.setMaximumPoolSize(connections);
      return new HikariDataSource(config);
    }

    /**
     * Provides a source of connections to a MongoDB database.
     */
    static MongoDatabase newMongoDatabase(String host,
                                          String databaseName,
                                          int connections) {
      MongoClientOptions.Builder options = MongoClientOptions.builder();
      options.minConnectionsPerHost(connections);
      options.connectionsPerHost(connections);
      MongoClient client = new MongoClient(host, options.build());
      return client.getDatabase(databaseName);
    }
  }
}
