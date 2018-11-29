package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class DecodingUtils {

    public static Position decodePosition(ByteBuf buffer) {
        long val = buffer.readLong();
        int x = (int) (val >> 38);
        int y = (int) ((val >> 26) & 0xFFF);
        int z = (int) (val << 38 >> 38);
        return new Position(x, z, y);
    }

    public static int parseVarint(ByteBuf buffer) throws IOException {
        return ByteBufUtils.readVarInt(buffer);
    }

    public static String parseUUID(ByteBuf buffer) {
        char[] uuid = new char[16];
        for (int i = 0; i < uuid.length; i++) {
            uuid[i] = (char) buffer.readByte();

        }
        return new String(uuid);
    }

    public static short parseAngle(ByteBuf buffer) {
        short angle = buffer.readByte();
        return angle;
    }

    public static void parseInformation(ByteBuf buffer, MinecraftPacket packet,  ParsingInformation information) throws IOException {
        for (int i = 0; i < information.getParsingOrder().length; i++) {

            Object parsed = null;
            switch (information.getParsingOrder()[i]) {
                case VARINT:
                    parsed = parseVarint(buffer);
                    break;
                case UUID:
                    parsed = parseUUID(buffer);
                    break;
                case DOUBLE:
                    parsed = buffer.getDouble(buffer.readerIndex());
                    buffer.readBytes(8).release();
                    break;
                case ANGLE:
                    parsed = parseAngle(buffer);
                    break;
                case INT:
                    parsed = buffer.readInt();
                    break;
                case SHORT:
                    parsed = buffer.readShort();
                    break;
                case POSITION:
                    parsed = decodePosition(buffer);
            }
            if (information.getEntityIdPosition() != null && information.getEntityIdPosition() == i) {
                packet.setEntityId((Integer)parsed);
            } else if (information.getChunkXPosition() != null && information.getChunkXPosition() == i) {
                packet.setChunkX((Integer)parsed);
            } else if (information.getChunkZPosition() != null && information.getChunkZPosition() == i) {
                packet.setChunkZ((Integer)parsed);
            } else if (information.getxPosition() != null && information.getxPosition() == i && information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.DOUBLE)) {
                packet.setBlockX((Double)parsed);
                packet.setChunkX((int) Math.floor(packet.getBlockX() / 16));
            } else if (information.getyPosition() != null && information.getyPosition() == i && information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.DOUBLE)) {
                packet.setBlockY((Double)parsed);
            } else if (information.getzPosition() != null && information.getzPosition() == i && information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.DOUBLE)) {
                packet.setBlockZ((Double) parsed);
                packet.setChunkZ((int) Math.floor(packet.getBlockZ() / 16));
            } else if (information.getxPosition() != null && information.getxPosition() == i && information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.POSITION)) {
                packet.setBlockX((double) ((Position)parsed).getX());
                packet.setBlockY((double) ((Position)parsed).getY());
                packet.setBlockZ((double) ((Position)parsed).getZ());
                packet.setChunkX((int) Math.floor(((Position)parsed).getX() / 16));
                packet.setChunkZ((int) Math.floor(((Position)parsed).getZ() / 16));
            }

        }
    }


}
