package at.aau.itec.mcpcapparser;

/**
 * The ParsingInformationPacket contains information about how to parse a Minecraft packet. This includes the
 * order of fields in the packet and the data types.
 */
public class ParsingInformation {

    public static enum MCDataTypes {
        VARINT,
        INT,
        DOUBLE,
        UUID,
        ANGLE,
        SHORT,
        STRING,
        BYTE_ENUM,
        BYTE,
        UNSIGNED_BYTE,
        LOCATION,
        FLOAT,
        LONG,
        BOOL
    }

    private MCDataTypes[] parsingOrder;
    private Integer entityIdPosition;
    private Integer chunkXPosition;
    private Integer chunkZPosition;
    private Integer xPosition;
    private Integer zPosition;
    private Integer yPosition;

    public ParsingInformation(MCDataTypes[] parsingOrder, Integer entityIdPosition, Integer chunkXPosition, Integer chunkZPosition, Integer xPosition, Integer zPosition, Integer yPosition) {
        this.parsingOrder = parsingOrder;
        this.entityIdPosition = entityIdPosition;
        this.chunkXPosition = chunkXPosition;
        this.chunkZPosition = chunkZPosition;
        this.xPosition = xPosition;
        this.zPosition = zPosition;
        this.yPosition = yPosition;
    }

    public MCDataTypes[] getParsingOrder() {
        return parsingOrder;
    }

    public void setParsingOrder(MCDataTypes[] parsingOrder) {
        this.parsingOrder = parsingOrder;
    }

    public Integer getEntityIdPosition() {
        return entityIdPosition;
    }

    public void setEntityIdPosition(Integer entityIdPosition) {
        this.entityIdPosition = entityIdPosition;
    }

    public Integer getChunkXPosition() {
        return chunkXPosition;
    }

    public void setChunkXPosition(Integer chunkXPosition) {
        this.chunkXPosition = chunkXPosition;
    }

    public Integer getChunkZPosition() {
        return chunkZPosition;
    }

    public void setChunkZPosition(Integer chunkZPosition) {
        this.chunkZPosition = chunkZPosition;
    }

    public Integer getxPosition() {
        return xPosition;
    }

    public void setxPosition(Integer xPosition) {
        this.xPosition = xPosition;
    }

    public Integer getzPosition() {
        return zPosition;
    }

    public void setzPosition(Integer zPosition) {
        this.zPosition = zPosition;
    }

    public Integer getyPosition() {
        return yPosition;
    }

    public void setyPosition(Integer yPosition) {
        this.yPosition = yPosition;
    }
}
