package com.senabak.messaging.producer;

import com.senabak.messaging.common.Product;
import com.senabak.messaging.common.Sale;
import com.senabak.messaging.common.SaleAdjustment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simulates Sales database on the message producer's side.
 * implemented with singleton to use the same instance throughout this program.
 */
public class ProducerSalesDataContainer {

    private static ProducerSalesDataContainer producerSalesDataContainer;

    private Map<Product, List<Sale>> productSalesByProduct;

    private Map<Product, List<SaleAdjustment>> productSaleAdjustmentsByProduct;

    private ProducerSalesDataContainer(){
        productSalesByProduct = new HashMap<>();
        productSaleAdjustmentsByProduct = new HashMap<>();
    }

    public static ProducerSalesDataContainer get(){
        if(producerSalesDataContainer ==null){
            producerSalesDataContainer = new ProducerSalesDataContainer();
        }
        return producerSalesDataContainer;
    }

    public Map<Product, List<Sale>> getSalesByProduct(){
        return productSalesByProduct;
    }

    public Map<Product, List<SaleAdjustment>> getSaleAdjustmentsByProduct() {
        return productSaleAdjustmentsByProduct;
    }
}
