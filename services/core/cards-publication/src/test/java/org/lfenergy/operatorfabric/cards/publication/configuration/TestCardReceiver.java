/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.lfenergy.operatorfabric.cards.publication.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.operatorfabric.cards.model.CardOperation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p></p>
 * Created on 06/08/18
 *
 * @author David Binder
 */
@Component
@Slf4j
public class TestCardReceiver {

    Queue<CardOperation> adminQueue = new LinkedList<>();
    Queue<CardOperation> tsoQueue = new LinkedList<>();
    Queue<CardOperation> ericQueue = new LinkedList<>();
    private ObjectMapper mapper;

    @Autowired
    public TestCardReceiver(ObjectMapper mapper){
        this.mapper = mapper;
    }

    @RabbitListener(queues = "#{groupQueue.name}")
    public void receiveGroup(Message message) throws IOException {
        String cardString = new String(message.getBody());
        String key = message.getMessageProperties().getReceivedRoutingKey();
        log.info("receiving group card");
        CardOperation card = mapper.readValue(cardString, CardOperation.class);
        if(key.contains("admin"))
            adminQueue.add(card);
        if(key.contains("mytso"))
            tsoQueue.add(card);
    }

    @RabbitListener(queues = "#{userQueue.name}")
    public void receiveUser(Message message) throws IOException {
        String cardString = new String(message.getBody());
        log.info("receiving user card");
        CardOperation card = mapper.readValue(cardString, CardOperation.class);
        ericQueue.add(card);
    }

    public void clear(){
        log.info("clearing data");
        adminQueue.clear();
        tsoQueue.clear();
        ericQueue.clear();
    }

    public Queue<CardOperation> getAdminQueue() {
        return adminQueue;
    }

    public Queue<CardOperation> getTsoQueue() {
        return tsoQueue;
    }

    public Queue<CardOperation> getEricQueue() {
        return ericQueue;
    }
}
