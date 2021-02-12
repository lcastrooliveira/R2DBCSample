import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnectOptions;

public class TestTransaction extends AbstractVerticle {
    public static void main(String[] args) {
        PgConnectOptions options = new PgConnectOptions()
            .setPort(6432)
            .setHost("localhost")
            .setDatabase("dbtest")
            .setUser("dbtest")
            .setPassword("dbtest")
            .setCachePreparedStatements(false);


        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new TestTransaction(options));
    }

    private final SqlConnectOptions options;

    public TestTransaction(SqlConnectOptions options) {
        this.options = options;
    }

    @Override
    public void start() {
        Pool pool = Pool.pool(vertx, options, new PoolOptions().setMaxSize(4));

        pool.withTransaction(sqlClient -> {
            // create a test table
            return sqlClient.query("create table test1(id int primary key, name varchar(255))").execute()
                .compose(v -> {
                    // insert some test data
                    return sqlClient.query("insert into test1 values (1, 'Hello'), (2, 'World')").execute();
                })
                .compose(v -> {
                    // query some data
                    return sqlClient.query("select * from test1").execute();
                });
        }) .onSuccess(rows -> {
            for (Row row : rows) {
                System.out.println("row = " + row.toJson());
            }
        }).onFailure(Throwable::printStackTrace);;
    }
}
