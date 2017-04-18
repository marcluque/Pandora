package de.datasec.pandora.slave.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Set;

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

    public void insert(String tableName, String[] columns, Object[] values) {
        String[] insertValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            insertValues[i] = "?";
        }

        if (!contains(tableName, values[1])) {
            PreparedStatement statement = session.prepare(
                    String.format("INSERT INTO %s %s VALUES %s;", tableName, createString(columns), createString(insertValues)));

            BoundStatement boundStatement = new BoundStatement(statement);

            session.execute(boundStatement.bind((Object[]) values));
        } else {
            update("indexes", values[1].toString(), (Set<String>) values[2]);
        }
    }

    public void disconnect() {
        cluster.close();
    }

    private void update(String tableName, String keyword, Set<String> urls) {
        Statement update = QueryBuilder.update(tableName)
                .with(QueryBuilder.addAll("urls", urls))
                .where((QueryBuilder.eq("keyword", keyword)));

        session.execute(update);
    }

    private String createString(String[] strings) {

        String result = "(";

        for (int i = 0; i < strings.length; i++) {
            result = String.format("%s%s", result, strings[i]);

            if(i < strings.length - 1) {
                result = String.format("%s,", result);
            }
        }

        result = String.format("%s)", result);

        return result;
    }

    private boolean contains(String tableName, Object key) {
        Statement select = QueryBuilder.select()
                .column("keyword")
                .from("indexes", tableName)
                .where(QueryBuilder.eq("keyword", key));

        return !(session.execute(select).isExhausted());
    }
}
