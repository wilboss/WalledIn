package walledin.game;

import walledin.game.entity.Entity;
import walledin.game.entity.behaviors.ItemRenderBehavior;
import walledin.game.entity.behaviors.SpatialBehavior;

public class Item extends Entity implements Cloneable {

	public Item(final String name, final String texPart) {
		super(name);
		
		addBehavior(new SpatialBehavior(this));
		addBehavior(new ItemRenderBehavior(this, texPart));
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	


}