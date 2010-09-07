package walledin.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import walledin.engine.math.Geometry;
import walledin.engine.math.Rectangle;
import walledin.engine.math.Vector2f;
import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;

public class QuadTree {
    /** Logger. */
    private static final Logger LOG = Logger.getLogger(QuadTree.class);

    /** Maximum depth of the tree. */
    public static final int MAX_DEPTH = 5;
    /**
     * Maximum objects per leaf if the depth is less than the maximum depth.
     */
    public static final int MAX_OBJECTS = 6;

    private final QuadTree[] children;
    private final List<Entity> objects;
    private final Rectangle rectangle;
    private int depth;
    private boolean leaf;

    public QuadTree(Rectangle rect) {
        this(rect, 0);
    }

    private QuadTree(Rectangle rect, int depth) {
        children = new QuadTree[4];
        objects = new ArrayList<Entity>();
        rectangle = rect;
        leaf = true;
        this.depth = depth;
    }

    private void addObject(Entity object) {
        objects.add(object);

        if (objects.size() > MAX_OBJECTS) {
            subdivide();
        }
    }

    private void removeObject(Entity object) {
        objects.remove(object);
    }

    private boolean containsFully(Entity object) {
        Rectangle rect = ((Geometry) object
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate((Vector2f) object.getAttribute(Attribute.POSITION));

        return rectangle.containsFully(rect);
    }

    private boolean containsFully(Rectangle rect) {
        return rectangle.containsFully(rect);
    }

    public void add(Entity object) {
        Rectangle rect = ((Geometry) object
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate((Vector2f) object.getAttribute(Attribute.POSITION));

        QuadTree tree = getSmallestQuadTreeContainingRectangle(rect);

        if (tree == null) {
            LOG.warn("Could not add object to the quadtree, because it is out of the bounds.");
        } else {
            addObject(object);
        }
    }

    private QuadTree getSmallestQuadTreeContainingRectangle(Rectangle rect) {
        /* If this is a leaf and it does not contain the object, return null. */
        for (int i = 0; i < 4; i++) {
            if (children[i].containsFully(rect)) {
                QuadTree tree = getSmallestQuadTreeContainingRectangle(rect);

                if (tree != null) {
                    return tree;
                }
            }
        }

        if (containsFully(rect)) {
            return this;
        }

        return null;
    }

    public QuadTree getQuadTreeContainingObject(Entity object) {
        Rectangle rect = ((Geometry) object
                .getAttribute(Attribute.BOUNDING_GEOMETRY)).asRectangle()
                .translate((Vector2f) object.getAttribute(Attribute.POSITION));

        if (objects.contains(object)) {
            return this;
        }

        /* If this is a leaf and it does not contain the object, return null. */
        if (leaf) {
            return null;
        }

        for (int i = 0; i < 4; i++) {
            if (children[i].containsFully(rect)) {
                QuadTree tree = getQuadTreeContainingObject(object);

                if (tree != null) {
                    return tree;
                }
            }
        }

        return null;
    }

    public List<Entity> getObjectsFromRectangle(Rectangle rect) {
        List<Entity> objectList = new ArrayList<Entity>();

        if (rectangle.containsFully(rect)) {
            objectList.addAll(getObjects());
        }

        if (!leaf) {
            for (int i = 0; i < 4; i++) {
                if (children[i].containsFully(rect)) {
                    List<Entity> tree = getObjectsFromRectangle(rect);

                    if (tree != null) {
                        objectList.addAll(tree);
                        return objectList;
                    }
                }
            }
        }

        return objectList;
    }

    public List<Entity> getObjects() {
        return objects;
    }

    public void remove(Entity object) {
        QuadTree tree = getQuadTreeContainingObject(object);

        if (tree != null) {
            tree.removeObject(object);
        }
    }

    /**
     * Subdivides this leaf into four rectangles.
     */
    private void subdivide() {
        if (!leaf || depth + 1 >= MAX_DEPTH) {
            return;
        }

        leaf = false;

        Vector2f size = new Vector2f(rectangle.getWidth() / 2.0f,
                rectangle.getHeight() / 2.0f);
        Vector2f middle = rectangle.getLeftTop().add(size);

        children[0] = new QuadTree(new Rectangle(rectangle.getLeft(),
                rectangle.getTop(), size.getX(), size.getY()), depth + 1);
        children[1] = new QuadTree(new Rectangle(rectangle.getLeft(),
                middle.getY(), size.getX(), size.getY()), depth + 1);
        children[2] = new QuadTree(new Rectangle(middle.getX(), middle.getY(),
                size.getX(), size.getY()), depth + 1);
        children[3] = new QuadTree(new Rectangle(middle.getX(),
                rectangle.getTop(), size.getX(), size.getY()), depth + 1);

        Iterator<Entity> it = objects.iterator();
        while (it.hasNext()) {
            Entity object = it.next();
            for (int i = 0; i < 4; i++) {
                if (children[i].containsFully(object)) {
                    children[i].addObject(object);
                    it.remove();
                }
            }
        }
    }
}