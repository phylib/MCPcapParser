package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.Charset;

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

    public static String parseString(ByteBuf buffer) throws IOException {
        int stringLen = ByteBufUtils.readVarInt(buffer);
        String str = buffer.readCharSequence(stringLen, Charset.defaultCharset()).toString();
        return str;
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
                case LOCATION:
                    parsed = decodePosition(buffer);
                    break;
                case BYTE:
                    parsed = buffer.readByte();
                    break;
                case STRING:
                    parsed = parseString(buffer);
                    break;
                case FLOAT:
                    parsed = buffer.readFloat();
                    break;
                case LONG:
                    parsed = buffer.readLong();
                    break;
                case BOOL:
                    parsed = buffer.readByte();
                    break;
                case EFFECT_POSTION:
                    int pos = buffer.readInt();
                    parsed = pos / 8.;
                    break;
            }
            if (information.getEntityIdPosition() != null && information.getEntityIdPosition() == i) {
                packet.setEntityId((Integer) parsed);
            } else if (information.getChunkXPosition() != null && information.getChunkXPosition() == i) {
                packet.setChunkX((Integer)parsed);
            } else if (information.getChunkZPosition() != null && information.getChunkZPosition() == i) {
                packet.setChunkZ((Integer)parsed);
            } else if (information.getxPosition() != null && information.getxPosition() == i && !information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.LOCATION)) {
                Double value = valueToDouble(parsed);
                packet.setBlockX(value);
                packet.setChunkX((int) Math.floor(packet.getBlockX() / 16));
            } else if (information.getyPosition() != null && information.getyPosition() == i && !information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.LOCATION)) {
                Double value = valueToDouble(parsed);
                packet.setBlockY(value);
            } else if (information.getzPosition() != null && information.getzPosition() == i && !information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.LOCATION)) {
                Double value = valueToDouble(parsed);
                packet.setBlockZ(value);
                packet.setChunkZ((int) Math.floor(packet.getBlockZ() / 16));
            } else if (information.getxPosition() != null && information.getxPosition() == i && information.getParsingOrder()[i].equals(ParsingInformation.MCDataTypes.LOCATION)) {
                packet.setBlockX((double) ((Position)parsed).getX());
                packet.setBlockY((double) ((Position)parsed).getY());
                packet.setBlockZ((double) ((Position)parsed).getZ());
                packet.setChunkX((int) Math.floor(((Position)parsed).getX() / 16));
                packet.setChunkZ((int) Math.floor(((Position)parsed).getZ() / 16));
            } else if (information.getObjectType() != null && information.getObjectType() == i) {
                String objectType = byteObjectTypeToString((Byte) parsed);
                packet.setEntityType(objectType);
            }

        }
    }

    private static Double valueToDouble(Object parsed) {
        Double value = null;
        if (parsed instanceof Double) {
            value = (Double) parsed;
        } else if (parsed instanceof Float) {
            value = ((Float) parsed).doubleValue();
        } else if (parsed instanceof Integer) {
            value = ((Integer) parsed).doubleValue();
        }
        return value;
    }


    private static String byteObjectTypeToString(byte objectType) {
        switch (objectType) {
            case 1:
                return "Boat";
            case 2:
                return "Item";
            case 3:
                return "Cloud";
            case 10:
                return "Minecart";
            case 50:
                return "TNT";
            case 51:
                return "EnderCrystal";
            case 60:
                return "Arrow (projectile)";
            case 61:
                return "Snowball (projectile)";
            case 62:
                return "Egg (projectile)";
            case 63:
                return "Fireball (projectile)";
            case 64:
                return "FireCharge (projectile)";
            case 65:
                return "EnderPearle (projectile)";
            case 66:
                return "WitherSkull (projectile)";
            case 67:
                return "ShulkerBullet (projectile)";
            case 70:
                return "FallingObject";
            case 71:
                return "ItemFrames";
            case 72:
                return "EyeOfEnder";
            case 73:
                return "Potion (projectile)";
            case 75:
                return "ThrownExpBottle (projectile)";
            case 76:
                return "FireworkRocket (projectile)";
            case 77:
                return "LeashKnot";
            case 78:
                return "ArmorStand";
            case 79:
                return "EvocationFangs";
            case 90:
                return "FishingHook";
            case 91:
                return "SpectralArrow (projectile)";
            case 93:
                return "DragonFireboall (projectile)";
            case 94:
                return "Trident";
        }
        return "Object";
    }


}
