package walledin.game.entity;

import java.util.HashMap;
import java.util.Map;

import walledin.game.components.Component;

public class Entity {
	private final Map<Class<? extends Component>, Component> components;
	private final String name;

	public Entity(String name) {
		components = new HashMap<Class<? extends Component>, Component>();
		this.name = name;
	}

	public void addComponent(Component component) {
		Class<? extends Component> clazz = component.getClass();
		if (components.containsKey(clazz)) {
			throw new IllegalArgumentException("Entity [" + toString()
					+ "] already contains Component of class: "
					+ component.getClass().getName());
		}
		component.setOwner(this);
		components.put(clazz, component);
	}

	public <T extends Component> T get(Class<T> clazz) { // Class<? extends Component> 
		return (T) components.get(clazz);
	}

	public Component removeComponent(Class<? extends Component> clazz) {
		Component component = components.remove(clazz);
		component.detachFromOwner();
		return component;
	}
}
