/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package walledin;

/**
 *
 * @author ben
 */
public class Camera {
    private Vector2f pos;
    private Vector2f scale;
    private float rot;

    public Camera() {
        pos = new Vector2f();
        scale = new Vector2f(1.0f, 1.0f);
    }


    public Vector2f getPos() {
        return pos;
    }

    public void setPos(Vector2f pos) {
        this.pos = pos;
    }

    public float getRot() {
        return rot;
    }

    public void setRot(float rot) {
        this.rot = rot;
    }

    public Vector2f getScale() {
        return scale;
    }

    public void setScale(Vector2f scale) {
        this.scale = scale;
    }



}
