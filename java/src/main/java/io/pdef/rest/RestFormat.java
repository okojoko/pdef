package io.pdef.rest;

import io.pdef.Message;
import io.pdef.descriptors.*;
import io.pdef.format.JsonFormat;
import io.pdef.format.NativeFormat;
import io.pdef.invoke.Invocation;
import io.pdef.invoke.InvocationResult;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class RestFormat {
	public static final String CHARSET_NAME = "UTF-8";
	private final NativeFormat nativeFormat = NativeFormat.instance();
	private final JsonFormat jsonFormat = JsonFormat.instance();

	// Invocation serialization.

	/** Converts an invocation into a rest request. */
	public RestRequest serializeInvocation(final Invocation invocation) {
		boolean isPost = invocation.getMethod().isPost();
		RestRequest request = isPost ? RestRequest.post() : RestRequest.get();

		for (Invocation inv : invocation.toChain()) {
			serializeSingleInvocation(request, inv);
		}

		return request;
	}

	/** Adds a single invocation to a rest request. */
	// VisibleForTesting
	@SuppressWarnings("unchecked")
	void serializeSingleInvocation(final RestRequest request, final Invocation invocation) {
		if (request == null) throw new NullPointerException("request");
		if (invocation == null) throw new NullPointerException("invocation");

		MethodDescriptor<?, ?> method = invocation.getMethod();
		if (method.isIndex()) {
			request.appendPath("/");
		} else {
			request.appendPath("/" + urlencode(method.getName()));
		}

		boolean isPost = method.isPost();
		boolean isRemote = method.isRemote();

		Object[] args = invocation.getArgs();
		List<ArgumentDescriptor<?>> argds = invocation.getMethod().getArgs();

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			ArgumentDescriptor argd = argds.get(i);

			if (isPost) {
				// Serialize an argument as a post param.
				// Post methods are remote.
				serializeParam(argd, arg, request.getPost());

			} else if (isRemote) {
					// Serialize an argument as a query param.
					serializeParam(argd, arg, request.getQuery());

			} else {
				request.appendPath("/" + serializePathArgument(argd, arg));
			}
		}
	}

	/** Serializes a positional arg and urlencodes it. */
	// VisibleForTesting
	<V> String serializePathArgument(final ArgumentDescriptor<V> argd, final V arg) {
		String serialized = serializeToString(argd.getType(), arg);
		return urlencode(serialized);
	}

	/** Serializes a query/post argument and puts it into a dst map. */
	// VisibleForTesting
	<V> void serializeParam(final ArgumentDescriptor<V> argd, final V arg,
			final Map<String, String> dst) {
		if (arg == null) {
			return;
		}

		DataDescriptor<V> descriptor = argd.getType();
		boolean isForm = (descriptor instanceof MessageDescriptor)
				&& ((MessageDescriptor<?>) descriptor).isForm();

		if (!isForm) {
			// Serialize as a single string param.
			String serialized = serializeToString(descriptor, arg);
			dst.put(argd.getName(), serialized);

		} else {
			// It's a form, serialize each its field into a string param.
			// Mind polymorphic messages.

			Message message = (Message) arg;
			@SuppressWarnings("unchecked")
			MessageDescriptor<Message> mdescriptor =
					(MessageDescriptor<Message>) message.descriptor(); // Polymorphic.

			for (FieldDescriptor<? super Message, ?> field : mdescriptor.getFields()) {
				String serialized = serializeFormField(field, message);
				if (serialized == null) {
					// Ignore null messages fields.
					continue;
				}

				dst.put(field.getName(), serialized);
			}
		}
	}

	@Nullable
	private <M, V> String serializeFormField(final FieldDescriptor<M, V> field, final M message) {
		V value = field.get(message);
		if (value == null) {
			return null;
		}

		return serializeToString(field.getType(), value);
	}

	/** Serializes primitives and enums to strings and other types to json. */
	// VisibleForTesting
	<V> String serializeToString(final DataDescriptor<V> descriptor, final V arg) {
		TypeEnum typeEnum = descriptor.getType();

		if (arg == null) {
			return "";
		} else if (typeEnum.isPrimitive()) {
			return arg.toString();
		} else if (typeEnum.isEnum()) {
			return arg.toString().toLowerCase();
		} else {
			return jsonFormat.serialize(arg, descriptor, false);
		}
	}

	// InvocationResult parsing.

	public <T, E> InvocationResult parseInvocationResult(
			final RestResponse response, final DataDescriptor<T> datad,
			@Nullable final DataDescriptor<E> excd) {
		if (response == null) throw new NullPointerException("response");
		if (datad == null) throw new NullPointerException("datad");

		MessageDescriptor <RestResult<T, E>> descriptor = resultDescriptor(datad, excd);
		RestResult<T, E> result = jsonFormat.parse(response.getContent(), descriptor);

		if (result.isSuccess()) {
			// It's a successful result.
			return InvocationResult.ok(result.getData());

		} else {
			// It's an expected application exception.
			E exc = result.getExc();
			if (excd != null) {
				return InvocationResult.exc((RuntimeException) exc);
			}

			String s = exc == null ? null : exc.toString();
			throw RestException.badRequest("Server returned an unknown exception: " + s);
		}
	}

	// Invocation parsing.

	public Invocation parseInvocation(final RestRequest request, InterfaceDescriptor<?> descriptor)
			throws Exception {
		if (request == null) throw new NullPointerException("request");
		if (descriptor == null) throw new NullPointerException("descriptor");

		String path = request.getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		// Split the path into a list of parts (method names and positions arguments).
		String[] partsArray = path.split("/", -1); // -1 disables discarding trailing empty
		// strings.
		LinkedList<String> parts = new LinkedList<String>();
		Collections.addAll(parts, partsArray);

		// Parse the parts as method invocations.
		Invocation invocation = Invocation.root();
		while (!parts.isEmpty()) {
			String part = parts.removeFirst();

			// Find a method by name .
			MethodDescriptor<?, ?> method = descriptor.findMethod(part);
			if (method == null) {
				// Try to get an index method, if it is not found by a name.
				method = descriptor.getIndexMethod();
			}

			if (method == null) {
				// Method is not found.
				throw RestException.methodNotFound("Method is not found: " + part);
			}

			if (method.isIndex() && !part.equals("")) {
				// It's an index method, and the part does not equal
				// the method name. Prepend the part back, it's an argument.
				parts.addFirst(part);
			}

			if (method.isPost() && !request.isPost()) {
				// The method requires a POST HTTP request.
				throw RestException.methodNotAllowed("Method not allowed, POST required");
			}

			// Parse method arguments.
			boolean isPost = method.isPost();
			boolean isRemote = method.isRemote();

			List<Object> args = new ArrayList<Object>();
			Map<String, String> post = request.getPost();
			Map<String, String> query = request.getQuery();

			for (ArgumentDescriptor<?> argd : method.getArgs()) {
				if (isPost) {
					// Parse a post param.
					args.add(parseParam(argd, post));

				} else if (isRemote) {
					// Parse a query param.
					args.add(parseParam(argd, query));

				} else {
					// Parse a path argument.
					if (parts.isEmpty()) {
						throw RestException.methodNotFound("Wrong number of method args");
					}

					args.add(parsePathArgument(argd, parts.removeFirst()));
				}
			}

			// Create a next invocation in a chain with the parsed arguments.
			invocation = invocation.next(method, args.toArray());

			if (method.isRemote()) {
				// It's the last method which returns a data type.
				// Stop parsing.

				if (!parts.isEmpty()) {
					// Cannot have any more parts here, bad url.
					throw RestException.methodNotFound("Method not found");
				}

				return invocation;
			}

			// It's an interface method.
			// Get the next interface and proceed parsing the parts.
			descriptor = (InterfaceDescriptor<?>) method.getResult();
		}

		// The parts are empty, and we failed to parse a remote method invocation.
		throw RestException.methodNotFound("Method not found");
	}

	// VisibleForTesting
	<V> V parsePathArgument(final ArgumentDescriptor<V> argd, final String s) {
		String value = urldecode(s);
		return parseFromString(argd.getType(), value);
	}

	// VisibleForTesting
	<V> V parseParam(final ArgumentDescriptor<V> argd, final Map<String, String> src) {
		DataDescriptor<V> descriptor = argd.getType();
		boolean isForm = descriptor instanceof MessageDescriptor
				&& ((MessageDescriptor) descriptor).isForm();

		if (!isForm) {
			// Parse as a single string format.
			String serialized = src.get(argd.getName());
			return parseFromString(descriptor, serialized);

		} else {
			// It's a form. Parse each its field.
			// Mind polymorphic messages.

			MessageDescriptor<Message> mdescriptor = (MessageDescriptor<Message>) descriptor;
			if (mdescriptor.isPolymorphic()) {
				FieldDescriptor<? super Message, ?> discriminator = mdescriptor.getDiscriminator();
				String serialized = src.get(discriminator.getName());

				if (serialized != null) {
					Object value = parseFromString(discriminator.getType(), serialized);
					mdescriptor = mdescriptor.findSubtypeOrThis(value);
				}
			}

			Message message = mdescriptor.newInstance();
			for (FieldDescriptor<? super Message, ?> field : mdescriptor.getFields()) {
				String serialized = src.get(field.getName());
				parseFormField(message, field, serialized);
			}

			@SuppressWarnings("unchecked")
			V result = (V) message;
			return result;
		}
	}

	private <M, V> void parseFormField(final M message, final FieldDescriptor<M, V> field,
			final String serialized) {
		V value = parseFromString(field.getType(), serialized);
		if (value == null) {
			// Skip null fields.
			return;
		}

		field.set(message, value);
	}

	// VisibleForTesting
	<V> V parseFromString(final DataDescriptor<V> descriptor, final String value) {
		TypeEnum typeEnum = descriptor.getType();

		if (value == null) {
			return null;

		} else if (value.equals("")) {
			return null;

		} else if (typeEnum.isPrimitive() || typeEnum.isEnum()) {
			// Native format supports parsing from string.
			return nativeFormat.parse(value, descriptor);

		} else {
			// Parse messages and collections from a JSON string.
			return jsonFormat.parse(value, descriptor);
		}
	}

	// InvocationResult serialization.

	public <T, E> RestResponse serializeInvocationResult(final InvocationResult result,
			final DataDescriptor<T> dataDescriptor,
			@Nullable final DataDescriptor<E> excDescriptor) {
		if (result == null) throw new NullPointerException("result");
		if (dataDescriptor == null) throw new NullPointerException("dataDescriptor");

		MessageDescriptor <RestResult<T, E>> descriptor = resultDescriptor(dataDescriptor,
				excDescriptor);

		T data = dataDescriptor.cast(result.getData());
		E exc = excDescriptor == null ? null : excDescriptor.cast(result.getExc());

		RestResult<T, E> restResult = descriptor.newInstance()
				.setSuccess(result.isOk())
				.setData(data)
				.setExc(exc);

		String content = jsonFormat.serialize(restResult, descriptor, true);
		return new RestResponse()
				.setOkStatus()
				.setContent(content)
				.setJsonContentType();
	}

	static <T, E> MessageDescriptor<RestResult<T, E>> resultDescriptor(
			final DataDescriptor<T> dataDescriptor, final DataDescriptor<E> excDescriptor) {
		return RestResult.runtimeDescriptor(dataDescriptor, excDescriptor);
	}

	/** Url-encodes a string. */
	static String urlencode(final String s) {
		try {
			return URLEncoder.encode(s, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/** Url-decodes a string. */
	static String urldecode(final String s) {
		try {
			return URLDecoder.decode(s, CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		Integer i = Integer.class.cast(null);
		System.out.println(i);
	}
}
