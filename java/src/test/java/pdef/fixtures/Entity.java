package pdef.fixtures;

import com.google.common.collect.ImmutableMap;
import pdef.*;
import pdef.generated.GeneratedFieldDescriptor;
import pdef.generated.GeneratedMessage;
import pdef.generated.GeneratedMessageDescriptor;

import java.util.Map;

public class Entity extends GeneratedMessage {
	private static final Entity defaultInstance = new Entity();
	public static Entity getDefaultInstance() {
		return defaultInstance;
	}

	private Id id;

	private Entity() {
		this(new Builder());
	}

	protected Entity(final Builder builder) {
		super(builder);
		this.id = builder.getId();
	}

	public Id getId() {
		if (id == null) {
			return Id.getDefaultInstance();
		}
		return id;
	}

	@Override
	public MessageDescriptor getDescriptor() { return Descriptor.getInstance(); }

	public static class Builder extends GeneratedMessage.Builder {
		private Id id;

		public Id getId() {
			if (id == null) {
				return Id.getDefaultInstance();
			}
			return id;
		}

		public Builder setId(final Id id) { this.id = id; return this; }

		@Override
		public Entity build() { return new Entity(this); }

		@Override
		public MessageDescriptor getDescriptor() { return Descriptor.getInstance(); }
	}

	public static class Descriptor extends GeneratedMessageDescriptor {
		private static final Descriptor instance = new Descriptor();
		public static Descriptor getInstance() { return instance; }

		private Map<Enum<?>, MessageDescriptor> typeMap;
		private SymbolTable<FieldDescriptor> declaredFields;
		private FieldDescriptor idField;

		private Descriptor() { super(Entity.class); }

		@Override
		public Enum<?> getBaseType() { return Type.ENTITY; }

		@Override
		public Map<Enum<?>, MessageDescriptor> getTypeMap() { return typeMap; }

		@Override
		public SymbolTable<FieldDescriptor> getDeclaredFields() { return declaredFields; }

		@Override
		protected void init() {
			idField = new GeneratedFieldDescriptor("id", Id.Descriptor.getInstance()) {
				@Override
				public Object get(final Message message) {
					return ((Entity) message).getId();
				}

				@Override
				public Object get(final Message.Builder builder) {
					return ((Builder) builder).getId();
				}

				@Override
				public void set(final Message.Builder builder, final Object value) {
					((Builder) builder).setId((Id) value);
				}

				@Override
				public void clear(final pdef.Message.Builder builder) {
				}
			};

			typeMap = ImmutableMap.<Enum<?>, MessageDescriptor>of(
					Type.ENTITY, this,
					Type.IMAGE, Image.Descriptor.getInstance(),
					Type.USER, User.Descriptor.getInstance()
			);

			declaredFields = ImmutableSymbolTable.of(idField);
		}

		static {
			instance.link();
		}
	}
}
