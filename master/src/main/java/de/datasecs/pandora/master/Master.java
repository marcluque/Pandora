package de.datasecs.pandora.master;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import de.datasecs.hydra.server.HydraServer;
import de.datasecs.hydra.server.Server;
import de.datasecs.hydra.shared.handler.Session;
import de.datasecs.hydra.shared.handler.listener.HydraSessionListener;
import de.datasecs.pandora.master.bot.MasterBot;
import de.datasecs.pandora.master.roundrobinlist.LinkedRoundRobinList;
import de.datasecs.pandora.master.roundrobinlist.RoundRobinList;
import de.datasecs.pandora.shared.PandoraProtocol;
import de.datasecs.pandora.shared.database.CassandraManager;
import io.netty.channel.ChannelOption;

/**
 * Created by DataSecs on 27.11.2016.
 */
public class Master {

    private static CassandraManager cassandraManager;

    private int urlsPerPacket;

    private RoundRobinList<Session> sessions = new LinkedRoundRobinList<>();

    private boolean crawlerRunning;

    public Master(int urlsPerPacket) {
        this.urlsPerPacket = urlsPerPacket;

        // Cassandra
        cassandraManager = new CassandraManager("188.68.54.85", "pandora");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void start() {
        HydraServer server = new Server.Builder("localhost", 8888, new PandoraProtocol())
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