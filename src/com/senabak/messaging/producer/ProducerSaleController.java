package com.senabak.messaging.producer;

import com.senabak.messaging.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * All public methods return simplified string responses to user actions.
 */
public class ProducerSaleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerSaleController.class);

    private BlockingQueue<Message> queue = MessageQueueContainer.get().getQueue();

    private List<Product> products = ProductDataContainer.get().getProductList();
    private Map<Product, List<Sale>> salesByProduct = ProducerSalesDataContainer.get().getSalesByProduct();
    private Map<Product, List<SaleAdjustment>> saleAdjustmentsByProduct = ProducerSalesDataContainer.get().getSaleAdjustmentsByProduct();
    private Producer producer = new Producer();

    public ProducerSaleController(){
        producer.start();
    }

    /**
     *
     * @param sale
     * this is only for controlled input that is always valid - used in the option 6 in Main.
     */
    public void addSale(Sale sale){
        Product p = sale.getProduct();

        if(salesByProduct.keySet().contains(p)){
            salesByProduct.get(p).add(sale);
        }else {
            List<Sale> saleList = new ArrayList<>();
            saleList.add(sale);
            salesByProduct.put(p, saleList);
        }

        int quantity = sale.getQuantity();
        // add message to the queue
        MessageType type = (quantity == 1) ? MessageType.ADD_SALE_SINGLE : MessageType.ADD_SALE_MULTI;
        Message m = new Message(type, sale);

        producer.put(m); //add the message to the queue;
    }

    /**
     *
     * @param product
     * @param value
     * @param quantity
     * @return String response; if successful, SUCCESS message. Otherwise, ERROR message.
     */
    public String addSale(Product product, BigDecimal value, int quantity){

         // handle invalid parameters
         if(!products.contains(product)){
             return "ERROR: product doesn't exist";
         }
         if(value.compareTo(BigDecimal.ZERO) != 1 || quantity <= 0){
             return "ERROR: value and quantity should be positive values";
         }

         Sale s = new Sale(product, value, quantity);

         if(salesByProduct.keySet().contains(product)){
             salesByProduct.get(product).add(s);
         }else {
             List<Sale> saleList = new ArrayList<>();
             saleList.add(s);
             salesByProduct.put(product, saleList);
         }

         // construct message
         MessageType type = (quantity == 1) ? MessageType.ADD_SALE_SINGLE : MessageType.ADD_SALE_MULTI;
         Message m = new Message(type, s);

         producer.put(m); //add the message to the queue;
         return "SUCCESS: Sale added";

    }

    public String adjustValueWithFixedAmount(Product product, SaleAdjustmentType type, BigDecimal amount){

        // handle invalid parameters
        if(!products.contains(product)){
            return "ERROR: product doesn't exist";
        }
        if(!salesByProduct.containsKey(product)) {
            return "ERROR: there is no sale of the product";
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0){
            return "ERROR: Price Adjustment Amount cannot be 0";
        }
        if(type.equals(SaleAdjustmentType.ADD)){
            if(amount.compareTo(BigDecimal.ZERO) != 1){
                return "ERROR: amount to ADD should be greater than 0";
            }
        }else if(type.equals(SaleAdjustmentType.SUBTRACT)){
            if(amount.compareTo(BigDecimal.ZERO)!= -1){
                return "ERROR: amount to SUBTRACT should be less than 0";
            }
        }else if(type.equals(SaleAdjustmentType.MULTIPLY)){
            if(amount.compareTo(BigDecimal.ZERO) != 1){
                return "ERROR: amount to MULTIPLY should be greater than 0";
            }
        } // invalid SaleAdjustmentType is prevented in the main method.


        // if any of the value in Sale record is to be equal to or less than 0 after adjustment,
        // the value should not be adjusted.
        // the existence of any sale of the product is validated above
        BigDecimal minSalesValueOfProduct = salesByProduct.get(product).stream()
                .map(Sale::getValue)
                .min(Comparator.naturalOrder())
                .orElse(null); // this is copy of ConsumerSaleController.minSaleValueOfProduct - better be in util.

        boolean adjustedValueMayNotPositive = minSalesValueOfProduct.add(amount).compareTo(BigDecimal.ZERO)!=1; //minSalesValueOfProduct will be always some value, not null.

        if(adjustedValueMayNotPositive){
            return "ERROR : potential adjusted value of any sale should be greater than 0";
        }else{
            // adjust sales
            salesByProduct.get(product).forEach(sale -> sale.setValue(sale.getValue().add(amount)));

            // construct a SaleAdjustment instance
            SaleAdjustment saleAdjustment = new SaleAdjustment(type, product, amount);

            // add to saleAdjustments
            saveSaleAdjustment(saleAdjustment);

            // add message
            Message m = new Message(MessageType.ADJUST_ALL_SALES, saleAdjustment);
            producer.put(m);//add the message to the queue

            return "SUCCESS : Value of product " + product.getName() + " was adjusted for all sales by" + saleAdjustment.getAdjustmentFactor().toString();
        }


    }

    /**
     * @param product
     * @param rate is greater than 0
     * @return String response to the caller
     */
    public String adjustValueByRate(Product product, BigDecimal rate){

        // handle invalid parameters
        if(!products.contains(product)){
            return "ERROR: product doesn't exist";
        }
        if(rate.compareTo(BigDecimal.ZERO) !=1){
            return "ERROR: Price Adjustment Rate should be greater than 0";
        }

        if(salesByProduct.keySet().contains(product)){
            salesByProduct.get(product).forEach(sale -> sale.setValue(sale.getValue().multiply(rate)));
            SaleAdjustment saleAdjustment = new SaleAdjustment(SaleAdjustmentType.MULTIPLY, product, rate);

            // add to saleAdjustments
            saveSaleAdjustment(saleAdjustment);

            // add message
            Message m = new Message(MessageType.ADJUST_ALL_SALES, saleAdjustment);
            producer.put(m);//add the message to the queue

            return "SUCCESS: " + product.getName() + " value Adjusted for all sales";

        }else {
            return "ERROR: There is no sale of the product " + product.getName();
        }
    }

    //duplicate of ConsumerSaleController.saveSaleAdjustment
    private void saveSaleAdjustment(SaleAdjustment saleAdjustment){
        //save to SaleAdjustmentsByProduct
        Product p = saleAdjustment.getProduct();
        if(saleAdjustmentsByProduct.keySet().contains(p)){
            saleAdjustmentsByProduct.get(p).add(saleAdjustment);
        }else {
            List<SaleAdjustment> saleAdjustments = new LinkedList<>();
            saleAdjustments.add(saleAdjustment);
            saleAdjustmentsByProduct.put(p, saleAdjustments);
        }

    }
}
