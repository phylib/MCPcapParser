package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.pkts.PacketHandler;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.util.HashMap;

/**
 * This handler extracts TCP packets from a PCAP file and creates a <code>ClientConnection</code> for each
 * TCP stream (IP-Adresse, Port combination).
 *
 * @see ClientConnection
 */
public class MinecraftPacketHandler implements PacketHandler {

    private static final String SERVER_IP = "143.205.122.57";
    private static final int SERVER_PORT = 25565;

    private String outputFolder;

    private HashMap<String, ClientConnection> clients = new HashMap<>();

    public MinecraftPacketHandler(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public boolean nextPacket(Packet packet) throws IOException {

        if (packet.hasProtocol(Protocol.TCP)) {

            TCPPacket tcpPacket = (TCPPacket) packet.getPacket(Protocol.TCP);

            Buffer buffer = tcpPacket.getPayload();
            //if (buffer != null && tcpPacket.getSourceIP().equals("143.205.122.57")) {
            if (buffer != null) {
                byte[] payload = new byte[buffer.getReadableBytes()];
                buffer.getByes(payload);
                ByteBuf payloadBuffer = Unpooled.wrappedBuffer(payload);

                long arrivalTime = packet.getArrivalTime();

                String serverIp;
                String clientIp;
                int clientPort;
                boolean serverbound = true;
                if (tcpPacket.getSourceIP().equals(SERVER_IP) && tcpPacket.getSourcePort() == SERVER_PORT) {
                    serverbound = false;
                    serverIp = tcpPacket.getSourceIP();
                    clientIp = tcpPacket.getDestinationIP();
                    clientPort = tcpPacket.getDestinationPort();
                } else {
                    serverbound = true;
                    serverIp = tcpPacket.getDestinationIP();
                    clientIp = tcpPacket.getSourceIP();
                    clientPort = tcpPacket.getSourcePort();
                }

                String tcpIdentifier = clientIp + "-" + clientPort;

                if (!clients.containsKey(tcpIdentifier)) {
                    clients.put(tcpIdentifier, new ClientConnection(clientIp, tcpIdentifier, outputFolder));
//                    System.out.println("Found new client, " + clientIp);
                }

                clients.get(tcpIdentifier).addPacket(arrivalTime, serverbound, payloadBuffer, tcpPacket.getSequenceNumber());

//                payloadBuffer.resetReaderIndex();
//                MinecraftPacket p = null;
//                if (type == 0x27) {
//                    p = new Entity();
//                } else if (type == 0x23) {
//                    p = new Effect();
//                } else {
//                    p = new MinecraftPacket();
//                }
//                p.parsePacket(p, payloadBuffer);
//
//                packets.add(p);
//
//                System.out.println(String.format("%4x", type));
//                System.out.println(p.toString());

            }
        }
        return true;
    }

    public void printStats() {
        System.out.println("# Found " + clients.size() + " clients");
        for (String client : clients.keySet()) {
            ClientConnection clientConnection = clients.get(client);
            System.out.println("Client " + client + "; #Serverbound: " + clientConnection.getServerbound().size()
                    + "; #Clientbound: " + clientConnection.getClientbound().size());
        }

        try {
//            clients.get("143.205.122.219-33138").parsePackets();
            for (ClientConnection clientConnection : clients.values()) {
                clientConnection.parsePackets();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
