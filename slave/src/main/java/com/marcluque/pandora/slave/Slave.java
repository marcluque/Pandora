package com.marcluque.pandora.slave;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.marcluque.pandora.slave.listener.SlavePacketListener;
import com.marcluque.hydra.client.Client;
import com.marcluque.hydra.shared.handler.Session;
import com.marcluque.hydra.shared.handler.listener.HydraSessionListener;
import com.marcluque.pandora.shared.PandoraProtocol;
import com.marcluque.pandora.shared.database.CassandraManager;
import io.netty.channel.ChannelOption;

/**
 * Created by marcluque on 27.11.2016.
 */
public class Slave {

    private static CassandraManager cassandraManager;

    public Slave() {
        cassandraManager = new CassandraManager("188.68.54.85", "pandora");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void connect() {
        new Client.Builder("localhost", 8888, new PandoraProtocol(new SlavePacketListener(3)))
                .workerThreads(2)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .useEpoll(false)
                .addSessionListener(new HydraSessionListener() {
                    @Override
                    public void onConnected(Session session) {
                        System.out.println("Connected to Pandora master server!");
                    }

                    @Override
                    public void onDisconnected(Session session) {
                        System.out.println("Disconnected from Pandora master server!");
                        cassandraManager.disconnect();
                    }
                })
                .build();
    }

    public static CassandraManager getCassandraManager() {
        return cassandraManager;
    }
}