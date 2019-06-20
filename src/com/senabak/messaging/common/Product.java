package com.senabak.messaging.common;

import java.util.Objects;

/**
 * contains minimal members.
 * product in general would come with a value attribute.
 * However, in this project, only the value of sales for certain product is captured.
 */
public class Product {
    private Long id;
    private String name;

    //constructor
    public Product(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    //getter and setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return getId().equals(product.getId()) &&
                getName().equals(product.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
