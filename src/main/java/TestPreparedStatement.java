import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.OPTIONS;
import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.PREPARED_STATEMENT_CACHE_QUERIES;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

public class TestPreparedStatement {
    public static void main(String[] args) {
        final Map<String, String> options = new HashMap<>();
        options.put("lock_timeout", "10s");
        options.put("statement_timeout", "5min");

        final ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "postgresql")
            .option(HOST, "localhost")
            .option(PORT, 5432)  // optional, defaults to 5432
            .option(USER, "dbtest")
            .option(PASSWORD, "dbtest")
            .option(DATABASE, "dbtest")  // optional
            .option(OPTIONS, options) // optional
            .option(PREPARED_STATEMENT_CACHE_QUERIES, 0) // 0 means disabled (according with doc)
            .build());

        final Mono<Connection> mono = Mono.from(connectionFactory.create());

        mono.flatMapMany(connection -> connection
            .createStatement("INSERT INTO person (id, first_name, last_name) VALUES ($1, $2, $3)")
            .bind("$1", 9)
            .bind("$2", "Julia")
            .bind("$3", "Schrader")
            .execute())
        .flatMap(result -> result
            .map((row, rowMetadata) -> row.get("id")))
        .blockFirst();

        Mono.from(connectionFactory.create())
            .flatMapMany(connection -> connection
                .createStatement("SELECT first_name FROM PERSON WHERE first_name = $1")
                .bind("$1", "walter")
                .execute())
            .flatMap(result -> result
                .map((row, rowMetadata) -> row.get("first_name", String.class)))
            .doOnNext(System.out::println)
            .blockFirst();
    }
}
