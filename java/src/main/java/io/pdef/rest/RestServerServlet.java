package io.pdef.rest;

import io.pdef.Func;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class RestServerServlet extends HttpServlet {
	private final Func<RestRequest, RestResponse> server;

	RestServerServlet(final Func<RestRequest, RestResponse> server) {
		if (server == null) throw new NullPointerException("server");
		this.server = server;
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		doService(req, resp);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		doService(req, resp);
	}

	private void doService(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
		if (request == null) throw new NullPointerException("request");
		if (response == null) throw new NullPointerException("response");

		RestRequest req = parseRequest(request);
		RestResponse resp = server.apply(req);
		writeResponse(resp, response);
	}

	// VisibleForTesting
	RestRequest parseRequest(final HttpServletRequest request) {
		String method = request.getMethod();

		// In servlets we cannot distinguish between query and post params,
		// so we use the same map for both. It is safe because Pdef REST protocol
		// always uses only one of them.
		String path = parsePath(request);
		Map<String, String> params = parseParams(request);
		return new RestRequest()
				.setMethod(method)
				.setPath(path)
				.setQuery(params)
				.setPost(params);
	}

	private String parsePath(final HttpServletRequest request) {
		String servletPath = request.getServletPath();
		String pathInfo = request.getPathInfo();

		servletPath = servletPath != null ? servletPath : "";
		pathInfo = pathInfo != null ? pathInfo : "";
		return servletPath + pathInfo;
	}

	private Map<String, String> parseParams(final HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();

		@SuppressWarnings("unchecked")
		Map<String, String[]> map = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			if (values == null || values.length == 0) {
				continue;
			}

			String value = values[0];
			params.put(key, value);
		}

		return params;
	}

	// VisibleForTesting
	void writeResponse(final RestResponse resp, final HttpServletResponse response)
			throws IOException {
		response.setStatus(resp.getStatus());
		response.setContentType(resp.getContentType());
		response.getWriter().print(resp.getContent());
	}
}
