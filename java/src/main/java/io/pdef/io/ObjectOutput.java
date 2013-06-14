package io.pdef.io;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class ObjectOutput implements Output {
	private Object value;

	public Object toObject() {
		return value;
	}

	@Override
	public void write(final boolean v) {
		this.value = v;
	}

	@Override
	public void write(final short v) {
		this.value = v;
	}

	@Override
	public void write(final int v) {
		this.value = v;
	}

	@Override
	public void write(final long v) {
		this.value = v;
	}

	@Override
	public void write(final float v) {
		this.value = v;
	}

	@Override
	public void write(final double v) {
		this.value = v;
	}

	@Override
	public void write(final String v) {
		this.value = v;
	}

	@Override
	public <T> void write(final T object, final Writer<T> writer) {
		ObjectOutput output = new ObjectOutput();
		writer.write(object, output);
		this.value = output.toObject();
	}

	@Override
	public <T> void write(final List<T> list, final Writer.ListWriter<T> writer) {
		ListOutput output = new ListOutput();
		writer.write(list, output);
		this.value = output.toObject();
	}

	@Override
	public <T> void write(final T message, final Writer.MessageWriter<T> writer) {
		MessageOutput output = new MessageOutput();
		writer.write(message, output);
		this.value = output.toObject();
	}

	static class ListOutput implements Output.ListOutput {
		private List<Object> value = Lists.newArrayList();

		List<Object> toObject() {
			return value;
		}

		@Override
		public <T> void write(final List<T> list, final Writer<T> elementWriter) {
			ObjectOutput elementOutput = new ObjectOutput();

			for (T element : list) {
				elementWriter.write(element, elementOutput);
				value.add(elementOutput.toObject());
				elementOutput.value = null;
			}
		}
	}

	static class MessageOutput implements Output.MessageOutput {
		private Map<String, Object> map = Maps.newLinkedHashMap();
		private ObjectOutput fieldOut; // Reusable field output.

		Map<String, Object> toObject() {
			return map;
		}

		@Override
		public <T> void write(final String field, final T value, final Writer<T> writer) {
			if (fieldOut == null) fieldOut = new ObjectOutput();
			fieldOut.value = null;

			writer.write(value, fieldOut);
			Object fieldValue = fieldOut.toObject();

			map.put(field, fieldValue);
		}
	}
}
