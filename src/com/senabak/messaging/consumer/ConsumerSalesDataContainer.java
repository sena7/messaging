package com.senabak.messaging.consumer;

import com.senabak.messaging.common.Product;
import com.senabak.messaging.common.Sale;
import com.senabak.messaging.common.SaleAdjustment;
import com.senabak.messaging.common.SaleAdjustmentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simulates Sales database on the message producer's side.
 * implemented with singleton to use the same instance throughout this program.
 */
public class ConsumerSalesDataContainer {

    private static ConsumerSalesDataContainer consumerSalesDataContainer;

    private Map<Product, List<Sale>> salesByProduct;

    private Map<Product, List<SaleAdjustment>> salesAdjustmentsByProduct;

    private Map<SaleAdjustmentType, List<SaleAdjustment>> salesAdjustmentsByType;

    private ConsumerSalesDataContainer(){
        salesByProduct = new HashMap<>();
        salesAdjustmentsByProduct = new HashMap<>();
        salesAdjustmentsByType = new HashMap<>();
    }

    public static ConsumerSalesDataContainer get(){
        if(consumerSalesDataContainer ==null){
            consumerSalesDataContainer = new ConsumerSalesDataContainer();
        }
        return consumerSalesDataContainer;
    }

    public Map<Product, List<Sale>> getSalesByProduct(){
        return salesByProduct;
    }

    public Map<Product, List<SaleAdjustment>> getSalesAdjustmentsByProduct() {
        return salesAdjustmentsByProduct;
    }

    public Map<SaleAdjustmentType, List<SaleAdjustment>> getSalesAdjustmentsByType() {
        return salesAdjustmentsByType;
    }
}
