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
package walledin.game.network.messages.game;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import walledin.game.network.NetworkEventListener;
import walledin.game.network.NetworkMessageReader;
import walledin.game.network.NetworkMessageWriter;
import walledin.game.network.server.ChangeSet;

public class GamestateMessage extends AbstractGameMessage {
    private ChangeSet changeSet;
    private int newVersion;

    public GamestateMessage() {

    }

    /**
     * Creates a gamestate message.
     * 
     * @param changeSet
     *            This changeset to send
     * @param newVersion
     *            The version it is updating to
     */
    public GamestateMessage(final ChangeSet changeSet, final int newVersion) {
        this.changeSet = changeSet;
        this.newVersion = newVersion;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public int getNewVersion() {
        return newVersion;
    }

    @Override
    public void read(final ByteBuffer buffer, final SocketAddress address) {
        newVersion = buffer.getInt();
        changeSet = NetworkMessageReader.readChangeSet(buffer);
    }

    @Override
    public void write(final ByteBuffer buffer) {
        buffer.putInt(newVersion);
        NetworkMessageWriter.writeChangeSet(changeSet, buffer);
    }

    @Override
    public void fireEvent(final NetworkEventListener listener,
            final SocketAddress address) {
        listener.receivedMessage(address, this);
    }
}
