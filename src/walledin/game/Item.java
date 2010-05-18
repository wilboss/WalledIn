package walledin.game;

import walledin.game.entity.Attribute;
import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.ItemRenderBehavior;
import walledin.game.entity.behaviors.SpatialBehavior;
import walledin.math.Rectangle;

public class Item extends Entity implements Cloneable {

	public Item(final String name, final String familyName,
			final String texPart, final Rectangle destRect) {
		super(name, familyName);

		addBehavior(new SpatialBehavior(this));
		addBehavior(new ItemRenderBehavior(this, texPart, destRect));

		setAttribute(Attribute.BOUNDING_RECT, destRect);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

}
