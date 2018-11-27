package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.Inflater;

public class ClientConnection {

    public enum ConnectionState {

        HANDSHAKE,
        LOGIN,
        PLAY,
        STATUS;
    }

    private static class ProtolInformation {
        String protocol;
        String parsingMethod;

        public ProtolInformation(String protocol, String parsingMethod) {
            this.protocol = protocol;
            this.parsingMethod = parsingMethod;
        }
    }

    private static final Map<Integer, ProtolInformation> CLIENTBOUND_PLAY;
    static {
        Map<Integer, ProtolInformation> aMap = new HashMap<>();
        aMap.put(0x00, new ProtolInformation("SpawnObject", null));
        aMap.put(0x01, new ProtolInformation("SpawnExperienceOrb", null));
        aMap.put(0x02, new ProtolInformation("SpawnGlobalEntity", null));
        aMap.put(0x03, new ProtolInformation("SpawnMob", null));
        aMap.put(0x04, new ProtolInformation("SpawnPainting", null));
        aMap.put(0x05, new ProtolInformation("SpawnPlayer", null));
        aMap.put(0x06, new ProtolInformation("Animation", null));
        aMap.put(0x07, new ProtolInformation("Statistics", null));
        aMap.put(0x08, new ProtolInformation("BlockBreak", null));
        aMap.put(0x09, new ProtolInformation("UpdateBlockEntity", null));
        aMap.put(0x0a, new ProtolInformation("BlockAction", null));
        aMap.put(0x0b, new ProtolInformation("BlockChange", null));
        aMap.put(0x0c, new ProtolInformation("BossBar", null));
        aMap.put(0x0d, new ProtolInformation("ServerDifficulty", null));
        aMap.put(0x0e, new ProtolInformation("TabComplete", null));
        aMap.put(0x0f, new ProtolInformation("ChatMessage", null));
        aMap.put(0x10, new ProtolInformation("Multiblock", null));
        aMap.put(0x11, new ProtolInformation("ConfirmTransaction", null));
        aMap.put(0x12, new ProtolInformation("CloseWindow", null));
        aMap.put(0x13, new ProtolInformation("OpenWindow", null));
        aMap.put(0x14, new ProtolInformation("WindowItems", null));
        aMap.put(0x15, new ProtolInformation("WindowProperty", null));
        aMap.put(0x16, new ProtolInformation("SetSlot", null));
        aMap.put(0x17, new ProtolInformation("SetCooldown", null));
        aMap.put(0x18, new ProtolInformation("PluginMessage", null));
        aMap.put(0x19, new ProtolInformation("NamedSoundEffect", null));
        aMap.put(0x1a, new ProtolInformation("Disconnect", null));
        aMap.put(0x1b, new ProtolInformation("EntityStatus", null));
        aMap.put(0x1c, new ProtolInformation("Explosion", null));
        aMap.put(0x1d, new ProtolInformation("UnloadChunk", null));
        aMap.put(0x1e, new ProtolInformation("ChangeGameState", null));
        aMap.put(0x1f, new ProtolInformation("KeepAlive", null));
        aMap.put(0x20, new ProtolInformation("ChunkData", null));
        aMap.put(0x21, new ProtolInformation("Effect", null));
        aMap.put(0x22, new ProtolInformation("Particle", null));
        aMap.put(0x23, new ProtolInformation("JoinGame", null));
        aMap.put(0x24, new ProtolInformation("Map", null));
        aMap.put(0x25, new ProtolInformation("Entity", null));
        aMap.put(0x26, new ProtolInformation("EntityRelativeMove", null));
        aMap.put(0x27, new ProtolInformation("EntityLookAndRelativeMove", null));
        aMap.put(0x28, new ProtolInformation("EntityLook", null));
        aMap.put(0x29, new ProtolInformation("VehicleMove", null));
        aMap.put(0x2a, new ProtolInformation("OpenSignEditor", null));
        aMap.put(0x2b, new ProtolInformation("CraftRecipe", null));
        aMap.put(0x2c, new ProtolInformation("PlayerAbilities", null));
        aMap.put(0x2d, new ProtolInformation("CombatEvent", null));
        aMap.put(0x2e, new ProtolInformation("PlayerListItem", null));
        aMap.put(0x2f, new ProtolInformation("PlayerPositionAndLook", null));
        aMap.put(0x30, new ProtolInformation("UseBed", null));
        aMap.put(0x31, new ProtolInformation("UnlockRecipies", null));
        aMap.put(0x32, new ProtolInformation("DestroyEntities", null));
        aMap.put(0x33, new ProtolInformation("RemoveEntityEffect", null));
        aMap.put(0x34, new ProtolInformation("ResourcePackSend", null));
        aMap.put(0x35, new ProtolInformation("Respawn", null));
        aMap.put(0x36, new ProtolInformation("EntityHeadLook", null));
        aMap.put(0x37, new ProtolInformation("SelectAdvancementTab", null));
        aMap.put(0x38, new ProtolInformation("WorldBorder", null));
        aMap.put(0x39, new ProtolInformation("Camera", null));
        aMap.put(0x3a, new ProtolInformation("HeldItemChange", null));
        aMap.put(0x3b, new ProtolInformation("DisplayScoreboard", null));
        aMap.put(0x3c, new ProtolInformation("EntityMetadata", null));
        aMap.put(0x3d, new ProtolInformation("AttachEntity", null));
        aMap.put(0x3e, new ProtolInformation("EntityVelocity", null));
        aMap.put(0x3f, new ProtolInformation("EntityEquipment", null));
        aMap.put(0x40, new ProtolInformation("SetExperience", null));
        aMap.put(0x41, new ProtolInformation("UpdateHealth", null));
        aMap.put(0x42, new ProtolInformation("ScoreboardObjective", null));
        aMap.put(0x43, new ProtolInformation("SetPassenger", null));
        aMap.put(0x44, new ProtolInformation("Teams", null));
        aMap.put(0x45, new ProtolInformation("UpdateScore", null));
        aMap.put(0x46, new ProtolInformation("SpawnPosition", null));
        aMap.put(0x47, new ProtolInformation("TimeUpdate", null));
        aMap.put(0x48, new ProtolInformation("Title", null));
        aMap.put(0x49, new ProtolInformation("SoundEffect", null));
        aMap.put(0x4a, new ProtolInformation("PlayerListHeaderAndFooter", null));
        aMap.put(0x4b, new ProtolInformation("CollectItem", null));
        aMap.put(0x4c, new ProtolInformation("EnttiyTeleport", null));
        aMap.put(0x4d, new ProtolInformation("Advancements", null));
        aMap.put(0x4e, new ProtolInformation("EntityProperties", null));
        aMap.put(0x4f, new ProtolInformation("EntityEffect", null));
        CLIENTBOUND_PLAY = Collections.unmodifiableMap(aMap);
    }

