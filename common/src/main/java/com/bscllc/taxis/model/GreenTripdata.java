package com.bscllc.taxis.model;

import java.time.LocalDateTime;

/**
 * Model class representing green taxi trip data from NYC taxi dataset.
 * Based on the schema from green_tripdata.parquet file.
 */
public class GreenTripdata {
    
    private Integer vendorId;
    private LocalDateTime lpepPickupDatetime;
    private LocalDateTime lpepDropoffDatetime;
    private String storeAndFwdFlag;
    private Integer ratecodeId;
    private Integer puLocationId;
    private Integer doLocationId;
    private Integer passengerCount;
    private Double tripDistance;
    private Double fareAmount;
    private Double extra;
    private Double mtaTax;
    private Double tipAmount;
    private Double tollsAmount;
    private Double ehailFee;
    private Double improvementSurcharge;
    private Double totalAmount;
    private Integer paymentType;
    private Integer tripType;
    private Double congestionSurcharge;

    // Getters and Setters
    
    public Integer getVendorId() {
        return vendorId;
    }

    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }

    public LocalDateTime getLpepPickupDatetime() {
        return lpepPickupDatetime;
    }

    public void setLpepPickupDatetime(LocalDateTime lpepPickupDatetime) {
        this.lpepPickupDatetime = lpepPickupDatetime;
    }

    public LocalDateTime getLpepDropoffDatetime() {
        return lpepDropoffDatetime;
    }

    public void setLpepDropoffDatetime(LocalDateTime lpepDropoffDatetime) {
        this.lpepDropoffDatetime = lpepDropoffDatetime;
    }

    public String getStoreAndFwdFlag() {
        return storeAndFwdFlag;
    }

    public void setStoreAndFwdFlag(String storeAndFwdFlag) {
        this.storeAndFwdFlag = storeAndFwdFlag;
    }

    public Integer getRatecodeId() {
        return ratecodeId;
    }

    public void setRatecodeId(Integer ratecodeId) {
        this.ratecodeId = ratecodeId;
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

    public Double getEhailFee() {
        return ehailFee;
    }

    public void setEhailFee(Double ehailFee) {
        this.ehailFee = ehailFee;
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

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getTripType() {
        return tripType;
    }

    public void setTripType(Integer tripType) {
        this.tripType = tripType;
    }

    public Double getCongestionSurcharge() {
        return congestionSurcharge;
    }

    public void setCongestionSurcharge(Double congestionSurcharge) {
        this.congestionSurcharge = congestionSurcharge;
    }

    @Override
    public String toString() {
        return "GreenTripdata{" +
                "vendorId=" + vendorId +
                ", lpepPickupDatetime=" + lpepPickupDatetime +
                ", lpepDropoffDatetime=" + lpepDropoffDatetime +
                ", storeAndFwdFlag='" + storeAndFwdFlag + '\'' +
                ", ratecodeId=" + ratecodeId +
                ", puLocationId=" + puLocationId +
                ", doLocationId=" + doLocationId +
                ", passengerCount=" + passengerCount +
                ", tripDistance=" + tripDistance +
                ", fareAmount=" + fareAmount +
                ", extra=" + extra +
                ", mtaTax=" + mtaTax +
                ", tipAmount=" + tipAmount +
                ", tollsAmount=" + tollsAmount +
                ", ehailFee=" + ehailFee +
                ", improvementSurcharge=" + improvementSurcharge +
                ", totalAmount=" + totalAmount +
                ", paymentType=" + paymentType +
                ", tripType=" + tripType +
                ", congestionSurcharge=" + congestionSurcharge +
                '}';
    }
}

