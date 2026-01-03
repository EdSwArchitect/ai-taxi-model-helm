package com.bscllc.taxis.model;

import java.time.LocalDateTime;

/**
 * Model class representing green taxi trip data from NYC taxi dataset.
 * Generated from the parquet schema of green_tripdata.parquet file.
 * 
 * Schema fields (in order):
 * 1. VendorID (Integer)
 * 2. lpep_pickup_datetime (LocalDateTime)
 * 3. lpep_dropoff_datetime (LocalDateTime)
 * 4. store_and_fwd_flag (String)
 * 5. RatecodeID (Integer)
 * 6. PULocationID (Integer)
 * 7. DOLocationID (Integer)
 * 8. passenger_count (Integer)
 * 9. trip_distance (Double)
 * 10. fare_amount (Double)
 * 11. extra (Double)
 * 12. mta_tax (Double)
 * 13. tip_amount (Double)
 * 14. tolls_amount (Double)
 * 15. ehail_fee (Double)
 * 16. improvement_surcharge (Double)
 * 17. total_amount (Double)
 * 18. payment_type (Integer)
 * 19. trip_type (Integer)
 * 20. congestion_surcharge (Double)
 */
public class GreenTripdata {
    
    // Fields from parquet schema (in order)
    private Integer vendorId;              // VendorID
    private LocalDateTime lpepPickupDatetime;      // lpep_pickup_datetime
    private LocalDateTime lpepDropoffDatetime;     // lpep_dropoff_datetime
    private String storeAndFwdFlag;        // store_and_fwd_flag
    private Integer ratecodeId;            // RatecodeID
    private Integer puLocationId;          // PULocationID
    private Integer doLocationId;          // DOLocationID
    private Integer passengerCount;        // passenger_count
    private Double tripDistance;           // trip_distance
    private Double fareAmount;             // fare_amount
    private Double extra;                  // extra
    private Double mtaTax;                 // mta_tax
    private Double tipAmount;              // tip_amount
    private Double tollsAmount;            // tolls_amount
    private Double ehailFee;               // ehail_fee
    private Double improvementSurcharge;   // improvement_surcharge
    private Double totalAmount;            // total_amount
    private Integer paymentType;           // payment_type
    private Integer tripType;              // trip_type
    private Double congestionSurcharge;    // congestion_surcharge

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
