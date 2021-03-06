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
package walledin.game.map;

import java.net.URL;
import java.util.List;

import walledin.game.EntityManager;
import walledin.game.entity.Entity;

/**
 * Reads or writes a map from or to a certain format. The details of the format
 * are taken care of by the classes that implement this interface.
 * 
 * @author ben
 */
public interface GameMapIO {
    /**
     * Reads a map from a file. Called by the server.
     * 
     * @param entityManager
     *            Entity manager
     * @param file
     *            URL of map file
     * @return Map entity
     */
    Entity readFromURL(EntityManager entityManager, URL file);

    /**
     * Reads the tiles from a map file. Called by the client.
     * 
     * @param file
     *            File to read from
     * @return List of tiles
     */
    List<Tile> readTilesFromURL(final URL file);

    boolean writeToFile(final Entity map, final String filename);
}
