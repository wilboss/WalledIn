package walledin.game.network.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.apache.log4j.Logger;

import walledin.game.entity.Entity;
import walledin.game.network.NetworkDataManager;

public class PlayerConnection {
	private static final Logger LOG = Logger.getLogger(PlayerConnection.class);
	private Entity player;
	private final SocketAddress address;

	private final long CHECK_TIME = 1000;
	private final long WAIT_TIME = 1000;
	private long prevTime;
	private boolean waitingForAck;
	private boolean alive;

	public PlayerConnection(SocketAddress address, Entity player) {
		super();
		this.player = player;
		this.address = address;
		
		alive = true;
		prevTime = System.currentTimeMillis();
	}

	public Entity getPlayer() {
		return player;
	}
	
	public SocketAddress getAddress() {
		return address;
	}

	public void isAliveReceived() {
		waitingForAck = false;
	}

	public boolean getAlive() {
		return alive;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	} 
	
	public void remove()
	{
		alive = false;
	}

	public void update(final DatagramChannel channel) {
		final long curTime = System.currentTimeMillis();

		if (!alive)
			return;

		if (waitingForAck && (curTime - prevTime > WAIT_TIME)) {
			LOG.info("Connection lost to client " + address.toString());
			alive = false;
			return;
		}

		if (curTime - prevTime > CHECK_TIME) {
			prevTime = curTime;
			waitingForAck = true;

			try {
				ByteBuffer buffer = ByteBuffer.allocate(6); // FIXME
				buffer.putInt(NetworkDataManager.DATAGRAM_IDENTIFICATION);
				buffer.put(NetworkDataManager.ALIVE_MESSAGE);
				buffer.flip();
				channel.send(buffer, address);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
