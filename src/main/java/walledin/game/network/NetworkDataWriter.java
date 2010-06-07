/*  Copyright 2010 Ben Ruijl, Wouter Smeenk

This file is part of Walled In.

Walled In is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3, or (at your option)
any later version.

Walled In is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Walled In; see the file LICENSE.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

 */
package walledin.game.network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import walledin.engine.math.Vector2f;
import walledin.game.EntityManager;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.map.Tile;
import walledin.game.network.server.ChangeSet;

/**
 * Writes network messages
 * 
 * All the methods with send prefix write a complete message and reset the
 * buffer. Methods with write prefix write part of a message and assume there is
 * enough room in the buffer.
 * 
 * @author Wouter Smeenk
 * 
 */
public class NetworkDataWriter {
    private static final Logger LOG = Logger.getLogger(NetworkDataWriter.class);
    private final ByteBuffer buffer;

    public NetworkDataWriter() {
        buffer = ByteBuffer.allocate(NetworkConstants.BUFFER_SIZE);
    }

    public void sendGamestateMessage(final DatagramChannel channel,
            final SocketAddress address, final EntityManager entityManager,
            final ChangeSet changeSet, final int knownClientVersion,
            final int currentVersion) throws IOException {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE);
        buffer.putInt(knownClientVersion);
        buffer.putInt(currentVersion);
        for (final String name : changeSet.getRemoved()) {
            buffer.put(NetworkConstants.GAMESTATE_MESSAGE_REMOVE_ENTITY);
            writeStringData(name, buffer);
        }

        for (final Entry<String, Family> entry : changeSet.getCreated()
                .entrySet()) {
            buffer.put(NetworkConstants.GAMESTATE_MESSAGE_CREATE_ENTITY);
            // write name of entity
            writeStringData(entry.getKey(), buffer);
            // write family of entity
            writeStringData(entry.getValue().toString(), buffer);
        }
        for (final Entry<String, Set<Attribute>> entry : changeSet.getUpdated()
                .entrySet()) {
            final Entity entity = entityManager.get(entry.getKey());

            writeAttributesData(entity, entry.getValue(), buffer);
        }
        // write end
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE_END);
        buffer.flip();
        buffer.rewind();
        channel.send(buffer, address);
    }

    public void sendInputMessage(final DatagramChannel channel,
            final int version, final Set<Integer> keysDown) throws IOException {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.INPUT_MESSAGE);
        buffer.putInt(version);
        buffer.putShort((short) keysDown.size());
        for (final int key : keysDown) {
            buffer.putShort((short) key);
        }
        buffer.flip();
        channel.write(buffer);
    }

    public void sendLoginMessage(final DatagramChannel channel,
            final String username) throws IOException {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGIN_MESSAGE);
        buffer.putInt(username.length());
        buffer.put(username.getBytes());
        buffer.flip();
        channel.write(buffer);
    }

    public void sendLogoutMessage(final DatagramChannel channel)
            throws IOException {
        buffer.clear();
        buffer.putInt(NetworkConstants.DATAGRAM_IDENTIFICATION);
        buffer.put(NetworkConstants.LOGOUT_MESSAGE);
        buffer.flip();
        channel.write(buffer);
    }

    private void writeAttributeData(final Attribute attribute,
            final Object data, final ByteBuffer buffer) {
        // Write attribute identification
        buffer.putShort((short) attribute.ordinal());
        switch (attribute) {
        case HEIGHT:
            writeIntegerData((Integer) data, buffer);
            break;
        case WIDTH:
            writeIntegerData((Integer) data, buffer);
            break;
        case HEALTH:
            writeIntegerData((Integer) data, buffer);
            break;
        case ORIENTATION:
            writeIntegerData((Integer) data, buffer);
            break;
        case PLAYER_NAME:
            writeStringData((String) data, buffer);
            break;
        case ITEM_LIST:
            writeItemsData((List<Entity>) data, buffer);
            break;
        case TILES:
            writeTilesData((List<Tile>) data, buffer);
            break;
        case POSITION:
            writeVector2fData((Vector2f) data, buffer);
            break;
        case VELOCITY:
            writeVector2fData((Vector2f) data, buffer);
            break;
        default:
            LOG.error("Could not process attribute " + attribute);
            break;
        }
    }

    private void writeAttributesData(final Entity entity,
            final Set<Attribute> attributes, final ByteBuffer buffer) {
        buffer.put(NetworkConstants.GAMESTATE_MESSAGE_ATTRIBUTES);
        writeStringData(entity.getName(), buffer);
        final Map<Attribute, Object> values = entity.getAttributes(attributes);
        buffer.putInt(attributes.size());
        for (final Map.Entry<Attribute, Object> entry : values.entrySet()) {
            writeAttributeData(entry.getKey(), entry.getValue(), buffer);
        }
    }

    private void writeIntegerData(final int data, final ByteBuffer buffer) {
        buffer.putInt(data);
    }

    private void writeItemsData(final List<Entity> data, final ByteBuffer buffer) {
        buffer.putInt(data.size());
        for (final Entity item : data) {
            writeStringData(item.getName(), buffer);
        }
    }

    private void writeStringData(final String data, final ByteBuffer buffer) {
        buffer.putInt(data.length());
        buffer.put(data.getBytes());
    }

    private void writeTilesData(final List<Tile> data, final ByteBuffer buffer) {
        buffer.putInt(data.size());
        for (final Tile tile : data) {
            buffer.putInt(tile.getX());
            buffer.putInt(tile.getY());
            buffer.putInt(tile.getType().ordinal());
        }
    }

    private void writeVector2fData(final Vector2f data, final ByteBuffer buffer) {
        buffer.putFloat(data.x);
        buffer.putFloat(data.y);
    }
}