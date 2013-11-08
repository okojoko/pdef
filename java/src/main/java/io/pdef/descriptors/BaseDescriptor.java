package io.pdef.descriptors;

import io.pdef.TypeEnum;

/**
 * Descriptor is a base class for Pdef descriptors. Descriptors provides information about
 * a Java type in runtime. For example, a message descriptor allows to explore declared fields,
 * inherited fields, etc.
 * */
public class BaseDescriptor<T> implements Descriptor<T> {
	private final TypeEnum type;
	private final Class<T> javaClass;

	protected BaseDescriptor(final TypeEnum type, final Class<T> javaClass) {
		this.type = type;
		this.javaClass = javaClass;

		if (type == null) throw new NullPointerException("type");
		if (javaClass == null) throw new NullPointerException("javaClass");
	}

	@Override
	public TypeEnum getType() {
		return type;
	}

	@Override
	public Class<T> getJavaClass() {
		return javaClass;
	}

	@Override
	public String toString() {
		return "Descriptor{" + type + "," + javaClass.getSimpleName() + '}';
	}
}