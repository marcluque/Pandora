package de.datasecs.pandora.shared.packets;

import de.datasecs.hydra.shared.protocol.packets.Packet;
import de.datasecs.hydra.shared.protocol.packets.PacketId;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by DataSecs on 01.12.2016.
 */
@PacketId(0)
public class UrlPacket extends Packet {

    private String[] linksToCrawl;

    public UrlPacket() {}

    @Override
    public void read(ByteBuf byteBuf) {
        linksToCrawl = readArray(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf) {
        writeArray(byteBuf, linksToCrawl);
    }

    public UrlPacket(Set<String> linksToCrawl) {
        this.linksToCrawl = linksToCrawl.toArray(new String[0]);
    }

    public String[] getLinksToCrawl() {
        return linksToCrawl;
    }

    @Override
    public String toString() {
        return "UrlPacket{" + "linksToCrawl=" + Arrays.toString(linksToCrawl) + '}';
    }
}