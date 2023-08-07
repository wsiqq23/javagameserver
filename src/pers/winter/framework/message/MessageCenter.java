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
package pers.winter.framework.message;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.winter.example.Constants;
import pers.winter.example.session.SessionContainer;
import pers.winter.framework.config.ApplicationConfig;
import pers.winter.framework.config.ConfigManager;
import pers.winter.framework.config.MonitorConfig;
import pers.winter.framework.entity.Transaction;
import pers.winter.framework.monitor.MonitorCenter;
import pers.winter.framework.threadpool.fair.FairPoolExecutor;
import pers.winter.framework.timer.TimerTaskManager;
import pers.winter.message.json.ActionFail;
import pers.winter.framework.threadpool.IExecutorHandler;
import pers.winter.framework.utils.ClassScanner;
import pers.winter.message.json.GenericResponse;
import pers.winter.message.multiroles.login.Handshake;
import pers.winter.monitor.ExecutorError;
import pers.winter.monitor.MessageProcessSlow;
import pers.winter.monitor.MessageTransactionFail;
import pers.winter.monitor.ExecutorQueueOverflow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    private ConcurrentHashMap<Thread,MessageTransaction> transactionPool;
    private TimerTaskManager.RepeatedTimerTask monitorTask;

    /**
     * Scan all classes in the project and init the handler for every message.
     * Start thread pool.
     * Must be called before the socket server starts.
     * Not thread safe.
     * @throws Exception while creating handler instance failed
     */
    public void start() throws Exception {
        initMessageHandler();
        initTransactionPool();
        initMonitor();
        startExecutor();
    }

    /**
     * Terminate the message center, called when terminate the server.
     * Message center will not receive any new message, but will still complete the messages remains.
     */
    public void terminate() {
        terminated = true;
        monitorTask.cancel();
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
                        int retryCount = 0;
                        AnnMessageMethod annMethod = method.getAnnotation(AnnMessageMethod.class);
                        if(annMethod != null){
                            retryCount = annMethod.retryCount();
                        }
                        MethodHandle methodHandle = MethodHandles.publicLookup().unreflect(method);
                        messageHandlers.put(parameterCls,new MessageHandler(handler,methodHandle,retryCount));
                    }
                }
            }
        }
    }
    private void initTransactionPool(){
        short threadCount = ConfigManager.INSTANCE.getConfig(ApplicationConfig.class).getMessageThreadPoolCount();
        transactionPool = new ConcurrentHashMap<>(threadCount);
    }
    private void initMonitor(){
        monitorTask = TimerTaskManager.getInstance().newRepeatedTimeout(new Transaction("MessageCenterMonitor") {
            @Override
            protected void process() {
                long messageInExecutor = executor.getEstimatedTaskCount();
                if(messageInExecutor>ConfigManager.INSTANCE.getConfig(MonitorConfig.class).getMessageQueueOverflowThreshold()){
                    ExecutorQueueOverflow queueOverflow = new ExecutorQueueOverflow();
                    queueOverflow.executorName = executor.getName();
                    queueOverflow.stackedNum = messageInExecutor;
                    queueOverflow.setTime(System.currentTimeMillis());
                    MonitorCenter.INSTANCE.report(queueOverflow);
                }
            }
            @Override
            protected void failed() {}
        },60,60, TimeUnit.SECONDS,0);
    }
    private void startExecutor(){
        this.executorHandler = new IExecutorHandler<AbstractBaseMessage>() {
            @Override
            public void execute(AbstractBaseMessage task) throws Throwable {
                MessageHandler messageHandler = messageHandlers.get(task.getClass());
                if(messageHandler == null){
                    logger.error("Message handler not found for {}", task.getClass().getSimpleName());
                    return;
                }
                MessageTransaction transaction = transactionPool.get(Thread.currentThread());
                if(transaction == null){
                    transaction = new MessageTransaction("MessageExecutor");
                    transactionPool.put(Thread.currentThread(),transaction);
                }
                transaction.message = task;
                transaction.handler = messageHandler;
                transaction.setRetryCount(messageHandler.getRetryCount());
                transaction.run();
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
            if(message.getClass() == Handshake.class){
                InetSocketAddress address = (InetSocketAddress) message.getChannel().remoteAddress();
                executor.add(address.getAddress().getHostAddress(),message);
            } else {
                Attribute<Boolean> verified = message.getChannel().attr(Constants.ATTRIBUTE_KEY_VERIFIED);
                if(verified.get() == null){
                    GenericResponse response = new GenericResponse();
                    response.code = Constants.ResponseCodes.CONNECTION_NOT_VERIFIED.getValue();
                    response.message = "Connection not verified!";
                    message.getChannel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                Attribute<Long> attrUid = message.getChannel().attr(Constants.ATTRIBUTE_KEY_USER_ID);
                if(attrUid.get() != null){
                    message.setSession(SessionContainer.getInstance().getSession(attrUid.get()));
                    executor.add(attrUid.get(),message);
                } else {
                    InetSocketAddress address = (InetSocketAddress) message.getChannel().remoteAddress();
                    executor.add(address.getAddress().getHostAddress(),message);
                }
            }
        }
    }

    private static class MessageTransaction extends Transaction{
        private AbstractBaseMessage message;
        private MessageHandler handler;
        public MessageTransaction(String name) {
            super(name);
        }
        @Override
        protected void process() {
            long st = System.currentTimeMillis();
            try{
                handler.getMethodHandle().invoke(handler.getService(),message);
            } catch (Throwable cause){
                logger.error("Execute message {} exception! Data: {}.", message.getClass().getSimpleName(), JSON.toJSONString(message),cause);
                ExecutorError report = new ExecutorError();
                report.setTime(System.currentTimeMillis());
                report.executorName = MessageCenter.INSTANCE.executor.getName();
                report.taskClass = message.getClass().getSimpleName();
                report.exceptionClass = cause.getClass().getName();
                report.exceptionMessage = cause.getMessage();
                if(cause.getStackTrace() != null){
                    StringBuilder stackTraceBuilder = new StringBuilder();
                    for(int i = 0;i<cause.getStackTrace().length;i++){
                        stackTraceBuilder.append(cause.getStackTrace()[i]);
                        stackTraceBuilder.append("\n");
                    }
                    report.exceptionStackTrace = stackTraceBuilder.toString();
                }
                MonitorCenter.INSTANCE.report(report);
            }
            long duration = System.currentTimeMillis() - st;
            if(duration > ConfigManager.INSTANCE.getConfig(MonitorConfig.class).getMessageProcessSlowThreshold()){
                MessageProcessSlow messageProcessSlow = new MessageProcessSlow();
                messageProcessSlow.duration = duration;
                messageProcessSlow.msgClass = message.getClass().getSimpleName();
                messageProcessSlow.setTime(st);
                MonitorCenter.INSTANCE.report(messageProcessSlow);
            }
        }
        @Override
        protected void failed() {
            if(message.getChannel() != null && message.getChannel().isActive()){
                message.getChannel().writeAndFlush(new ActionFail());
            }
            MessageTransactionFail failRecord = new MessageTransactionFail();
            failRecord.msgClass = message.getClass().getSimpleName();
            failRecord.retryCount = handler.getRetryCount();
            failRecord.setTime(System.currentTimeMillis());
            MonitorCenter.INSTANCE.report(failRecord);
        }
    }
}
