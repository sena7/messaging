package com.senabak.messaging.consumer;

import com.senabak.messaging.common.Message;
import com.senabak.messaging.common.MessageQueueContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;

public class Consumer extends Thread{

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private final MessageQueueContainer messageQueueContainer = MessageQueueContainer.get();

    private final BlockingQueue<Message> queue = messageQueueContainer.getQueue();

    private final BlockingQueue<Message> deadLetter = messageQueueContainer.getDeadLetter();

    private ConsumerSaleController consumerSaleController = ConsumerSaleController.get();


    @Override
    public void run(){
        int totalMessageConsumed = 0;
        while(!Thread.currentThread().isInterrupted()){

            synchronized (queue){

                while(queue.size()==0) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try{
                    Message message ;
                    while((message = queue.poll()) != null){

                        boolean successful = consumerSaleController.process(message);

                        if(successful){
                            totalMessageConsumed += 1;
                            LOGGER.info("total message consumed : " + totalMessageConsumed);
                        }else {
                            deadLetter.add(message);
                            LOGGER.error("message couldn't be consumed");
                        }

                        if(totalMessageConsumed % 10 == 0){
                            LOGGER.info("Sales by product summary: " + consumerSaleController.getTotalSalesSummary());
                        }
                        if(totalMessageConsumed % 50 == 0){
                            sleep(1000); // extract it to a config file or global constant
                            LOGGER.info("Sale adjustments by adjustment type summary: " + consumerSaleController.getSaleAdjustmentsSummaryByAdjustmentType());
                        }
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }


        }



    }
}

