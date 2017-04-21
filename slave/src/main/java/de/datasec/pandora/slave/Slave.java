package de.datasec.pandora.slave;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import de.datasec.pandora.shared.PandoraProtocol;
import de.datasec.pandora.slave.database.CassandraManager;
import de.datasec.pandora.slave.listener.SlavePacketListener;
import de.jackwhite20.cascade.client.Client;
import de.jackwhite20.cascade.client.ClientFactory;
import de.jackwhite20.cascade.shared.Options;
import de.jackwhite20.cascade.shared.protocol.listener.PacketListener;
import de.jackwhite20.cascade.shared.session.Session;
import de.jackwhite20.cascade.shared.session.SessionListener;

import java.net.StandardSocketOptions;

/**
 * Created by DataSec on 27.11.2016.
 */
public class Slave implements PacketListener {

    private static CassandraManager cassandraManager;

    private String host;

    private int port;

    private Client client;

    public Slave(String host, int port) {
        this.host = host;
        this.port = port;
        cassandraManager = new CassandraManager("127.0.0.1", "indexes");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void connect() {
        client = ClientFactory.create(host, port, new PandoraProtocol(new SlavePacketListener(2)), Options.of(StandardSocketOptions.TCP_NODELAY, true));
        client.addSessionListener(new SessionListener() {

            @Override
            public void onConnected(Session session) {
                System.out.println("Connected to Pandora master server!");
            }

            @Override
            public void onDisconnected(Session session) {
                System.out.println("Disconnected from Pandora master server!");
                cassandraManager.disconnect();
                client.disconnect();
            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onStopped() {

            }
        });

        client.connect();
    }

    public static CassandraManager getCassandraManager() {
        return cassandraManager;
    }
}
