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
package pers.winter.message;

import java.lang.invoke.MethodHandle;

/**
 * Message handler for a message.
 * getMethodHandle().invoke(getService(), message) will be called when the server receives a message.
 * @author Winter
 */
public class MessageHandler {
    private final Object service;
    private final MethodHandle methodHandle;
    private final int retryCount;
    public MessageHandler(Object service, MethodHandle methodHandle, int retryCount){
        this.service = service;
        this.methodHandle = methodHandle;
        this.retryCount = retryCount;
    }
    public Object getService(){
        return service;
    }
    public MethodHandle getMethodHandle(){
        return methodHandle;
    }
    public int getRetryCount(){return retryCount;}
}
