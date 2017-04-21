package de.datasec.pandora.shared;

import de.datasec.pandora.shared.packets.UrlPacket;
import de.jackwhite20.cascade.shared.protocol.Protocol;
import de.jackwhite20.cascade.shared.protocol.listener.PacketListener;

/**
 * Created by DataSec on 27.11.2016.
 */
public class PandoraProtocol extends Protocol {

    public PandoraProtocol(PacketListener packetListener) {
        registerListener(packetListener);
        registerPacket(UrlPacket.class);
    }
}
