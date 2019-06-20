package com.senabak.messaging.common;

import java.util.ArrayList;
import java.util.List;

/**
 * simulates Product database both message producer and consumer refer to.
 * implemented with singleton to use the same instance and the same member productList throughout this program.
 */
public class ProductDataContainer {

    private static volatile ProductDataContainer productDataContainer;

    private List<Product> productList;

    private ProductDataContainer(){
        productList = new ArrayList<>();
    }

    public static ProductDataContainer get(){
        if(productDataContainer==null){
            productDataContainer = new ProductDataContainer();
        }
        return productDataContainer;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public Product getProductById(Long id){
        return productList.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
