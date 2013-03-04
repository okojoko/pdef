package pdef.generated;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import pdef.*;

import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class GeneratedMessageDescriptor extends GeneratedTypeDescriptor
		implements MessageDescriptor, GeneratedDescriptor {

	private final Map<List<TypeDescriptor>, ParameterizedMessageDescriptor> pmap;
	private final Queue<ParameterizedMessageDescriptor> pqueue;
	private SymbolTable<FieldDescriptor> fields;

	protected GeneratedMessageDescriptor(final Class<?> type) {
		super(type);
		pmap = Maps.newHashMap();
		pqueue = Lists.newLinkedList();
	}

	@Override
	public MessageDescriptor getBase() {
		return null;
	}

	@Override
	public Enum<?> getBaseType() {
		return null;
	}

	@Override
	public Enum<?> getDefaultType() {
		MessageDescriptor base = getBase();
		return base == null ? null : base.getDefaultType();
	}

	@Override
	public FieldDescriptor getTypeField() {
		MessageDescriptor base = getBase();
		return base == null ? null : base.getTypeField();
	}

	@Override
	public Map<Enum<?>, MessageDescriptor> getSubtypes() {
		MessageDescriptor base = getBase();
		return base == null ? ImmutableMap.<Enum<?>, MessageDescriptor>of() : base.getSubtypes();
	}

	@Override
	public SymbolTable<VariableDescriptor> getVariables() {
		return ImmutableSymbolTable.of();
	}

	@Override
	public SymbolTable<FieldDescriptor> getFields() {
		// Merging two immutable symbol tables and caching the result is thread-safe,
		// because it always produces the same result;
		if (fields != null) {
			return fields;
		}

		MessageDescriptor base = getBase();
		if (base == null) {
			fields = getDeclaredFields();
		} else {
			fields = base.getFields().merge(getDeclaredFields());
		}

		return fields;
	}

	@Override
	public MessageDescriptor parameterize(final TypeDescriptor... args) {
		checkNotNull(args);
		checkArgument(getVariables().size() == args.length,
				"Wrong number of args for %s: %s", this, args);
		List<TypeDescriptor> argList = ImmutableList.copyOf(args);

		synchronized (TypeDescriptor.class) {
			if (pmap.containsKey(argList)) {
				return pmap.get(argList);
			}

			ParameterizedMessageDescriptor pmessage =
					new ParameterizedMessageDescriptor(this, argList);
			pmap.put(argList, pmessage);

			if (isLinked()) {
				pmessage.link();
			} else {
				pqueue.add(pmessage);
			}

			return pmessage;
		}
	}

	@Override
	public MessageDescriptor bind(final Map<VariableDescriptor, TypeDescriptor> argMap) {
		return this;
	}

	@Override
	protected final void doLink() {
		init();
		linkParameterized();
	}

	protected abstract void init();

	private void linkParameterized() {
		for (ParameterizedMessageDescriptor pmessage; (pmessage = pqueue.poll()) != null; ) {
			pmessage.link();
		}
	}
}