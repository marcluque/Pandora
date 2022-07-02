package com.marcluque.pandora.shared;

import com.marcluque.hydra.shared.protocol.impl.HydraProtocol;
import com.marcluque.hydra.shared.protocol.packets.listener.HydraPacketListener;
import com.marcluque.pandora.shared.packets.UrlPacket;

/**
 * Created by marcluque on 27.11.2016.
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