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
package walledin.game.entity.behaviors.logic;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.apache.log4j.Logger;

import walledin.engine.math.AbstractGeometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.engine.physics.PhysicsManager;
import walledin.game.CollisionInformation;
import walledin.game.EntityManager;
import walledin.game.entity.AbstractBehavior;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.Family;
import walledin.game.entity.MessageType;
import walledin.util.Utils;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

public class WeaponBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(WeaponBehavior.class);
    /** The mass of the weapon. */
    private final float bulletAccelerationConstant;
    private final Vector2f bulletStartPositionRight = new Vector2f(50.0f, 10.0f);
    private final Vector2f bulletStartPositionLeft = new Vector2f(-30.0f, 10.0f);

    private final int fireLag;
    private final Family bulletFamily;
    private boolean canShoot;
    private int lastShot; // frame of last shot

    public WeaponBehavior(final Entity owner, final int fireLag,
            final Family bulletFamily) {
        super(owner);
        this.fireLag = fireLag;
        lastShot = fireLag;
        canShoot = true;
        this.bulletFamily = bulletFamily;
        bulletAccelerationConstant = 600.0f;

        setAttribute(Attribute.PICKED_UP, Boolean.FALSE);
        setAttribute(Attribute.ORIENTATION_ANGLE, Float.valueOf(0));
    }

    public WeaponBehavior(final Entity owner, final int fireLag,
            final float bulletAcceleration, final Family bulletFamily) {
        super(owner);
        this.fireLag = fireLag;
        lastShot = fireLag;
        canShoot = true;
        this.bulletFamily = bulletFamily;
        bulletAccelerationConstant = bulletAcceleration;

        setAttribute(Attribute.PICKED_UP, Boolean.FALSE);
        setAttribute(Attribute.ORIENTATION_ANGLE, Float.valueOf(0));
    }

    @Override
    public final void onMessage(final MessageType messageType, final Object data) {
        if (messageType == MessageType.ATTRIBUTE_SET
                && (Attribute) data == Attribute.PICKED_UP) {
            if ((Boolean) getAttribute(Attribute.PICKED_UP)) {
                /**
                 * The weapon cannot collide anymore when it is attached to the
                 * player. The physics are also removed.
                 */
                PhysicsManager.getInstance().addToRemoveQueue(
                        getOwner().getName());
                getOwner().removeBehavior(PhysicsBehavior.class);
            } else {
                /* FIXME: do this differently. */

                final Vector2f pos = (Vector2f) getAttribute(Attribute.POSITION);
                final Rectangle destRect = ((AbstractGeometry) getAttribute(Attribute.BOUNDING_GEOMETRY))
                        .asRectangle();

                CollisionShape shape = new BoxShape(new Vector3f(
                        (float) (destRect.getWidth() / 2.0f), (float) (destRect
                                .getHeight() / 2.0f), 2));
                DefaultMotionState state = new DefaultMotionState(
                        new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1),
                                new Vector3f(pos.getX() + destRect.getWidth()
                                        / 2.0f, pos.getY()
                                        + destRect.getHeight() / 2.0f, 0), 1)));
                float mass = 1.0f;
                Vector3f inertia = new Vector3f();
                shape.calculateLocalInertia(mass, inertia);
                RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(
                        mass, state, shape, inertia);
                RigidBody rb = new RigidBody(fallRigidBodyCI);
                rb.setUserPointer(getOwner().getName());
                rb.setLinearFactor(new Vector3f(1, 1, 0));
                rb.setAngularFactor(new Vector3f(0, 0, 1));
                PhysicsManager
                        .getInstance()
                        .getWorld()
                        .addRigidBody(
                                rb,
                                CollisionInformation.WEAPON,
                                (short) (CollisionInformation.PLAYER | CollisionInformation.TILE));

                getOwner().addBehavior(new PhysicsBehavior(getOwner(), rb));
            }
        }

        if (messageType == MessageType.PICK_UP) {
            setAttribute(Attribute.OWNED_BY, ((Entity) data).getName());
            setAttribute(Attribute.PICKED_UP, Boolean.TRUE);
        }

        if (messageType == MessageType.DROP) { // to be called by Player only
            LOG.info("Weapon " + getOwner().getName() + " dropped.");
            setAttribute(Attribute.PICKED_UP, Boolean.FALSE);
        }

        if (messageType == MessageType.SHOOT) {
            if (canShoot) {
                final Entity player = (Entity) data;

                final boolean facingRight = Utils.getCircleHalf((Float) player
                        .getAttribute(Attribute.ORIENTATION_ANGLE)) == 1;
                final Vector2f playerPos = (Vector2f) player
                        .getAttribute(Attribute.POSITION);

                final Vector2f bulletPosition;

                // slightly more complicated, since the player pos is defined as
                // the top left
                if (facingRight) {
                    bulletPosition = playerPos.add(bulletStartPositionRight);
                } else {
                    bulletPosition = playerPos.add(bulletStartPositionLeft);
                }

                final Vector2f target = (Vector2f) player
                        .getAttribute(Attribute.CURSOR_POS);
                final Vector2f bulletAcceleration = target.sub(bulletPosition)
                        .scaleTo(bulletAccelerationConstant);

                final EntityManager manager = getEntityManager();
                final Entity bullet = manager.create(bulletFamily);

                bullet.setAttribute(Attribute.POSITION, bulletPosition);
                bullet.setAttribute(Attribute.TARGET, target);
                bullet.setAttribute(Attribute.ORIENTATION_ANGLE, (float) Math
                        .atan2(target.getY() - bulletPosition.getY(), target
                                .getX()
                                - bulletPosition.getX()));
                bullet.setAttribute(Attribute.OWNED_BY,
                        getAttribute(Attribute.OWNED_BY));
                bullet.sendMessage(MessageType.APPLY_FORCE, bulletAcceleration);

                canShoot = false;
                lastShot = fireLag;
            }
        }
    }

    @Override
    public final void onUpdate(final double delta) {
        if (!canShoot) {
            lastShot--;

            if (lastShot <= 0) {
                canShoot = true;
            }
        }

    }

}
