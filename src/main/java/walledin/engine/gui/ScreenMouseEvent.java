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
package walledin.engine.gui;

import walledin.engine.math.Vector2f;

public class ScreenMouseEvent {
    /** The screen the mouse is on. */
    private final AbstractScreen screen;
    private final Vector2f pos;

    public ScreenMouseEvent(final AbstractScreen screen, final Vector2f pos) {
        super();
        this.screen = screen;
        this.pos = pos;
    }

    public AbstractScreen getScreen() {
        return screen;
    }

    public Vector2f getPos() {
        return pos;
    }
}
