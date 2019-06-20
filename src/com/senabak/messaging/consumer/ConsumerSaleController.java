package com.senabak.messaging.consumer;

import com.senabak.messaging.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Return type simplified to be String instead of full response message.
 */
public class ConsumerSaleController {

    private static ConsumerSaleController consumerSaleController;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerSaleController.class);

    private List<Product> productList = ProductDataContainer.get().getProductList();
    private Map<Product, List<Sale>> salesByProduct = ConsumerSalesDataContainer.get().getSalesByProduct();
    private Map<Product, List<SaleAdjustment>> saleAdjustmentsByProduct = ConsumerSalesDataContainer.get().getSalesAdjustmentsByProduct();
    private Map<SaleAdjustmentType, List<SaleAdjustment>> saleAdjustmentsByType = ConsumerSalesDataContainer.get().getSalesAdjustmentsByType();


    public static ConsumerSaleController get(){
        if(consumerSaleController==null){
            consumerSaleController = new ConsumerSaleController();
        }
        return consumerSaleController;
    }

    /**
     *
     * @param message
     * @return true if the message is successfully processed, otherwise false.
     * validation steps can be refactored with case statement with enum parameters
     * and extracted into a separate functions to return boolean value for the validity and the logging message.
     */
    public boolean process(Message message) {

        MessageType messageType = message.getMessageType();

        boolean isValidSaleMessage = (messageType.equals(MessageType.ADD_SALE_SINGLE) || messageType.equals(MessageType.ADD_SALE_MULTI))
                && message.getContent().getClass().equals(Sale.class);

        boolean isValidSaleAdjustmentMessage = messageType.equals(MessageType.ADJUST_ALL_SALES) && message.getContent().getClass().equals(SaleAdjustment.class);

        // validate Sale message
        if(isValidSaleMessage){
            boolean isValid = false; //initial value, only overwritten to true after being passed through all validation rules within the clause.

            Sale s = (Sale) message.getContent();

            if(!productList.contains(s.getProduct())){
                LOGGER.error("The product doesn't exist. The sale message cannot be processed");
            }else if(s.getValue().compareTo(BigDecimal.ZERO) != 1 || s.getQuantity() < 1){
                LOGGER.error("value and quantity should be positive values");
            }else {
                isValid = true;
            }
            // when valid, call addSale and return the output boolean value.
            return isValid ? addSale(s) : false;

        // validate SaleAdjustment message
        } else if(isValidSaleAdjustmentMessage){
            SaleAdjustment saleAdjustment = (SaleAdjustment) message.getContent();
            Product product = saleAdjustment.getProduct();

            SaleAdjustmentType adjustmentType = saleAdjustment.getSaleAdjustmentType();
            BigDecimal adjustmentFactor = saleAdjustment.getAdjustmentFactor();

            // validate whether the product exists
            if(!productList.contains(product)){
                LOGGER.error("The product doesn't exist. The sale message cannot be processed");
                return false;
            // validate whether any sale of the product exists
            } else if(!salesByProduct.containsKey(product)){
                LOGGER.error("There is no sale of the product");
                return false;
            // validate whether the amount to add/subtract/multiply (adjustmentFactor) is not zero
            } else if(adjustmentFactor.compareTo(BigDecimal.ZERO)==0){
                LOGGER.error("Value adjustment factor cannot be 0");
                return false;

            // validate the combination of valueAdjustmentType and the other attributes
            } else {
                // when ADD
                if(adjustmentType.equals(SaleAdjustmentType.ADD)){
                    boolean isValid = false; //initial value, only overwritten to true after being passed through all validation rules within the clause.
                    // value to add should be positive number.
                    if(adjustmentFactor.compareTo(BigDecimal.ZERO)!= 1){
                        LOGGER.error("Value to add should be positive number.");
                    }else {
                        isValid = true;
                    }
                    // when valid, call adjustValueWithFixedAmount
                    return isValid ? adjustValueWithFixedAmount(saleAdjustment) : false;
                // when SUBTRACT
                }else if(adjustmentType.equals(SaleAdjustmentType.SUBTRACT)){
                    boolean isValid = false; //initial value, only overwritten to true after being passed through all validation rules within the clause.

                    BigDecimal minSaleValue
                            = minSaleValueOfProduct(product);

                    if(adjustmentFactor.compareTo(BigDecimal.ZERO) != -1){
                        LOGGER.error("Value to subtract should be negative number.");
                    } else if (saleAdjustment.getAdjustmentFactor().abs().compareTo(minSaleValue) != -1){
                        LOGGER.error("The value to subtract is greater than minimum value in all sales");
                    }else {
                        isValid = true;
                    }
                    // when valid, call adjustValueWithFixedAmount and return the output boolean value.
                    return isValid ? adjustValueWithFixedAmount(saleAdjustment) : false;
                // when MULTIPLY
                }else if(adjustmentType.equals(SaleAdjustmentType.MULTIPLY)){
                    boolean isValid = false; //initial value, only overwritten to true after being passed through all validation rules within the clause.

                    if(saleAdjustment.getAdjustmentFactor().compareTo(BigDecimal.ZERO) != 1){
                        LOGGER.error("Value to multiply should be positive number");
                    } else {
                        isValid = true;
                    }
                    // when valid, call adjustValueByRate and return the output boolean value.
                    return isValid ? adjustValueByRate(saleAdjustment) : false;

                // when valueAdjustmentType is not any of the enum SaleAdjustmentType,
                } else {
                    LOGGER.error("value adjustment type invalid");
                    return false;
                }
            }
        // when messageType is not any of the enum MessageType
        } else {
            LOGGER.error("Message type invalid");
            return false;
        }


        //simpler version
        /*if(isValidSaleMessage){
            Sale s = (Sale) message.getContent();
            return addSale(s);
        }
        if(isValidSaleAdjustmentMessage){
            SaleAdjustment saleAdjustment = (SaleAdjustment) message.getContent();
            if(saleAdjustment.getSaleAdjustmentType().equals(SaleAdjustmentType.ADD) || saleAdjustment.getSaleAdjustmentType().equals(SaleAdjustmentType.SUBTRACT) ){
                return adjustValueWithFixedAmount(saleAdjustment);
            }else if(saleAdjustment.getSaleAdjustmentType().equals(SaleAdjustmentType.MULTIPLY)){
                return adjustValueByRate(saleAdjustment);
            }else {
                return false;
            }

        }
        return false;*/
    }



    /**
     *
     * @param sale is validated before this method is invoked in the method process.
     * @return true is sale was added, otherwise false.
     */
    private boolean addSale(Sale sale) {
         Product product = sale.getProduct();

         if(salesByProduct.keySet().contains(product)){
             salesByProduct.get(product).add(sale);
         }else {
             List<Sale> saleList = new ArrayList<>();
             saleList.add(sale);
             salesByProduct.put(product, saleList);
         }

        LOGGER.info("Sale of " + sale.getQuantity() + " at " + sale.getValue().setScale(2, BigDecimal.ROUND_HALF_DOWN) +  " for " + product.getName() +  " was added");

        return true;

    }

    /**
     * @param saleAdjustment
     * @return true if the adjustment was applied, otherwise false.
     */
    private boolean adjustValueWithFixedAmount(SaleAdjustment saleAdjustment){
        Product product = saleAdjustment.getProduct();
        BigDecimal amount = saleAdjustment.getAdjustmentFactor();

        salesByProduct.get(product).forEach(sale -> sale.setValue(sale.getValue().add(amount)));
        saveSalesAdjustment(saleAdjustment);
        LOGGER.info("All sales of product " + product.getName() + " was adjusted with augend " + saleAdjustment.getAdjustmentFactor().floatValue());//log  "Value of product " + product.getName() + " was adjusted for all sales"
        return true;
    }

    /**
     * @param saleAdjustment
     * @return true if the adjustment was applied, otherwise false.
     */
    private boolean adjustValueByRate(SaleAdjustment saleAdjustment){

        Product product = saleAdjustment.getProduct();
        BigDecimal rate = saleAdjustment.getAdjustmentFactor();

        salesByProduct.get(product).forEach(sale -> sale.setValue(sale.getValue().multiply(rate)));
        saveSalesAdjustment(saleAdjustment);
        LOGGER.info("All sales of product " + product.getName() + " was adjusted with multiplicand " + saleAdjustment.getAdjustmentFactor().floatValue());
        return true;
    }


    private Map<Product, BigDecimal> getTotalSalesValue(){
        Map<Product, BigDecimal> summary = new HashMap<>();
        salesByProduct.entrySet()
                .forEach(e -> {
                    List<Sale> saleList = e.getValue();
                    BigDecimal totalValue = saleList.stream()
                            .map(sale -> sale.getValue().multiply(new BigDecimal(sale.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    summary.put(e.getKey(), totalValue);
                });
        return summary;
    }

    public String getTotalSalesSummary(){
        return getTotalSalesValue().entrySet().stream().map(e->{
            Product p = e.getKey();
            BigDecimal v = e.getValue().setScale(2, BigDecimal.ROUND_HALF_DOWN);
            return p.getName() + " : " + v.toString();
        }).collect( Collectors.joining( ", " ));
    }

    private void saveSalesAdjustment(SaleAdjustment saleAdjustment){
        //save to saleAdjustmentsByProduct
        Product p = saleAdjustment.getProduct();
        if(saleAdjustmentsByProduct.keySet().contains(p)){
            saleAdjustmentsByProduct.get(p).add(saleAdjustment);
        }else {
            List<SaleAdjustment> saleAdjustments = new LinkedList<>();
            saleAdjustments.add(saleAdjustment);
            saleAdjustmentsByProduct.put(p, saleAdjustments);
        }

        //save to saleAdjustmentsByType
        SaleAdjustmentType adjustmentType = saleAdjustment.getSaleAdjustmentType();
        if(saleAdjustmentsByType.keySet().contains(adjustmentType)){
            saleAdjustmentsByType.get(adjustmentType).add(saleAdjustment);
        }else {
            List<SaleAdjustment> saleAdjustments = new LinkedList<>();
            saleAdjustments.add(saleAdjustment);
            saleAdjustmentsByType.put(adjustmentType, saleAdjustments);
        }
    }

    public String getSaleAdjustmentsSummaryByAdjustmentType() {
        return
                saleAdjustmentsByType.entrySet().stream().map(e -> {
                            String adjustmentType = e.getKey().toString() + " : ";
                            List<SaleAdjustment> adjustments = e.getValue();
                            String allAdjustmentsByType
                                    = adjustments.stream()
                                    .map(adjustment -> "All sales of product "
                                            .concat(adjustment.getProduct().getName() + " was adjusted(" + adjustment.getSaleAdjustmentType() + ") by ")
                                            .concat(adjustment.getAdjustmentFactor().toString() + ", ")
                                            .concat("min sale value: " + minSaleValueOfProduct(adjustment.getProduct()).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString())
                                            .concat(", ")
                                            .concat("max sale value: " + maxSaleValueOfProduct(adjustment.getProduct()).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString()))
                                    .collect(Collectors.joining("\n"));
                            return adjustmentType.concat(allAdjustmentsByType);
                        }).collect(Collectors.joining("\n"));
    }

    /**
     *
     * @param product
     * @return minimum BigDecmial value of all existing sales of the product. If there is no sale of the product, then returns null.
     */
    private BigDecimal minSaleValueOfProduct(Product product){
        return salesByProduct.get(product).stream()
                .map(Sale::getValue)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     *
     * @param product
     * @return maximum BigDecmial value of all existing sales of the product. If there is no sale of the product, then returns null.
     */
    private BigDecimal maxSaleValueOfProduct(Product product){
        return salesByProduct.get(product).stream()
                .map(Sale::getValue)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
