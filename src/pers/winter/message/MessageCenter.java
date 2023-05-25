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

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.config.ApplicationConfig;
import pers.winter.config.ConfigManager;
import pers.winter.threadpool.IExecutorHandler;
import pers.winter.threadpool.fair.FairPoolExecutor;
import pers.winter.utils.ClassScanner;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message center of the server, manage all actions about message.
 * @author Winter
 */
public class MessageCenter {
    public static final MessageCenter INSTANCE = new MessageCenter();
    private static final Logger logger = LogManager.getLogger(MessageCenter.class);
    private MessageCenter(){}
    private Map<Class<?>, MessageHandler> messageHandlers = new HashMap<>();
    private IExecutorHandler<AbstractBaseMessage> executorHandler;
    private FairPoolExecutor<AbstractBaseMessage> executor;
    private boolean terminated = false;

    /**
     * Scan all classes in the project and init the handler for every message.
     * Start thread pool.
     * Must be called before the socket server starts.
     * Not thread safe.
     * @throws Exception while creating handler instance failed
     */
    public void start() throws Exception {
        initMessageHandler();
        startExecutor();
    }

    /**
     * Terminate the message center, called when terminate the server.
     * Message center will not receive any new message, but will still complete the messages remains.
     */
    public void terminate() {
        terminated = true;
        executor.terminate(false);
    }
    private void initMessageHandler() throws Exception {
        List<Class<?>> handlerClasses = ClassScanner.getTypesAnnotatedWith(AnnMessageServiceImpl.class);
        for(Class<?> handlerClass:handlerClasses){
            Object handler = null;
            for(Method method:handlerClass.getMethods()){
                if(method.getParameterTypes().length == 1){
                    Class<?> parameterCls = method.getParameterTypes()[0];
                    if(AbstractBaseMessage.class.isAssignableFrom(parameterCls) && parameterCls != AbstractBaseMessage.class){
                        if(handler == null){
                            Constructor<?> defaultConstructor = handlerClass.getConstructor();
                            handler = defaultConstructor.newInstance();
                        }
                        MethodHandle methodHandle = MethodHandles.publicLookup().unreflect(method);
                        messageHandlers.put(parameterCls,new MessageHandler(handler,methodHandle));
                    }
                }
            }
        }
    }
    private void startExecutor(){
        this.executorHandler = new IExecutorHandler<AbstractBaseMessage>() {
            @Override
            public void execute(AbstractBaseMessage task) throws Throwable {
                MessageHandler messageHandler = messageHandlers.get(task.getClass());
                if(messageHandler == null){
                    throw new UnsupportedOperationException("Message handler not found!");
                }
                messageHandler.getMethodHandle().invoke(messageHandler.getService(), task);
            }
            @Override
            public void exceptionCaught(AbstractBaseMessage task, Throwable cause) {
                logger.error("Execute message {} exception! Data: {}.", task.getClass().getSimpleName(), JSON.toJSONString(task),cause);
            }
        };
        short threadCount = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getMessageThreadPoolCount();
        executor = new FairPoolExecutor<>("MessageExecutor",threadCount,this.executorHandler);
        executor.start();
    }

    /**
     * Receive a message, put it into the executor
     * @param message
     */
    public void receiveMessage(AbstractBaseMessage message){
        if(!terminated){
            InetSocketAddress address = (InetSocketAddress) message.getChannel().remoteAddress();
            executor.add(address.getAddress().getHostAddress(),message);
        }
    }
}
