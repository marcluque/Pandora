package de.datasec.pandora.master.config;

import de.jackwhite20.cascade.server.impl.ServerConfig;
import de.jackwhite20.cascade.shared.protocol.Protocol;

import java.net.StandardSocketOptions;

/**
 * Created by DataSec on 27.11.2016.
 */
public class MasterServerConfig extends ServerConfig {

    public MasterServerConfig(Protocol protocol) {
        host("localhost");
        port(8050);
        workerThreads(4);
        backlog(200);
        option(StandardSocketOptions.TCP_NODELAY, true);
        protocol(protocol);
    }
}
