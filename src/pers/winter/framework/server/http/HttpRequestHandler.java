package pers.winter.framework.server.http;

import java.lang.invoke.MethodHandle;

/**
 * The reflected handler of http request
 * @author Winter
 */
public class HttpRequestHandler {
    private final Object service;
    private final MethodHandle methodHandle;
    private final HttpServerHandler.HttpRequestMethod requestMethod;

    public HttpRequestHandler(Object service, MethodHandle methodHandle, HttpServerHandler.HttpRequestMethod requestMethod) {
        this.service = service;
        this.methodHandle = methodHandle;
        this.requestMethod = requestMethod;
    }
    public Object getService() {
        return service;
    }

    public MethodHandle getMethodHandle() {
        return methodHandle;
    }

    public HttpServerHandler.HttpRequestMethod getRequestMethod() {
        return requestMethod;
    }
}
