package de.datasec.pandora.slave;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import de.datasec.hydra.client.Client;
import de.datasec.pandora.shared.PandoraProtocol;
import de.datasec.pandora.shared.database.CassandraManager;
import de.datasec.pandora.slave.listener.SlavePacketListener;

import java.net.StandardSocketOptions;

/**
 * Created by DataSec on 27.11.2016.
 */
public class Slave {

    private static CassandraManager cassandraManager;

    public Slave() {
        cassandraManager = new CassandraManager("188.68.54.85", "pandora");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void connect() {
        new Client.Builder("188.68.54.85", 8888, new PandoraProtocol(new SlavePacketListener(3)))
                .workerThreads(2)
                .option(StandardSocketOptions.TCP_NODELAY, true)
                .option(StandardSocketOptions.SO_KEEPALIVE, true)
                .build();
    }

    public static CassandraManager getCassandraManager() {
        return cassandraManager;
    }
}
