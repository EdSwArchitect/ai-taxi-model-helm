package com.bscllc.taxis.model;

import java.time.LocalDateTime;

/**
 * Model class representing yellow taxi trip data from NYC taxi dataset.
 * Generated from the parquet schema of yellow_tripdata.parquet file.
 * 
 * Schema fields (in order):
 * 1. VendorID (Integer)
 * 2. tpep_pickup_datetime (LocalDateTime)
 * 3. tpep_dropoff_datetime (LocalDateTime)
 * 4. passenger_count (Integer)
 * 5. trip_distance (Double)
 * 6. RatecodeID (Integer)
 * 7. store_and_fwd_flag (String)
 * 8. PULocationID (Integer)
 * 9. DOLocationID (Integer)
 * 10. payment_type (Integer)
 * 11. fare_amount (Double)
 * 12. extra (Double)
 * 13. mta_tax (Double)
 * 14. tip_amount (Double)
 * 15. tolls_amount (Double)
 * 16. improvement_surcharge (Double)
 * 17. total_amount (Double)
 * 18. congestion_surcharge (Double)
 */
public class YellowTripdata {
    
    // Fields from parquet schema (in order)
    private Integer vendorId;              // VendorID
    private LocalDateTime tpepPickupDatetime;      // tpep_pickup_datetime
    private LocalDateTime tpepDropoffDatetime;     // tpep_dropoff_datetime
    private Integer passengerCount;        // passenger_count
    private Double tripDistance;           // trip_distance
    private Integer ratecodeId;            // RatecodeID
    private String storeAndFwdFlag;        // store_and_fwd_flag
    private Integer puLocationId;          // PULocationID
    private Integer doLocationId;          // DOLocationID
    private Integer paymentType;           // payment_type
    private Double fareAmount;             // fare_amount
    private Double extra;                  // extra
    private Double mtaTax;                 // mta_tax
    private Double tipAmount;              // tip_amount
    private Double tollsAmount;            // tolls_amount
    private Double improvementSurcharge;   // improvement_surcharge
    private Double totalAmount;            // total_amount
    private Double congestionSurcharge;    // congestion_surcharge

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
