package com.senabak.messaging.common;

/**
 *  model of a message.
 */
public class Message {
    private MessageType messageType;
    private Object content;

    //constructor
    public Message(MessageType type, Object obj){

        boolean isSale = ( type.equals(MessageType.ADD_SALE_SINGLE) || type.equals(MessageType.ADD_SALE_MULTI) )
                         && obj.getClass().equals(Sale.class);
        boolean isSaleAdjustment = type.equals(MessageType.ADJUST_ALL_SALES) && obj.getClass().equals(SaleAdjustment.class);

        if(!(isSale || isSaleAdjustment)){
            throw new IllegalArgumentException("Message type and content does not correspond.");
        }

        this.messageType = type;
        this.content = isSale ? (Sale) obj : (SaleAdjustment) obj;
    }

    //getter and setter
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}

