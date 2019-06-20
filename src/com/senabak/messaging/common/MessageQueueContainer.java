package com.senabak.messaging.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * simulates a Message Broker.
 * implemented with singleton to use the same instance and the same member queue throughout this program.
 */
public class MessageQueueContainer {

    private static volatile MessageQueueContainer messageQueueContainer;

    private BlockingQueue<Message> queue;
    private BlockingQueue<Message> deadLetter;

    private MessageQueueContainer(){
        queue = new LinkedBlockingDeque<>();
        deadLetter = new LinkedBlockingDeque<>();
    }

    public static MessageQueueContainer get(){
        if (messageQueueContainer==null){
            messageQueueContainer = new MessageQueueContainer();
        }
        return messageQueueContainer;
    }

    public BlockingQueue<Message> getQueue(){
        return this.queue;
    }
    public BlockingQueue<Message> getDeadLetter(){
        return this.deadLetter;
    }

}
