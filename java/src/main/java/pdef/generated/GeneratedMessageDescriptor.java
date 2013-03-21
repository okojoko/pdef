package pdef.generated;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import pdef.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public abstract class GeneratedMessageDescriptor extends GeneratedTypeDescriptor
		implements MessageDescriptor, GeneratedDescriptor {
	private final Map<List<TypeDescriptor>, ParameterizedMessageDescriptor> pmap;

	protected GeneratedMessageDescriptor(final Class<?> type) {
		super(type);
		pmap = Maps.newHashMap();
	}

	@Override
	public MessageDescriptor getBase() {
		return null;
	}

	@Nullable
	@Override
	public MessageTree getTree() {
		MessageTree tree = getRootTree();
		return tree != null ? tree : getBaseTree();
	}

	@Nullable
	@Override
	public MessageTree getBaseTree() {
		return null;
	}

	@Nullable
	@Override
	public MessageTree getRootTree() {
		return null;
	}

	@Override
	public SymbolTable<VariableDescriptor> getVariables() {
		return ImmutableSymbolTable.of();
	}

	@Override
	public MessageDescriptor parameterize(final TypeDescriptor... args) {
		checkNotNull(args);
		checkArgument(getVariables().size() == args.length,
				"Wrong number of args for %s: %s", this, args);
		List<TypeDescriptor> argList = ImmutableList.copyOf(args);

		final ParameterizedMessageDescriptor pmessage;
		synchronized (this) {
			if (pmap.containsKey(argList)) {
				pmessage = pmap.get(argList);
			} else {
				pmessage = new ParameterizedMessageDescriptor(this, argList);
				pmap.put(argList, pmessage);
			}
		}

		pmessage.initialize();
		return pmessage;
	}

	@Override
	public MessageDescriptor bind(final Map<VariableDescriptor, TypeDescriptor> argMap) {
		return this;
	}
}
