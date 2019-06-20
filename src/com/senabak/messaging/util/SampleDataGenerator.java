package com.senabak.messaging.util;

import com.senabak.messaging.common.Product;
import com.senabak.messaging.common.Sale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleDataGenerator {

    // arguments validation needs to be added
    public static List<Sale> generateSales(List<Product> products, int numberOfSales, BigDecimal minValue, BigDecimal maxValue, int minQuantity, int maxQuantity) {

        int productsMaxIndex = products.size() - 1;

        List<Sale> sales = new ArrayList<>(numberOfSales); // set the arraylist initial capacity with numberOfSales
        for (int i = 1; i <= numberOfSales; i++) {
            sales.add(new Sale(
                      products.get(generateRandomInt(1, productsMaxIndex))
                    , generateRandomBigDecimal(minValue, maxValue)
                    , generateRandomInt(minQuantity, maxQuantity)));
        }
        return sales;
    }


    public static BigDecimal generateRandomBigDecimal(BigDecimal min, BigDecimal max) {
        return min.add(new BigDecimal(Math.random()).multiply(max.subtract(min)));
        //.setScale(5, BigDecimal.ROUND_HALF_UP)
    }

    public static int generateRandomInt(int min, int max){
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
}


