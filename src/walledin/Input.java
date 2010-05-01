/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package walledin;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ben
 */
public class Input implements KeyListener {

    public enum KeyState {

        Up,
        Down
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // quick way out
            System.exit(0);
        }

        mKeyStates.put(e.getKeyCode(), KeyState.Down);
    }

    public void keyReleased(KeyEvent e) {
        mKeyStates.put(e.getKeyCode(), KeyState.Up);
    }

    public boolean keyDown(int nKey) {
        if (!mKeyStates.containsKey(nKey)) {
            return false;
        }

        return mKeyStates.get(nKey) == KeyState.Down;
    }

    /**
     * Flag the key nKey as being up.
     * @param nKey The keycode of the key
     */
    public void setKeyUp(int nKey)
    {
        mKeyStates.put(nKey, KeyState.Up);
    }

    private Input() {
        mKeyStates = new HashMap<Integer, KeyState>();
    }

    public static Input getInstance() {
        if (ref == null)
            ref = new Input();

        return ref;
    }

    private static Input ref = null;
    private Map<Integer, KeyState> mKeyStates;
}