    private List<MinecraftPacket> allPackets = new ArrayList<>();
    private List<MinecraftPacket> serverbound = new ArrayList<>();
    private List<MinecraftPacket> clientbound = new ArrayList<>();

    private boolean compressed = false;
    private String ip;
    private int compressionThreshold = -1;

    private int decompressionErrors;

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
        int i = -1;
        for (Iterator<MinecraftPacket> iterator = allPackets.iterator(); iterator.hasNext();) {
            MinecraftPacket packet = iterator.next();
            i++;

            int size = ByteBufUtils.readVarInt(packet.getPayload());

            int uncompressedSize = -1;
            int packetType = -1;
            ByteBuf readBuffer = null;
            if (compressed) {
                int pointerBefore = packet.getPayload().readerIndex();
                uncompressedSize = ByteBufUtils.readVarInt(packet.getPayload());
                int varIntSize = packet.getPayload().readerIndex() - pointerBefore;

                if (uncompressedSize > 0) {

                    int len = size - varIntSize;
                    ByteBuf compressed = packet.getPayload().readBytes(len);
                    ByteBuf uncompressed = decompressData(compressed, len, uncompressedSize);
                    if (uncompressed.maxCapacity() == 0) {
                        continue;
                    }
                    packetType = ByteBufUtils.readVarInt(uncompressed);
                    readBuffer = uncompressed;

                } else {
                    // If Data Length is set to zero, then the packet is uncompressed; otherwise it is the size of the uncompressed packet.
                    packetType = ByteBufUtils.readVarInt(packet.getPayload());
                    readBuffer = packet.getPayload();
                }

            } else {
                packetType = ByteBufUtils.readVarInt(packet.getPayload());
                readBuffer = packet.getPayload();
            }

            if (connectionState == ConnectionState.HANDSHAKE) {

                if (packetType == 0x00) {
                    logPacket(packet, i, "Handshake", connectionState);
                    parseHandshakePacket(readBuffer);
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

                if (packet.isServerbound()) {

                    if (packetType == 0x00) {
                        logPacket(packet, i, "LoginStart", connectionState);
                    } else if (packetType == 0x01) {
                        logPacket(packet, i, "EncryptionResponse", connectionState);
                    }

                } else {

                    if (packetType == 0x00) {
                        logPacket(packet, i, "Disconnect", connectionState);
                    } else if (packetType == 0x01) {
                        logPacket(packet, i, "EncryptionRequest", connectionState);
                    } else if (packetType == 0x02) {
                        logPacket(packet, i, "LoginSuccess", connectionState);
                        connectionState = ConnectionState.PLAY;
                    } else if (packetType == 0x03) {
                        logPacket(packet, i, "SetCompression", connectionState);
                        parseSetcompressionPacket(readBuffer);
                    }

                }
            } else if (connectionState == ConnectionState.PLAY) {

                String messageType = String.format("%02X", packetType);
                if (CLIENTBOUND_PLAY.containsKey(packetType)) {
                    messageType = CLIENTBOUND_PLAY.get(packetType).protocol;
                }
                logPacket(packet, i, messageType, connectionState);

            }

        }
    }

