package pdef.fixtures;

import pdef.ImmutableSymbolTable;
import pdef.PdefMessage;
import pdef.SymbolTable;
import pdef.descriptors.*;

public class Node<T> implements PdefMessage {
	private RootNode<T> root;
	private T element;

	public RootNode<T> getRoot() {
		return root;
	}

	public void setRoot(final RootNode<T> root) {
		this.root = root;
	}

	public T getElement() {
		return element;
	}

	public void setElement(final T element) {
		this.element = element;
	}

	@Override
	public MessageDescriptor getPdefDescriptor() {
		return Descriptor.getInstance();
	}

	public static class Descriptor extends BaseMessageDescriptor {
		private static final Descriptor INSTANCE = new Descriptor();

		public static Descriptor getInstance() {
			INSTANCE.link();
			return INSTANCE;
		}

		private final VariableDescriptor var0;
		private final SymbolTable<VariableDescriptor> variables;
		private SymbolTable<FieldDescriptor> declaredFields;

		Descriptor() {
			super(Node.class);

			var0 = new BaseVariableDescriptor("T");
			variables = ImmutableSymbolTable.of(var0);
		}

		@Override
		public SymbolTable<VariableDescriptor> getVariables() {
			return variables;
		}

		@Override
		public SymbolTable<FieldDescriptor> getDeclaredFields() {
			return declaredFields;
		}

		@Override
		protected void init() {
			declaredFields = ImmutableSymbolTable.<FieldDescriptor>of(
					new BaseFieldDescriptor("root",
							RootNode.Descriptor.getInstance().parameterize(var0)) {
						@Override
						public Object get(final PdefMessage message) {
							return ((Node) message).getRoot();
						}

						@Override
						@SuppressWarnings("unchecked")
						public void set(final PdefMessage message, final Object value) {
							((Node) message).setRoot((RootNode) value);
						}
					},

					new BaseFieldDescriptor("element", var0) {

						@Override
						public Object get(final PdefMessage message) {
							return ((Node) message).getElement();
						}

						@Override
						@SuppressWarnings("unchecked")
						public void set(final PdefMessage message, final Object value) {
							((Node) message).setElement(value);
						}
					}
			);
		}
	}
}