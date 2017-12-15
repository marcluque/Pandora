package de.datasec.pandora.shared.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Collections;
import java.util.HashSet;

/**
 * Created by DataSec on 11.12.2016.
 */
public class CassandraManager {

    private String indexesTable;

    private String host;

    private String keySpace;

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

    public void insert(Object[] values) {
        TupleValue tupleValue = cluster.getMetadata().newTupleType(DataType.text(), DataType.text(), DataType.text()).newValue(values[1]);

        if (!contains(indexesTable, "keyword", values[0])) {
            //String[] tupleValues = ((String[]) (values[1]));
            //String[] tuple = {tupleValues[0], tupleValues[1], tupleValues[2]};

            PreparedStatement statement = session.prepare(String.format("INSERT INTO %s (keyword, urls) VALUES (?, {:tuple});", indexesTable));
            BoundStatement boundStatement = new BoundStatement(statement).bind(values[1]);
            boundStatement.setTupleValue(":tuple", tupleValue);
            session.executeAsync(boundStatement);
        } else {
            update(indexesTable, values[0].toString(), tupleValue);
        }
    }

    public void insertCounterTable(String url) {
        session.executeAsync(QueryBuilder.update("visited")
                .with(QueryBuilder.incr("pointing_score"))
                .where(QueryBuilder.eq("url", url)));
    }

    public boolean contains(String tableName, String column, Object keyword) {
        return !(session.execute(QueryBuilder.select()
                .column(column)
                .from(keySpace, tableName)
                .limit(1)
                .where(QueryBuilder.eq(column, keyword)))
                .isExhausted());
    }

    private void update(String tableName, String keyword, TupleValue tuple) {
        session.executeAsync(QueryBuilder.update(tableName)
                .with(QueryBuilder.addAll("urls", new HashSet<>(Collections.singletonList(tuple))))
                .where((QueryBuilder.eq("keyword", keyword)))
        );
    }

    public void disconnect() {
        cluster.close();
    }
}