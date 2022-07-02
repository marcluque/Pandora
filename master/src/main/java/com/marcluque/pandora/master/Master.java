package com.marcluque.pandora.master;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.marcluque.hydra.server.Server;
import com.marcluque.hydra.shared.handler.Session;
import com.marcluque.hydra.shared.handler.listener.HydraSessionListener;
import com.marcluque.pandora.master.bot.MasterBot;
import com.marcluque.pandora.master.roundrobinlist.LinkedRoundRobinList;
import com.marcluque.pandora.master.roundrobinlist.RoundRobinList;
import com.marcluque.pandora.shared.PandoraProtocol;
import com.marcluque.pandora.shared.database.CassandraManager;
import io.netty.channel.ChannelOption;

/**
 * Created by marcluque on 27.11.2016.
 */
public class Master {

    private static CassandraManager cassandraManager;

    private final int urlsPerPacket;

    private final RoundRobinList<Session> sessions = new LinkedRoundRobinList<>();

    private boolean crawlerRunning;

    public Master(int urlsPerPacket) {
        this.urlsPerPacket = urlsPerPacket;

        // Cassandra
        cassandraManager = new CassandraManager("188.68.54.85", "pandora");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void start() {
        new Server.Builder("localhost", 8888, new PandoraProtocol())
                .bossThreads(4)
                .workerThreads(2)
                .useEpoll(true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 10 * 1000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .addListener(new HydraSessionListener() {
                    @Override
                    public void onConnected(Session session) {
                        sessions.add(session);

                        System.out.println("Slave connected! Amount of connected slaves: " + sessions.size());

                        if (!crawlerRunning) {
                            crawlerRunning = true;
                            new MasterBot(sessions, urlsPerPacket, 3);
                        }
                    }

                    @Override
                    public void onDisconnected(Session session) {
                        sessions.remove(session);

                        System.out.println("Slave disconnected! Amount of connected slaves: " + sessions.size());
                    }
                })
                .build();

        System.out.println("Server started!");

        //server.close();
    }

    public static CassandraManager getCassandraManager() {
        return cassandraManager;
    }
}