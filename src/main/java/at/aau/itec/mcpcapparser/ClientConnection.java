package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.Inflater;

/**
 * The real magic occurs here. This class holds a list of clientbound packets and one of all serverbound packets. The
 * the <code>parsePackets</code> method combines these methods and parses the Minecraft specific information out
 * of the packets. After the parsing method is called, all parsed packets are stored in the <code>parsedPackets</code>
 * list. The packets then contain entity, as well as spatial information.
 */
public class ClientConnection {

    public enum ConnectionState {

        HANDSHAKE,
        LOGIN,
        PLAY,
        STATUS;
    }

    private static class ProtolInformation {
        public String protocol;
        public ParsingInformation parsingInformation;

        public ProtolInformation(String protocol, ParsingInformation parsingInformation) {
            this.protocol = protocol;
            this.parsingInformation = parsingInformation;
        }
    }

    private static final Map<Integer, ProtolInformation> CLIENTBOUND_PLAY;
    static {
        Map<Integer, ProtolInformation> aMap = new HashMap<>();
        aMap.put(0x00, new ProtolInformation("SpawnObject", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.UUID,
                ParsingInformation.MCDataTypes.BYTE,
                ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE,
                ParsingInformation.MCDataTypes.ANGLE, ParsingInformation.MCDataTypes.ANGLE},
                0, null, null, 3, 5, 4, 2)));
        aMap.put(0x01, new ProtolInformation("SpawnExperienceOrb", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE},
                0, null, null, 1, 2, 3)));
        aMap.put(0x02, new ProtolInformation("SpawnGlobalEntity", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.BYTE,
                ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE},
                0, null, null, 2, 4, 3)));
        aMap.put(0x03, new ProtolInformation("SpawnMob", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.UUID, ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE},
                0, null, null, 3, 5, 4)));
        aMap.put(0x04, new ProtolInformation("SpawnPainting", null));
        aMap.put(0x05, new ProtolInformation("SpawnPlayer", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.UUID,
                ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE,
                ParsingInformation.MCDataTypes.ANGLE, ParsingInformation.MCDataTypes.ANGLE},
                0, null, null, 2, 4, 3)));
        aMap.put(0x06, new ProtolInformation("Animation", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.BYTE},
                0, null, null, null, null, null)));
        aMap.put(0x07, new ProtolInformation("Statistics", null));
        aMap.put(0x08, new ProtolInformation("BlockBreak", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.LOCATION,
                ParsingInformation.MCDataTypes.BYTE}, 0, null,
                null, 1, null, null)));
        aMap.put(0x09, new ProtolInformation("UpdateBlockEntity", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.LOCATION,
                ParsingInformation.MCDataTypes.BYTE}, null, null,
                null, 0, null, null)));
        aMap.put(0x0a, new ProtolInformation("BlockAction", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.LOCATION,
                ParsingInformation.MCDataTypes.BYTE, ParsingInformation.MCDataTypes.BYTE, ParsingInformation.MCDataTypes.VARINT}
                , null, null,null, 0, null, null)));
        aMap.put(0x0b, new ProtolInformation("BlockChange", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.LOCATION}
                , null, null,null, 0, null, null)));
        aMap.put(0x0c, new ProtolInformation("BossBar", null));
        aMap.put(0x0d, new ProtolInformation("ServerDifficulty", null));
        aMap.put(0x0e, new ProtolInformation("TabComplete", null));
        aMap.put(0x0f, new ProtolInformation("ChatMessage", null));
        aMap.put(0x10, new ProtolInformation("Multiblock", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.INT}
                , null, 0,1, null, null, null)));
        aMap.put(0x11, new ProtolInformation("ConfirmTransaction", null));
        aMap.put(0x12, new ProtolInformation("CloseWindow", null));
        aMap.put(0x13, new ProtolInformation("OpenWindow", null));
        aMap.put(0x14, new ProtolInformation("WindowItems", null));
        aMap.put(0x15, new ProtolInformation("WindowProperty", null));
        aMap.put(0x16, new ProtolInformation("SetSlot", null));
        aMap.put(0x17, new ProtolInformation("SetCooldown", null));
        aMap.put(0x18, new ProtolInformation("PluginMessage", null));
        aMap.put(0x19, new ProtolInformation("NamedSoundEffect", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.STRING, ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.INT}
                , null, null,null, 2, 4, 3)));
        aMap.put(0x1a, new ProtolInformation("Disconnect", null));
        aMap.put(0x1b, new ProtolInformation("EntityStatus", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.BYTE}
                , 0, null,null, null, null, null)));
        aMap.put(0x1c, new ProtolInformation("Explosion", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.FLOAT, ParsingInformation.MCDataTypes.FLOAT, ParsingInformation.MCDataTypes.FLOAT}
                , null, null,null, 0, 2, 1)));
        aMap.put(0x1d, new ProtolInformation("UnloadChunk", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.INT},
                null, 0, 1, null, null, null)));
        aMap.put(0x1e, new ProtolInformation("ChangeGameState", null));
        aMap.put(0x1f, new ProtolInformation("KeepAlive", null));
        aMap.put(0x20, new ProtolInformation("ChunkData", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.INT},
                null, 0, 1, null, null, null)));
        aMap.put(0x21, new ProtolInformation("Effect", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.LOCATION},
                null, null, null, 1, null, null)));
        aMap.put(0x22, new ProtolInformation("Particle", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.INT, ParsingInformation.MCDataTypes.BOOL,
                ParsingInformation.MCDataTypes.FLOAT, ParsingInformation.MCDataTypes.FLOAT, ParsingInformation.MCDataTypes.FLOAT},
                null, null, null, 2, 4, 3)));
        aMap.put(0x23, new ProtolInformation("JoinGame", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x24, new ProtolInformation("Map", null));
        aMap.put(0x25, new ProtolInformation("Entity", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x26, new ProtolInformation("EntityRelativeMove", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.SHORT, ParsingInformation.MCDataTypes.SHORT, ParsingInformation.MCDataTypes.SHORT},
                0, null, null, null, null, null)));
        aMap.put(0x27, new ProtolInformation("EntityLookAndRelativeMove", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.SHORT, ParsingInformation.MCDataTypes.SHORT, ParsingInformation.MCDataTypes.SHORT, ParsingInformation.MCDataTypes.ANGLE, ParsingInformation.MCDataTypes.ANGLE},
                0, null, null, null, null, null)));
        aMap.put(0x28, new ProtolInformation("EntityLook", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.ANGLE, ParsingInformation.MCDataTypes.ANGLE},
                0, null, null, null, null, null)));
        aMap.put(0x29, new ProtolInformation("VehicleMove", null));
        aMap.put(0x2a, new ProtolInformation("OpenSignEditor", null));
        aMap.put(0x2b, new ProtolInformation("CraftRecipe", null));
        aMap.put(0x2c, new ProtolInformation("PlayerAbilities", null));
        aMap.put(0x2d, new ProtolInformation("CombatEvent", null));
        aMap.put(0x2e, new ProtolInformation("PlayerListItem", null));
        aMap.put(0x2f, new ProtolInformation("PlayerPositionAndLook", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.ANGLE, ParsingInformation.MCDataTypes.ANGLE},
                0, null, null, null, null, null)));
        aMap.put(0x30, new ProtolInformation("UseBed", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.LOCATION},
                0, null, null, 1, null, null)));
        aMap.put(0x31, new ProtolInformation("UnlockRecipies", null));
        aMap.put(0x32, new ProtolInformation("DestroyEntities", null));
        aMap.put(0x33, new ProtolInformation("RemoveEntityEffect", null));
        aMap.put(0x34, new ProtolInformation("ResourcePackSend", null));
        aMap.put(0x35, new ProtolInformation("Respawn", null));
        aMap.put(0x36, new ProtolInformation("EntityHeadLook", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x37, new ProtolInformation("SelectAdvancementTab", null));
        aMap.put(0x38, new ProtolInformation("WorldBorder", null));
        aMap.put(0x39, new ProtolInformation("Camera", null));
        aMap.put(0x3a, new ProtolInformation("HeldItemChange", null));
        aMap.put(0x3b, new ProtolInformation("DisplayScoreboard", null));
        aMap.put(0x3c, new ProtolInformation("EntityMetadata", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x3d, new ProtolInformation("AttachEntity", null));
        aMap.put(0x3e, new ProtolInformation("EntityVelocity", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x3f, new ProtolInformation("EntityEquipment", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x40, new ProtolInformation("SetExperience", null));
        aMap.put(0x41, new ProtolInformation("UpdateHealth", null));
        aMap.put(0x42, new ProtolInformation("ScoreboardObjective", null));
        aMap.put(0x43, new ProtolInformation("SetPassenger", null));
        aMap.put(0x44, new ProtolInformation("Teams", null));
        aMap.put(0x45, new ProtolInformation("UpdateScore", null));
        aMap.put(0x46, new ProtolInformation("SpawnPosition", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.LOCATION},
                null, null, null, 0, null, null)));
        aMap.put(0x47, new ProtolInformation("TimeUpdate", null));
        aMap.put(0x48, new ProtolInformation("Title", null));
        aMap.put(0x49, new ProtolInformation("SoundEffect", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.EFFECT_POSTION, ParsingInformation.MCDataTypes.EFFECT_POSTION, ParsingInformation.MCDataTypes.EFFECT_POSTION},
                null, null, null, 2, 4, 3)));
        aMap.put(0x4a, new ProtolInformation("PlayerListHeaderAndFooter", null));
        aMap.put(0x4b, new ProtolInformation("CollectItem", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT, ParsingInformation.MCDataTypes.VARINT},
                1, null, null, null, null, null)));
        aMap.put(0x4c, new ProtolInformation("EnttiyTeleport", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT,
                ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE, ParsingInformation.MCDataTypes.DOUBLE},
                0, null, null, 1, 3, 2)));
        aMap.put(0x4d, new ProtolInformation("Advancements", null));
        aMap.put(0x4e, new ProtolInformation("EntityProperties", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        aMap.put(0x4f, new ProtolInformation("EntityEffect", new ParsingInformation(new ParsingInformation.MCDataTypes[]{
                ParsingInformation.MCDataTypes.VARINT},
                0, null, null, null, null, null)));
        CLIENTBOUND_PLAY = Collections.unmodifiableMap(aMap);
    }

    private static final Map<Integer, ProtolInformation> SERVERBOUND_PLAY;
    static {
        Map<Integer, ProtolInformation> aMap = new HashMap<>();
        aMap.put(0x00, new ProtolInformation("TeleportConfirm", null));
        aMap.put(0x01, new ProtolInformation("TabComplete", null));
        aMap.put(0x02, new ProtolInformation("ChatMessage", null));
        aMap.put(0x03, new ProtolInformation("ClientStatus", null));
        aMap.put(0x04, new ProtolInformation("ClientSettings", null));
        aMap.put(0x05, new ProtolInformation("ConfirmTransaction", null));
        aMap.put(0x06, new ProtolInformation("EnchantItem", null));
        aMap.put(0x07, new ProtolInformation("ClickWindow", null));
        aMap.put(0x08, new ProtolInformation("CloseWindow", null));
        aMap.put(0x09, new ProtolInformation("PluginMessage", null));
        aMap.put(0x0a, new ProtolInformation("UseEntity", null));
        aMap.put(0x0b, new ProtolInformation("KeepAlive", null));
        aMap.put(0x0c, new ProtolInformation("Player", null));
        aMap.put(0x0d, new ProtolInformation("PlayerPosition", null));
        aMap.put(0x0e, new ProtolInformation("PlayerPositionAndLook", null));
        aMap.put(0x0f, new ProtolInformation("PlayerLook", null));
        aMap.put(0x10, new ProtolInformation("VehicleMove", null));
        aMap.put(0x11, new ProtolInformation("SteerBoat", null));
        aMap.put(0x12, new ProtolInformation("CraftRecipeRequest", null));
        aMap.put(0x13, new ProtolInformation("PlayerAbilities", null));
        aMap.put(0x14, new ProtolInformation("PlayerDigging", null));
        aMap.put(0x15, new ProtolInformation("EntityAction", null));
        aMap.put(0x16, new ProtolInformation("SteerVehicle", null));
        aMap.put(0x17, new ProtolInformation("CraftingBookData", null));
        aMap.put(0x18, new ProtolInformation("ResourcePackStatus", null));
        aMap.put(0x19, new ProtolInformation("AdvancementTab", null));
        aMap.put(0x1a, new ProtolInformation("HeldItemChange", null));
        aMap.put(0x1b, new ProtolInformation("CreativeInventoryAction", null));
        aMap.put(0x1c, new ProtolInformation("UpdateSign", null));
        aMap.put(0x1d, new ProtolInformation("Animation", null));
        aMap.put(0x1e, new ProtolInformation("Spectate", null));
        aMap.put(0x1f, new ProtolInformation("PlayerBlockPlacement", null));
        aMap.put(0x20, new ProtolInformation("UseItem", null));
        SERVERBOUND_PLAY = Collections.unmodifiableMap(aMap);
    }

    private List<MinecraftPacket> serverbound = new ArrayList<>();
    private List<MinecraftPacket> clientbound = new ArrayList<>();
    private List<MinecraftPacket> parsedPackets = new ArrayList<>();

    private boolean compressed = false;
    private String ip;
    private String streamIdentifier;
    private int compressionThreshold = -1;
    private PrintWriter writer;

    private byte[] serverboundCarryover = null;
    private byte[] clientboundCarryover = null;

    private int parsingErrors = 0;

    private ConnectionState connectionState = ConnectionState.HANDSHAKE;

    public ClientConnection(String ip, String streamIdentifier) {
        this.ip = ip;
        this.streamIdentifier = streamIdentifier;
        try {
            this.writer = new PrintWriter(streamIdentifier + "_parsedPackets.log", "UTF-8");
            printCSVHeader();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void addPacket(long timestamp, boolean serverbound, ByteBuf payload, long tcpSeqNo) {
        if (serverbound) {
            this.serverbound.add(new MinecraftPacket(timestamp, serverbound, payload, tcpSeqNo));
        } else {
            this.clientbound.add(new MinecraftPacket(timestamp, serverbound, payload, tcpSeqNo));
        }
    }

    public void parsePackets() throws IOException {

        Collections.sort(clientbound, new MinecraftPacket.MinecraftPacketSeqNoComparator());
        Collections.sort(serverbound, new MinecraftPacket.MinecraftPacketSeqNoComparator());
        eliminateDupSeqNos(clientbound);
        eliminateDupSeqNos(serverbound);
        List<MinecraftPacket> allPackets = new ArrayList<>();
        allPackets.addAll(clientbound);
        allPackets.addAll(serverbound);
        Collections.sort(allPackets, new MinecraftPacket.MinecraftPacketTimestampComparator());
        parsedPackets.clear();

        int i = -1;
        for (Iterator<MinecraftPacket> iterator = allPackets.iterator(); iterator.hasNext();) {
            MinecraftPacket packet = iterator.next();
            i++;

            // Check if event already started in last packet
            if (packet.isServerbound() && serverboundCarryover != null) {
                byte[] newPayload = ArrayUtils.addAll(serverboundCarryover, packet.getPayload().array());
                ByteBuf newPayloadBuf = Unpooled.wrappedBuffer(newPayload);
                packet.getPayload().release();
                packet.setPayload(newPayloadBuf);
                serverboundCarryover = null;
            } else if (!packet.isServerbound() && clientboundCarryover != null) {
                byte[] newPayload = ArrayUtils.addAll(clientboundCarryover, packet.getPayload().array());
                ByteBuf newPayloadBuf = Unpooled.wrappedBuffer(newPayload);
                packet.getPayload().release();
                packet.setPayload(newPayloadBuf);
                clientboundCarryover = null;
            }

            // Check if multiple packets are hidden in payload
            while (packet.getPayload().readerIndex() < packet.getPayload().maxCapacity()) {
                boolean releaseReadBuffer = false;
                packet.getPayload().markReaderIndex();
                int packetStart = packet.getPayload().readerIndex();
                int size = -1;
                try {
                    size = ByteBufUtils.readVarInt(packet.getPayload());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    parsingErrors++;
                    if (packet.isServerbound()) {
                        serverboundCarryover = null;
                    } else {
                        clientboundCarryover = null;
                    }
                    break;
                }

                if (size < 0 || size > 10000) {
                    if (packet.isServerbound()) {
                        serverboundCarryover = null;
                    } else {
                        clientboundCarryover = null;
                    }
                    break;
                }
                int sizeVarIntLenght = packet.getPayload().readerIndex() - packetStart;

                // Check if remaining payload size is to small to cover payload
                if (size > packet.getPayload().maxCapacity() - packet.getPayload().readerIndex()) {
                    packet.getPayload().resetReaderIndex();
                    byte[] remainingData = new byte[packet.getPayload().maxCapacity() - packet.getPayload().readerIndex()];
                    packet.getPayload().readBytes(remainingData);
                    if (packet.isServerbound()) {
                        serverboundCarryover = remainingData;
                    } else {
                        clientboundCarryover = remainingData;
                    }
                    break;
                }
                packet.setPayloadLength(size + sizeVarIntLenght);

                int uncompressedSize = -1;
                int packetType = -1;
                ByteBuf readBuffer = null;
                if (compressed) {
                    int pointerBefore = packet.getPayload().readerIndex();
                    //uncompressedSize = ByteBufUtils.readVarInt(packet.getPayload());
                    try {
                        uncompressedSize = ByteBufUtils.readVarInt(packet.getPayload());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        parsingErrors++;
                        if (packet.isServerbound()) {
                            serverboundCarryover = null;
                        } else {
                            clientboundCarryover = null;
                        }
                        break;
                    }
                    int varIntSize = packet.getPayload().readerIndex() - pointerBefore;

                    if (uncompressedSize > 0) {

                        int len = size - varIntSize;
                        ByteBuf compressed = packet.getPayload().readBytes(len);
                        ByteBuf uncompressed = decompressData(compressed, len, uncompressedSize);
                        compressed.release();
                        releaseReadBuffer = true;
                        if (uncompressed.maxCapacity() == 0) {
                            parsingErrors++;
                            if (packet.isServerbound()) {
                                serverboundCarryover = null;
                            } else {
                                clientboundCarryover = null;
                            }
                            uncompressed.release();
                            break;
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
                        packet.setPacketType("Handshake");
                        storeAndLogParsedPacket(packet, i, connectionState);
                        parseHandshakePacket(readBuffer);
                    }

                } else if (connectionState == ConnectionState.STATUS) {

                    if (packet.isServerbound() && packetType == 0x00) {
                        // Serverbound request
                        packet.setPacketType("Request");
                        storeAndLogParsedPacket(packet, i, connectionState);
                    } else if (packet.isServerbound() && packetType == 0x01) {
                        // Serverbound ping
                        packet.setPacketType("Ping");
                        storeAndLogParsedPacket(packet, i, connectionState);
                    } else if (!packet.isServerbound() && packetType == 0x00) {
                        // Clientbound response
                        packet.setPacketType("Response");
                        storeAndLogParsedPacket(packet, i, connectionState);

                        int stringLen = ByteBufUtils.readVarInt(packet.getPayload());
                        String response = packet.getPayload().readCharSequence(stringLen, Charset.defaultCharset()).toString();
                        System.out.println(response);

                    } else if (!packet.isServerbound() && packetType == 0x01) {
                        // Clientbound pong
                        packet.setPacketType("Pong");
                        storeAndLogParsedPacket(packet, i, connectionState);
                    }

                } else if (connectionState == ConnectionState.LOGIN) {

                    if (packet.isServerbound()) {

                        if (packetType == 0x00) {
                            packet.setPacketType("LoginStart");
                            storeAndLogParsedPacket(packet, i, connectionState);
                        } else if (packetType == 0x01) {
                            packet.setPacketType("EncryptionResponse");
                            storeAndLogParsedPacket(packet, i, connectionState);
                        }

                    } else {

                        if (packetType == 0x00) {
                            packet.setPacketType("Disconnect");
                            storeAndLogParsedPacket(packet, i, connectionState);
                        } else if (packetType == 0x01) {
                            packet.setPacketType("EncryptionRequest");
                            storeAndLogParsedPacket(packet, i, connectionState);
                        } else if (packetType == 0x02) {
                            packet.setPacketType("LoginSuccess");
                            storeAndLogParsedPacket(packet, i, connectionState);
                            connectionState = ConnectionState.PLAY;
                        } else if (packetType == 0x03) {
                            packet.setPacketType("SetCompression");
                            parseSetcompressionPacket(readBuffer);
                            storeAndLogParsedPacket(packet, i, connectionState);
                        }

                    }
                } else if (connectionState == ConnectionState.PLAY) {

                    Map<Integer, ProtolInformation> protocolInfo = packet.isServerbound() ? SERVERBOUND_PLAY : CLIENTBOUND_PLAY;
                    if (protocolInfo.containsKey(packetType)) {
                        ProtolInformation protocol = protocolInfo.get(packetType);
                        packet.setPacketType(protocol.protocol);
                        if (protocol.parsingInformation != null) {
                            MinecraftPacket parsedPacket = new MinecraftPacket(packet.getTimestamp(), packet.isServerbound(), packet.getPayload(), packet.getTcpSeqNo());
                            parsedPacket.setPacketNo(i);
                            parsedPacket.setPacketType(packet.getPacketType());
                            parsedPacket.setPayloadLength(packet.getPayloadLength());
                            DecodingUtils.parseInformation(readBuffer, parsedPacket, protocol.parsingInformation);
                            storeAndLogParsedPacket(parsedPacket, i, connectionState);

                        } else {

                            storeAndLogParsedPacket(packet, i, connectionState);

                        }

                    } else {
                        packet.setPacketType(String.format("%02X", packetType));
                        storeAndLogParsedPacket(packet, i, connectionState);
                    }


                }

                if (releaseReadBuffer) {
                    // Otherwise causes exception
                    readBuffer.release();
                }

                // Check if everything was read, if not, set ReaderIndex to correct position
                if (packet.getPayload().readerIndex() < packetStart + size + sizeVarIntLenght) {
                    int bytesToRead = (packetStart + size + sizeVarIntLenght) - packet.getPayload().readerIndex();
                    packet.getPayload().readBytes(bytesToRead).release();
                }
            }

        }
        System.out.println("ParsingErrors: " + parsingErrors);
        writer.close();
    }

    private void eliminateDupSeqNos(List<MinecraftPacket> packetlist) {
        long lastSeq = -1;
        for (Iterator<MinecraftPacket> iterator = packetlist.iterator(); iterator.hasNext();) {
            MinecraftPacket p = iterator.next();
            if (lastSeq == p.getTcpSeqNo()) {
                iterator.remove();
            } else {
                lastSeq = p.getTcpSeqNo();
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

    private void storeAndLogParsedPacket(MinecraftPacket packet, int packetNo, ConnectionState connectionState) {
//        System.out.println(packet.getTimestamp() + "\t" + packetNo + "\t"
//                + (packet.isServerbound() ? "C->S" : "S->C") + "\t"
//                + connectionState + "\t"
//                + packetType);
        packet.setPacketNo(packetNo);
        writer.println(packet.getTimestamp() + "\t" + packet.getPacketNo() + "\t"
                + (packet.isServerbound() ? "C->S" : "S->C") + "\t"
                + connectionState + "\t"
                + packet.getPacketType() + "\t"
                + packet.getPayloadLength() + "\t"
                + Objects.toString(packet.getEntityId(), "") + "\t"
                + Objects.toString(packet.getBlockX(), "") + "\t"
                + Objects.toString(packet.getBlockZ()) + "\t"
                + Objects.toString(packet.getBlockY(), "") + "\t"
                + Objects.toString(packet.getChunkX(), null) + "\t"
                + Objects.toString(packet.getChunkZ(), null) + "\t"
                + Objects.toString(packet.getEntityType()));
        parsedPackets.add(packet);
    }

    private void printCSVHeader() {
        writer.println("Timestamp\tPacketNo\tDirection\tState\tMessageType\tPayloadInBytes\tEntityId\tx\tz\ty\tChunkX\tChunkZ\tEntityType");
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
        int counter = 0;
        try {
            while (!iflr.finished()) {
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
                counter++;
                if (counter > targetLen) {
                    throw new RuntimeException("Unzipping runs in infinite loop");
                }
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
