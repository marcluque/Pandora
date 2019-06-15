package de.datasecs.pandora.shared;

import de.datasecs.hydra.shared.protocol.HydraProtocol;
import de.datasecs.hydra.shared.protocol.packets.listener.HydraPacketListener;
import de.datasecs.pandora.shared.packets.UrlPacket;

/**
 * Created by DataSecs on 27.11.2016.
 */
public class PandoraProtocol extends HydraProtocol {

    public PandoraProtocol(HydraPacketListener packetListener) {
        registerListener(packetListener);
        registerPacket(UrlPacket.class);
    }

    public PandoraProtocol() {
        registerPacket(UrlPacket.class);
    }
}