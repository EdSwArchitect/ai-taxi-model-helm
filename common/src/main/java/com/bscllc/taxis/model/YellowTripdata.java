package com.bscllc.taxis.model;

import java.time.LocalDateTime;

/**
 * Model class representing yellow taxi trip data from NYC taxi dataset.
 * Based on the schema from yellow_tripdata.parquet file.
 */
public class YellowTripdata {
    
    private Integer vendorId;
    private LocalDateTime tpepPickupDatetime;
    private LocalDateTime tpepDropoffDatetime;
    private Integer passengerCount;
    private Double tripDistance;
    private Integer ratecodeId;
    private String storeAndFwdFlag;
    private Integer puLocationId;
    private Integer doLocationId;
    private Integer paymentType;
    private Double fareAmount;
    private Double extra;
    private Double mtaTax;
    private Double tipAmount;
    private Double tollsAmount;
    private Double improvementSurcharge;
    private Double totalAmount;
    private Double congestionSurcharge;

    // Getters and Setters
    
    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public LocalDateTime getTpepPickupDatetime() {
        return tpepPickupDatetime;
    }

    public void setTpepPickupDatetime(LocalDateTime tpepPickupDatetime) {
        this.tpepPickupDatetime = tpepPickupDatetime;
    }

    public LocalDateTime getTpepDropoffDatetime() {
        return tpepDropoffDatetime;
    }

    public void setTpepDropoffDatetime(LocalDateTime tpepDropoffDatetime) {
        this.tpepDropoffDatetime = tpepDropoffDatetime;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public Double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(Double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public Integer getRatecodeId() {
        return ratecodeId;
    }

    public void setRatecodeId(Integer ratecodeId) {
        this.ratecodeId = ratecodeId;
    }

    public String getStoreAndFwdFlag() {
        return storeAndFwdFlag;
    }

    public void setStoreAndFwdFlag(String storeAndFwdFlag) {
        this.storeAndFwdFlag = storeAndFwdFlag;
    }

    public Integer getPuLocationId() {
        return puLocationId;
    }

    public void setPuLocationId(Integer puLocationId) {
        this.puLocationId = puLocationId;
    }

    public Integer getDoLocationId() {
        return doLocationId;
    }

    public void setDoLocationId(Integer doLocationId) {
        this.doLocationId = doLocationId;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public Double getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(Double fareAmount) {
        this.fareAmount = fareAmount;
    }

    public Double getExtra() {
        return extra;
    }

    public void setExtra(Double extra) {
        this.extra = extra;
    }

    public Double getMtaTax() {
        return mtaTax;
    }

    public void setMtaTax(Double mtaTax) {
        this.mtaTax = mtaTax;
    }

    public Double getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Double tipAmount) {
        this.tipAmount = tipAmount;
    }

    public Double getTollsAmount() {
        return tollsAmount;
    }

    public void setTollsAmount(Double tollsAmount) {
        this.tollsAmount = tollsAmount;
    }

    public Double getImprovementSurcharge() {
        return improvementSurcharge;
    }

    public void setImprovementSurcharge(Double improvementSurcharge) {
        this.improvementSurcharge = improvementSurcharge;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getCongestionSurcharge() {
        return congestionSurcharge;
    }

    public void setCongestionSurcharge(Double congestionSurcharge) {
        this.congestionSurcharge = congestionSurcharge;
    }

    @Override
    public String toString() {
        return "YellowTripdata{" +
                "vendorId=" + vendorId +
                ", tpepPickupDatetime=" + tpepPickupDatetime +
                ", tpepDropoffDatetime=" + tpepDropoffDatetime +
                ", passengerCount=" + passengerCount +
                ", tripDistance=" + tripDistance +
                ", ratecodeId=" + ratecodeId +
                ", storeAndFwdFlag='" + storeAndFwdFlag + '\'' +
                ", puLocationId=" + puLocationId +
                ", doLocationId=" + doLocationId +
                ", paymentType=" + paymentType +
                ", fareAmount=" + fareAmount +
                ", extra=" + extra +
                ", mtaTax=" + mtaTax +
                ", tipAmount=" + tipAmount +
                ", tollsAmount=" + tollsAmount +
                ", improvementSurcharge=" + improvementSurcharge +
                ", totalAmount=" + totalAmount +
                ", congestionSurcharge=" + congestionSurcharge +
                '}';
    }
}

