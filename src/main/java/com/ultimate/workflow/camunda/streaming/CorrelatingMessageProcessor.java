package com.ultimate.workflow.camunda.streaming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binding.StreamListenerMessageHandler;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

//@EnableBinding(Processor.class)
public class CorrelatingMessageProcessor {

//    @Autowired
//    private CorrelatingMessageListener listener;
//
    @Autowired
    @Qualifier("integrationMessageHandlerMethodFactory")
    private MessageHandlerMethodFactory messageHandlerMethodFactory;

    @Autowired
    private ApplicationContext applicationContext;

    //@StreamListener
    public void receive(@Input(Processor.INPUT) SubscribableChannel input,
                        @Output(Processor.OUTPUT) final MessageChannel output) {

        // helpful: https://github.com/spring-cloud/spring-cloud-stream/blob/e9baeea5a9c4a7464c8ea4f5a369f184c28b801b/spring-cloud-stream/src/main/java/org/springframework/cloud/stream/binding/StreamListenerAnnotationBeanPostProcessor.java

        try {
//        InvocableHandlerMethod invocableHandlerMethod = this.messageHandlerMethodFactory
//                .createInvocableHandlerMethod(
//                        CorrelatingMessageListener.class,
//                        ReflectionUtils.findMethod(CorrelatingMessageListener.class, "handleMessage", String.class));
//

            input.subscribe(
                    buildStreamListener(CorrelatingMessageListener.class, "handleMessage", String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StreamListenerMessageHandler buildStreamListener(
            Class<?> handlerClass,
            String handlerMethodName,
            Class<?>... parameters) throws Exception {

        Method m = ReflectionUtils.findMethod(handlerClass, handlerMethodName, parameters);
        InvocableHandlerMethod method = new InvocableHandlerMethod(this, m);
        HandlerMethodArgumentResolverComposite resolver = new HandlerMethodArgumentResolverComposite();
        CompositeMessageConverterFactory factory = new CompositeMessageConverterFactory();
        resolver.addResolver(new PayloadArgumentResolver(
                factory.getMessageConverterForAllRegistered()));
        method.setMessageMethodArgumentResolvers(resolver);
        Constructor<?> c = ReflectionUtils.accessibleConstructor(
                StreamListenerMessageHandler.class, InvocableHandlerMethod.class,
                boolean.class, String[].class);
        StreamListenerMessageHandler handler = (StreamListenerMessageHandler) c
                .newInstance(method, false, new String[] {});
//        handler.setOutputChannelName(channelName);
        handler.setBeanFactory(this.applicationContext);
        handler.afterPropertiesSet();
//		context.refresh();
        return handler;
    }

}
