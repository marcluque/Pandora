package de.datasec.pandora.shared;

import de.datasec.hydra.shared.protocol.HydraProtocol;
import de.datasec.hydra.shared.protocol.packets.HydraPacketListener;
import de.datasec.pandora.shared.packets.UrlPacket;

/**
 * Created by DataSec on 27.11.2016.
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
