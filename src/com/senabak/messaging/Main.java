package com.senabak.messaging;

import com.senabak.messaging.common.*;
import com.senabak.messaging.consumer.Consumer;
import com.senabak.messaging.consumer.ConsumerSaleController;
import com.senabak.messaging.consumer.ConsumerSalesDataContainer;
import com.senabak.messaging.producer.ProducerSalesDataContainer;
import com.senabak.messaging.producer.ProducerSaleController;
import com.senabak.messaging.util.SampleDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * it does not contain extra checks against input values other than language built-in type checks.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static ProductDataContainer productDataContainer = ProductDataContainer.get();
    private static List<Product> productList = productDataContainer.getProductList();

    private static Map<Product, List<Sale>> producerSalesData = ProducerSalesDataContainer.get().getSalesByProduct();

    // this contains a producer thread to produce messages.
    private static ProducerSaleController producerSaleController = new ProducerSaleController();

    private static ConsumerSaleController consumerSaleController = ConsumerSaleController.get();

    public static void main(String[] args) {

        //consumer thread has consumerSaleController as a member
        Thread consumer = new Consumer();
        consumer.start();

        productList.add(new Product(1L,"chocolate"));
        productList.add(new Product(2L,"apple"));
        productList.add(new Product(3L,"pie"));
        productList.add(new Product(4L,"sausage"));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Program started");
        LOGGER.info("program started");

        int choice;
        boolean isRunning = true;

        while (isRunning) {
            String menu =  "------enter action number----- "
                    .concat("\n1. add a sale ")
                    .concat("\n2. adjust value of all sales of a single product. ")
                    .concat("\n3. show all products. ")
                    .concat("\n4. show all sales in producer database")
                    .concat("\n5. show sale adjustments summary in producer database")
                    .concat("\n6. ADD RANDOM SALES")
                    .concat("\n7. Prove all messages were consumed - confirm consumer and producer sales & adjustments match")
                    .concat("\n8. Test consumer side message validation by adding broken messages - not done");

            System.out.println(menu);
            choice = scanner.nextInt();

            Long productId;
            switch (choice) {
                case 1:
                    System.out.println("select product id");
                    productId = scanner.nextLong();

                    System.out.println("enter the value of the product");
                    BigDecimal value = scanner.nextBigDecimal();

                    System.out.println("enter the quantity of the product");
                    int quantity = scanner.nextInt();

                    System.out.println(producerSaleController.addSale(productDataContainer.getProductById(productId), value, quantity));

                    break;
                case 2:
                    System.out.println("select product id");
                    productId = scanner.nextLong();
                    Product product = productDataContainer.getProductById(productId);
                    boolean saleOfProductExists = producerSalesData.containsKey(product);

                    System.out.println("enter the adjustment type number 1.ADD 2.SUBTRACT 3.MULTIPLY");

                    //implement this in enum itself
                    int adjustmentTypeChoice = scanner.nextInt();
                    SaleAdjustmentType type = null;
                    switch (adjustmentTypeChoice){
                        case 1:
                        type = SaleAdjustmentType.ADD;
                        break;
                        case 2:
                        type = SaleAdjustmentType.SUBTRACT;
                        break;
                        case 3:
                        type = SaleAdjustmentType.MULTIPLY;
                        break;
                    }

                    System.out.println("enter the adjustment factor in number (if SUBTRACT, enter negative value)");
                    BigDecimal adjustmentFactor = scanner.nextBigDecimal();

                    String response = "ERROR: Enter correct adjustment type"; // default message in case adjustment type selection is wrong.
                    if(type!=null){

                        if(type.equals(SaleAdjustmentType.ADD) ||type.equals( SaleAdjustmentType.SUBTRACT)) {
                            response = producerSaleController.adjustValueWithFixedAmount(product, type, adjustmentFactor);
                        }else {
                            response = producerSaleController.adjustValueByRate(product, adjustmentFactor);
                        }

                    }
                    System.out.println(response);
                    break;
                case 3:
                    System.out.println(productList.stream().map(p ->  p.getId() + " " + p.getName()).collect(Collectors.joining("\n")));
                    break;
                case 4:
                    System.out.println(consumerSaleController.getTotalSalesSummary());
                    break;
                case 5:
                    System.out.println(consumerSaleController.getSaleAdjustmentsSummaryByAdjustmentType());
                    break;
                case 6:
                    // all parameters can be changed as you wish
                    System.out.println("enter the number of sales you want to generate.");
                    int numOfSales = scanner.nextInt();
                    // generate sales
                    List<Sale> sales = SampleDataGenerator.generateSales(productList, numOfSales, new BigDecimal(1), new BigDecimal(20), 1, 20);
                    // add to the producer database (and add messages)
                    for(Sale s : sales){
                        producerSaleController.addSale(s);
                    }
                    System.out.println(sales.size() + "SUCCESS: random sales added");
                    break;
                case 7:
                    //compare sales
                    Map<Product, List<Sale>> producerSalesByProduct = ProducerSalesDataContainer.get().getSalesByProduct();
                    Map<Product, List<Sale>> consumerSalesByProduct = ConsumerSalesDataContainer.get().getSalesByProduct();

                    boolean salesByProductMatch = false;
                    if(producerSalesByProduct!=null){
                        salesByProductMatch = producerSalesByProduct.equals(consumerSalesByProduct);
                    }

                    // compare adjustments
                    Map<Product, List<SaleAdjustment>> producerSaleAdjustmentsByProduct = ProducerSalesDataContainer.get().getSaleAdjustmentsByProduct();
                    Map<Product, List<SaleAdjustment>> consumerSaleAdjustmentsByProduct = ConsumerSalesDataContainer.get().getSalesAdjustmentsByProduct();

                    boolean saleAdjustmentsByProductMatch = false;
                    if(producerSaleAdjustmentsByProduct!=null){
                        saleAdjustmentsByProductMatch = producerSaleAdjustmentsByProduct.equals(consumerSaleAdjustmentsByProduct);
                    }

                    boolean salesAndAdjustmentsMatch = salesByProductMatch && saleAdjustmentsByProductMatch;

                    String result = "Consumer and Producer Sales and Adjustments ";
                    System.out.println(salesAndAdjustmentsMatch ? result.concat("MATCH") : result.concat("DO NOT MATCH"));

                    break;
                default:
                    break;

            }

        }
        scanner.close();
    }


}

