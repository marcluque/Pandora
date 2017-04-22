package de.datasec.pandora.slave.config;

import de.jackwhite20.cascade.client.impl.ClientConfig;
import de.jackwhite20.cascade.shared.protocol.Protocol;

import java.net.StandardSocketOptions;

/**
 * Created by DataSec on 27.11.2016.
 */
public class SlaveClientConfig extends ClientConfig {

    public SlaveClientConfig(Protocol protocol) {
        host("localhost");
        port(8050);
        workerThreads(1);
        option(StandardSocketOptions.TCP_NODELAY, true);
        protocol(protocol);
    }
}
