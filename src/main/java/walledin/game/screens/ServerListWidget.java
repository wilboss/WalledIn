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
package walledin.game.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import walledin.engine.Font;
import walledin.engine.Input;
import walledin.engine.Renderer;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.network.ServerData;
import walledin.game.screens.Screen.ScreenState;
import walledin.game.screens.ScreenManager.ScreenType;

public class ServerListWidget extends Screen {
    Screen refreshButton;
    List<ServerData> serverList; // list of servers
    List<Screen> serverButtons; // list of buttons

    public ServerListWidget(final Screen parent, final Rectangle boudingRect) {
        super(parent, boudingRect);
        serverButtons = new ArrayList<Screen>();
    }

    @Override
    public void initialize() {
        refreshButton = new Button(this, new Rectangle(0, -20, 100, 25),
                "Refresh", getPosition().add(new Vector2f(400, 40)));
        addChild(refreshButton);
    }

    @Override
    public void update(final double delta) {

        /** If clicked on refresh button, get server list */
        if (refreshButton.pointInScreen(Input.getInstance().getMousePos()
                .asVector2f())) {
            if (Input.getInstance().getMouseDown()) {
                serverButtons.clear();

                // request a refresh
                getManager().getClient().refreshServerList();
                Input.getInstance().setMouseUp(); // FIXME
            }
        }

        serverList = new ArrayList<ServerData>(
                getManager().getClient().getServerList());
        
        serverButtons.clear();
        
        for (int i = 0; i < serverList.size(); i++) {
            Screen server = new Button(this,
                    new Rectangle(0, -20, 300, 25), serverList.get(i).getName(),
                    getPosition().add(new Vector2f(10, 65 + i * 20)));
            server.registerScreenManager(getManager());
            serverButtons.add(server);
        }

        for (int i = 0; i < serverButtons.size(); i++) {
            serverButtons.get(i).update(delta);
        }

        // if clicked on server, load the game
        for (int i = 0; i < serverButtons.size(); i++) {
            if (serverButtons.get(i).pointInScreen(
                    Input.getInstance().getMousePos().asVector2f())) {
                if (Input.getInstance().getMouseDown()) {
                    // connect to server
                    getManager().getClient().connectToServer(serverList.get(i));
                    
                    getManager().getScreen(ScreenType.GAME).initialize();
                    getManager().getScreen(ScreenType.GAME).setActive(true);
                    getParent().setState(ScreenState.Hidden); // hide main menu
                    
                    Input.getInstance().setMouseUp(); // FIXME
                }
            }
        }

        super.update(delta);
    }

    @Override
    public void draw(final Renderer renderer) {
        final Font font = getManager().getFont("arial20");
        font.renderText(renderer, "Server Name",
                getPosition().add(new Vector2f(10, 40)));

        for (int i = 0; i < serverButtons.size(); i++)
            serverButtons.get(i).draw(renderer);

        // TODO Auto-generated method stub
        renderer.drawRectOutline(getRectangle().translate(getPosition()));
        super.draw(renderer);
    }

}
