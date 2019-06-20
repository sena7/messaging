package com.senabak.messaging.common;

import java.math.BigDecimal;
import java.util.Objects;

public class SaleAdjustment {
    private SaleAdjustmentType saleAdjustmentType;
    private Product product;
    private BigDecimal adjustmentFactor;

    public SaleAdjustment(SaleAdjustmentType saleAdjustmentType, Product product, BigDecimal adjustmentFactor) {
        //constructor validation
        boolean invalid =
                (SaleAdjustmentType.ADD.equals(saleAdjustmentType) && adjustmentFactor.compareTo(BigDecimal.ZERO) != 1)
                || (SaleAdjustmentType.SUBTRACT.equals(saleAdjustmentType) && adjustmentFactor.compareTo(BigDecimal.ZERO) != -1)
                || (SaleAdjustmentType.MULTIPLY.equals(saleAdjustmentType) && adjustmentFactor.compareTo(BigDecimal.ZERO) != 1);
        if(invalid){
            throw new IllegalArgumentException("saleAdjustmentType and adjustmentFactor combination is invalid");
        }

        this.saleAdjustmentType = saleAdjustmentType;
        this.product = product;
        this.adjustmentFactor = adjustmentFactor;
    }

    public SaleAdjustmentType getSaleAdjustmentType() {
        return saleAdjustmentType;
    }

    public void setSaleAdjustmentType(SaleAdjustmentType saleAdjustmentType) {
        this.saleAdjustmentType = saleAdjustmentType;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getAdjustmentFactor() {
        return adjustmentFactor;
    }

    public void setAdjustmentFactor(BigDecimal adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SaleAdjustment)) return false;
        SaleAdjustment that = (SaleAdjustment) o;
        return getSaleAdjustmentType() == that.getSaleAdjustmentType() &&
                getProduct().equals(that.getProduct()) &&
                getAdjustmentFactor().equals(that.getAdjustmentFactor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSaleAdjustmentType(), getProduct(), getAdjustmentFactor());
    }
}
