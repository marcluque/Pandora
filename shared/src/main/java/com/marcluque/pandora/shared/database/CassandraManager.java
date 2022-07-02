package com.marcluque.pandora.shared.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Collections;
import java.util.HashSet;

/**
 * Created by marcluque on 11.12.2016.
 */
public class CassandraManager {

    private final String host;

    private final String keySpace;

    private final String indexesTable;

    private Cluster cluster;

    private Session session;

    public CassandraManager(String host, String keySpace) {
        this.host = host;
        this.keySpace = keySpace;
        indexesTable = "indexes";
    }

    public void connect(DefaultRetryPolicy defaultRetryPolicy) {
        cluster = Cluster.builder()
                .addContactPoint(host)
                .withRetryPolicy(defaultRetryPolicy)
                .withSocketOptions(new SocketOptions().setReadTimeoutMillis(Integer.MAX_VALUE))
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .build();

        session = cluster.connect(keySpace);
        System.out.println("CONNECTED TO CASSANDRA ON: " + host);
    }

    public void insert(String keyword, String[] values) {
        if (contains(indexesTable, "keyword", keyword)) {
            PreparedStatement statement = session.prepare(
                    String.format("INSERT INTO %s (keyword, url_packages) VALUES (?, {('%s', '%s', '%s')});",
                            indexesTable, values[0], values[1], values[2]));
            session.execute(new BoundStatement(statement).bind(keyword));
        } else {
            update(indexesTable, keyword, session.getCluster().getMetadata().newTupleType(DataType.text(),
                    DataType.text(), DataType.text()).newValue((Object) values));
        }
    }

    public void insertCounterTable(String url) {
        session.execute(QueryBuilder.update("visited")
                .with(QueryBuilder.incr("pointing_score"))
                .where(QueryBuilder.eq("url", url)));
    }

    public boolean contains(String tableName, String column, Object keyword) {
        return session.execute(QueryBuilder.select()
                        .column(column)
                        .from(keySpace, tableName)
                        .limit(1)
                        .where(QueryBuilder.eq(column, keyword)))
                .isExhausted();
    }

    private void update(String tableName, String keyword, TupleValue tuple) {
        session.executeAsync(QueryBuilder.update(tableName)
                .with(QueryBuilder.addAll("url_packages", new HashSet<>(Collections.singletonList(tuple))))
                .where((QueryBuilder.eq("keyword", keyword)))
        );
    }

    public void disconnect() {
        cluster.close();
    }
}