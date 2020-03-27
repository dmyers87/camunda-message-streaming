package com.ultimate.workflow.camunda;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class MessageListener {

    @Autowired
    private ProcessEngine camunda;

//    @StreamListener(target = Sink.INPUT,
//            condition="(headers['messageType']?:'')=='RetrievePaymentCommand'")
    @Transactional
    public void handleMessage(String messageJson) {
//        String messageNameFromCustomMapper = "blah-blah-blah";
//        String tenantId = "7CB202FF-890F-4EEE-8F32-4D91AC0EB0D2";
//        String businessInstanceKeyFromMessage = "some-business-key-from-message";
    }

}
