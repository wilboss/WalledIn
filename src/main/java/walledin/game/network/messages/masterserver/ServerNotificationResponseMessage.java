package walledin.game.network.messages.masterserver;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.GameMode;
import walledin.game.network.NetworkConstants;
import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.messages.game.MasterServerProtocolMessage;

public class ServerNotificationResponseMessage extends
        MasterServerProtocolMessage {
    private int port;
    private String name;
    private int players;
    private int maxPlayers;
    private GameMode gameMode;

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        throw new IllegalStateException("Not yet implemented");
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(NetworkConstants.MS_DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.SERVER_NOTIFICATION_MESSAGE);
        buffer.putInt(port);
        NetworkMessageWriter.writeStringData(name, buffer);
        buffer.putInt(players);
        buffer.putInt(maxPlayers);
        buffer.putInt(gameMode.ordinal());
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
