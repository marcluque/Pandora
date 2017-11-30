package de.datasec.pandora.shared.packets;

import de.datasec.hydra.shared.protocol.packets.Packet;
import de.datasec.hydra.shared.protocol.packets.PacketId;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by DataSec on 01.12.2016.
 */
@PacketId(0)
public class UrlPacket extends Packet {

    private String[] linksToCrawl;

    public UrlPacket() {}

    public UrlPacket(Set<String> linksToCrawl) {
        this.linksToCrawl = linksToCrawl.toArray(new String[linksToCrawl.size()]);
    }

    @Override
    public void read() {
        linksToCrawl = readArray();
    }

    @Override
    public void write() {
        writeArray(linksToCrawl);
    }

    public String[] getLinksToCrawl() {
        return linksToCrawl;
    }

    @Override
    public String toString() {
        return "UrlPacket{" + "linksToCrawl=" + Arrays.toString(linksToCrawl) + '}';
    }
}