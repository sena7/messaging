package com.senabak.messaging.common;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * model of a sale
 */
public class Sale {

    private Product product;
    private BigDecimal value;
    private int quantity;

    public Sale(Product product, BigDecimal value, int quantity) throws IllegalArgumentException{
        //constructor validation
        if(quantity < 1){
           throw new IllegalArgumentException("quantity should be a positive integer");
        }

        this.product = product;
        this.value = value;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sale)) return false;
        Sale sale = (Sale) o;
        return getQuantity() == sale.getQuantity() &&
                getProduct().equals(sale.getProduct()) &&
                getValue().equals(sale.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProduct(), getValue(), getQuantity());
    }

    @Override
    public String toString() {
        return "Sale{" +
                "product=" + product +
                ", value=" + value +
                ", quantity=" + quantity +
                '}';
    }
}
