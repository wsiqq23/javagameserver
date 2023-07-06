/*
 * Copyright 2023 Winter Game Server
 *
 * The Winter Game Server licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
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
