package de.datasec.pandora.master;

import com.datastax.driver.core.policies.DefaultRetryPolicy;
import de.datasec.hydra.server.HydraServer;
import de.datasec.hydra.server.Server;
import de.datasec.hydra.shared.handler.Session;
import de.datasec.pandora.master.bot.MasterBot;
import de.datasec.pandora.master.roundrobinlist.LinkedRoundRobinList;
import de.datasec.pandora.master.roundrobinlist.RoundRobinList;
import de.datasec.pandora.shared.PandoraProtocol;
import de.datasec.pandora.shared.database.CassandraManager;

import java.net.StandardSocketOptions;

/**
 * Created by DataSec on 27.11.2016.
 */
public class Master {

    private static CassandraManager cassandraManager;

    private String startUrl;

    private int urlsPerPacket;

    private RoundRobinList<Session> sessions = new LinkedRoundRobinList<>();

    public Master(String startUrl, int urlsPerPacket) {
        this.startUrl = startUrl;
        this.urlsPerPacket = urlsPerPacket;

        // Cassandra
        cassandraManager = new CassandraManager("188.68.54.85", "pandora");
        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
    }

    public void start() {
        HydraServer server = new Server.Builder("188.68.54.85", 8888, new PandoraProtocol())
                .bossThreads(4)
                .workerThreads(2)
                .option(StandardSocketOptions.TCP_NODELAY, true)
                .option(StandardSocketOptions.SO_KEEPALIVE, true)
                .childOption(StandardSocketOptions.TCP_NODELAY, true)
                .childOption(StandardSocketOptions.SO_KEEPALIVE, true)
                .build();

        // TODO: LISTENER FOR SESSIONS THAT CONNECT TO SERVER

        new MasterBot(sessions, startUrl, urlsPerPacket, 3);
        server.close();
    }

    public static CassandraManager getCassandraManager() {
        return cassandraManager;
    }
}