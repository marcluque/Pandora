package de.datasec.pandora.shared.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by DataSec on 11.12.2016.
 */
public class CassandraManager {

    private String host;

    private String keyspace;

    private Cluster cluster;

    private Session session;

    public CassandraManager(String host, String keyspace) {
        this.host = host;
        this.keyspace = keyspace;
    }

    public void connect(DefaultRetryPolicy defaultRetryPolicy) {
        cluster = Cluster.builder()
                .addContactPoint(host)
                .withRetryPolicy(defaultRetryPolicy)
                .withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy()))
                .build();
        session = cluster.connect(keyspace);
        System.out.println("CONNECTED TO CASSANDRA ON: " + host);
    }

    public void insert(String tableName, String columnForWhere, String columnForWhereMatch, String[] columns, Object[] values) {
        String[] insertValues = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            insertValues[i] = "?";
        }

        if (!contains(tableName, columnForWhere, columnForWhereMatch, values[0])) {
            PreparedStatement statement = session.prepare(
                    String.format("INSERT INTO %s %s VALUES %s;", tableName, createString(columns), createString(insertValues)));

            BoundStatement boundStatement = new BoundStatement(statement);

            session.executeAsync(boundStatement.bind((Object[]) values));
        } else {
            if (tableName.equalsIgnoreCase("indexes")) {
                update(tableName, values[0].toString(), (Set<String>) values[1]);
            }
        }
    }

    public boolean contains(String tableName, String column, String columnForMatch, Object key) {
        ResultSet result = null;
        try {
            result = session.executeAsync(QueryBuilder.select()
                    .column(column)
                    .from(keyspace, tableName)
                    .where(QueryBuilder.eq(columnForMatch, key))).get();
        } catch (InterruptedException | ExecutionException | NoHostAvailableException ignore) {}

        return result != null && result.one() != null;
    }

    private void update(String tableName, String keyword, Set<String> urls) {
        session.executeAsync(QueryBuilder.update(tableName)
                .with(QueryBuilder.addAll("urls", urls))
                .where((QueryBuilder.eq("keyword", keyword)))
        );
    }

    public void disconnect() {
        cluster.close();
    }

    private String createString(String[] strings) {
        String result = "(";

        for (int i = 0; i < strings.length; i++) {
            result = String.format("%s%s", result, strings[i]);

            if(i < strings.length - 1) {
                result = String.format("%s,", result);
            }
        }

        return String.format("%s)", result);
    }
}
