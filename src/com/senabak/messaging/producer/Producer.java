package com.senabak.messaging.producer;

import com.senabak.messaging.common.Message;
import com.senabak.messaging.common.MessageQueueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

class Producer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    private final MessageQueueContainer messageQueueContainer = MessageQueueContainer.get();

    private final BlockingQueue<Message> queue = messageQueueContainer.getQueue();

    synchronized void put(Message message) {
        synchronized (queue){
            try {
                queue.put(message);
                LOGGER.info("message added");
                queue.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

