package de.datasec.pandora.slave;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import de.datasec.pandora.shared.PandoraProtocol;
import de.datasec.pandora.shared.database.CassandraManager;
import de.datasec.pandora.slave.config.SlaveClientConfig;
import de.datasec.pandora.slave.listener.SlavePacketListener;
import de.jackwhite20.cascade.client.Client;
import de.jackwhite20.cascade.client.ClientFactory;
import de.jackwhite20.cascade.shared.pipeline.PipelineUtils;
import de.jackwhite20.cascade.shared.protocol.listener.PacketListener;
import de.jackwhite20.cascade.shared.session.Session;
import de.jackwhite20.cascade.shared.session.SessionListener;

import java.lang.reflect.Field;

/**
 * Created by DataSec on 27.11.2016.
 */
public class Slave implements PacketListener {

    private static CassandraManager cassandraManager;

    private Client client;

    public Slave() {
        try {
            //TODO: Find the reason why epoll is not working with the slave
            Field epoll = PipelineUtils.class.getDeclaredField("epoll");
            epoll.setAccessible(true);
            epoll.set(PipelineUtils.class, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cassandraManager = new CassandraManager("188.68.54.85", "pandora");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void connect() {
        client = ClientFactory.create(new SlaveClientConfig(new PandoraProtocol(new SlavePacketListener(3))));
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
