package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection {

    public enum ConnectionState {

        HANDSHAKE,
        LOGIN,
        PLAY,
        STATUS;
    }

    private List<MinecraftPacket> allPackets = new ArrayList<>();
    private List<MinecraftPacket> serverbound = new ArrayList<>();
    private List<MinecraftPacket> clientbound = new ArrayList<>();

    private boolean compressed = false;
    private String ip;
    private int compressionThreshold = -1;

    private ConnectionState connectionState = ConnectionState.HANDSHAKE;

    public ClientConnection(String ip) {
        this.ip = ip;
    }

    public void addPacket(long timestamp, boolean serverbound, ByteBuf payload) {
        allPackets.add(new MinecraftPacket(timestamp, serverbound, payload));
        if (serverbound) {
            this.serverbound.add(new MinecraftPacket(timestamp, serverbound, payload));
        } else {
            this.clientbound.add(new MinecraftPacket(timestamp, serverbound, payload));
        }
    }

    public void parsePackets() throws IOException {
        int i = 0;
        for (MinecraftPacket packet : allPackets) {

            int size = ByteBufUtils.readVarInt(packet.getPayload());
            int compressedSize = -1;
            int packetType = -1;
            if (compressed) {
                throw new RuntimeException("Compression not implemented yet");
            } else {
                packetType = ByteBufUtils.readVarInt(packet.getPayload());
            }

            if (connectionState == ConnectionState.HANDSHAKE) {

                if (packetType == 0x00) {
                    logPacket(packet, i, "Handshake", connectionState);
                    parseHandshakePacket(packet);
                }

            } else if (connectionState == ConnectionState.STATUS) {

                if (packet.isServerbound() && packetType == 0x00) {
                    // Serverbound request
                    logPacket(packet, i, "Request", connectionState);
                } else if (packet.isServerbound() && packetType == 0x01) {
                    // Serverbound ping
                    logPacket(packet, i, "Ping", connectionState);
                } else if (!packet.isServerbound() && packetType == 0x00) {
                    // Clientbound response
                    logPacket(packet, i, "Response", connectionState);

                    int stringLen = ByteBufUtils.readVarInt(packet.getPayload());
                    String response = packet.getPayload().readCharSequence(stringLen, Charset.defaultCharset()).toString();
                    System.out.println(response);

                } else if (!packet.isServerbound() && packetType == 0x01) {
                    // Clientbound pong
                    logPacket(packet, i, "Pong", connectionState);
                }

            } else if (connectionState == ConnectionState.LOGIN) {

            }

            i++;
        }
    }

    private void parseHandshakePacket(MinecraftPacket packet) throws IOException {
        int protocolVersion = ByteBufUtils.readVarInt(packet.getPayload());
        int stringLen = ByteBufUtils.readVarInt(packet.getPayload());
        String serverIp = packet.getPayload().readCharSequence(stringLen, Charset.defaultCharset()).toString();
        int serverPort = packet.getPayload().getUnsignedShort(packet.getPayload().readerIndex());
        packet.getPayload().readBytes(2); // Required because last method does not modify reader index
        int nextState = ByteBufUtils.readVarInt(packet.getPayload());
        if (nextState == 1) {
            connectionState = ConnectionState.STATUS;
        } else if (nextState == 2) {
            connectionState = ConnectionState.LOGIN;
        }
    }

    private void logPacket(MinecraftPacket packet, int packetNo, String packetType, ConnectionState connectionState) {
        System.out.println(packet.getTimestamp() + "\t" + packetNo + "\t"
                + (packet.isServerbound() ? "C->S" : "S->C") + "\t"
                + connectionState + "\t"
                + packetType);
    }

    // ------- Begin GETTER AND SETTER ---------

    public List<MinecraftPacket> getServerbound() {
        return serverbound;
    }

    public void setServerbound(List<MinecraftPacket> serverbound) {
        this.serverbound = serverbound;
    }

    public List<MinecraftPacket> getClientbound() {
        return clientbound;
    }

    public void setClientbound(List<MinecraftPacket> clientbound) {
        this.clientbound = clientbound;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
