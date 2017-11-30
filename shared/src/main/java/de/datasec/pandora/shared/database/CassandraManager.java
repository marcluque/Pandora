package de.datasec.pandora.shared.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Set;

/**
 * Created by DataSec on 11.12.2016.
 */
public class CassandraManager {

    private String host;

    private String keySpace;

    private Cluster cluster;

    private Session session;

    public CassandraManager(String host, String keySpace) {
        this.host = host;
        this.keySpace = keySpace;
    }

    public void connect(DefaultRetryPolicy defaultRetryPolicy) {
        cluster = Cluster.builder()
                .addContactPoint(host)
                .withRetryPolicy(defaultRetryPolicy)
                .withSocketOptions(new SocketOptions().setReadTimeoutMillis(600000))
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .build();

        session = cluster.connect(keySpace);
        System.out.println("CONNECTED TO CASSANDRA ON: " + host);
    }

    public void insert(String tableName, String column, String[] columns, Object[] values) {
        String[] insertValues = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            insertValues[i] = "?";
        }

        if (!contains(tableName, column, values[0])) {
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

    public boolean contains(String tableName, String column, Object key) {
        return !(session.execute(QueryBuilder.select()
                .column(column)
                .from(keySpace, tableName)
                .limit(1)
                .where(QueryBuilder.eq(column, key)))
                .isExhausted());
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