    private void parseHandshakePacket(ByteBuf packet) throws IOException {
        int protocolVersion = ByteBufUtils.readVarInt(packet);
        int stringLen = ByteBufUtils.readVarInt(packet);
        String serverIp = packet.readCharSequence(stringLen, Charset.defaultCharset()).toString();
        int serverPort = packet.getUnsignedShort(packet.readerIndex());
        packet.readBytes(2); // Required because last method does not modify reader index
        int nextState = ByteBufUtils.readVarInt(packet);
        if (nextState == 1) {
            connectionState = ConnectionState.STATUS;
        } else if (nextState == 2) {
            connectionState = ConnectionState.LOGIN;
        }
    }

    private void parseSetcompressionPacket(ByteBuf packet) throws IOException {
        int threshold = ByteBufUtils.readVarInt(packet);
        compressed = true;
        compressionThreshold = threshold;
    }

    private void logPacket(MinecraftPacket packet, int packetNo, String packetType, ConnectionState connectionState) {
        System.out.println(packet.getTimestamp() + "\t" + packetNo + "\t"
                + (packet.isServerbound() ? "C->S" : "S->C") + "\t"
                + connectionState + "\t"
                + packetType);
    }

    private ByteBuf decompressData(ByteBuf compressed, int len, int targetLen) {

        boolean error = false;
        byte[] compressedBytes = new byte[len];
        compressed.readBytes(compressedBytes);
        ByteArrayOutputStream baos = null;
        Inflater iflr = new Inflater();
        iflr.setInput(compressedBytes);
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4 * 1024];
        try {
            while (!iflr.finished()) {
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error = true;
        } finally {
            try {
                if (baos != null) baos.close();
            } catch (Exception ex) {
                error = true;
            }
        }

        if (error) {
            decompressionErrors++;
        }

        byte[] bytes = baos.toByteArray();
        return Unpooled.wrappedBuffer(bytes);
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
