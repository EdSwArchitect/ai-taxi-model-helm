package com.bscllc.taxis.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GreenTripdataTest {

    private GreenTripdata greenTripdata;

    @BeforeEach
    void setUp() {
        greenTripdata = new GreenTripdata();
    }

    @Test
    void testVendorId() {
        assertNull(greenTripdata.getVendorId());
        greenTripdata.setVendorId(1);
        assertEquals(1, greenTripdata.getVendorId());
    }

    @Test
    void testLpepPickupDatetime() {
        assertNull(greenTripdata.getLpepPickupDatetime());
        LocalDateTime pickupTime = LocalDateTime.of(2023, 1, 15, 10, 30, 0);
        greenTripdata.setLpepPickupDatetime(pickupTime);
        assertEquals(pickupTime, greenTripdata.getLpepPickupDatetime());
    }

    @Test
    void testLpepDropoffDatetime() {
        assertNull(greenTripdata.getLpepDropoffDatetime());
        LocalDateTime dropoffTime = LocalDateTime.of(2023, 1, 15, 11, 0, 0);
        greenTripdata.setLpepDropoffDatetime(dropoffTime);
        assertEquals(dropoffTime, greenTripdata.getLpepDropoffDatetime());
    }

    @Test
    void testStoreAndFwdFlag() {
        assertNull(greenTripdata.getStoreAndFwdFlag());
        greenTripdata.setStoreAndFwdFlag("N");
        assertEquals("N", greenTripdata.getStoreAndFwdFlag());
        greenTripdata.setStoreAndFwdFlag("Y");
        assertEquals("Y", greenTripdata.getStoreAndFwdFlag());
    }

    @Test
    void testRatecodeId() {
        assertNull(greenTripdata.getRatecodeId());
        greenTripdata.setRatecodeId(1);
        assertEquals(1, greenTripdata.getRatecodeId());
    }

    @Test
    void testPuLocationId() {
        assertNull(greenTripdata.getPuLocationId());
        greenTripdata.setPuLocationId(100);
        assertEquals(100, greenTripdata.getPuLocationId());
    }

    @Test
    void testDoLocationId() {
        assertNull(greenTripdata.getDoLocationId());
        greenTripdata.setDoLocationId(200);
        assertEquals(200, greenTripdata.getDoLocationId());
    }

    @Test
    void testPassengerCount() {
        assertNull(greenTripdata.getPassengerCount());
        greenTripdata.setPassengerCount(2);
        assertEquals(2, greenTripdata.getPassengerCount());
    }

    @Test
    void testTripDistance() {
        assertNull(greenTripdata.getTripDistance());
        greenTripdata.setTripDistance(2.5);
        assertEquals(2.5, greenTripdata.getTripDistance());
    }

    @Test
    void testFareAmount() {
        assertNull(greenTripdata.getFareAmount());
        greenTripdata.setFareAmount(10.50);
        assertEquals(10.50, greenTripdata.getFareAmount());
    }

    @Test
    void testExtra() {
        assertNull(greenTripdata.getExtra());
        greenTripdata.setExtra(1.0);
        assertEquals(1.0, greenTripdata.getExtra());
    }

    @Test
    void testMtaTax() {
        assertNull(greenTripdata.getMtaTax());
        greenTripdata.setMtaTax(0.5);
        assertEquals(0.5, greenTripdata.getMtaTax());
    }

    @Test
    void testTipAmount() {
        assertNull(greenTripdata.getTipAmount());
        greenTripdata.setTipAmount(2.0);
        assertEquals(2.0, greenTripdata.getTipAmount());
    }

    @Test
    void testTollsAmount() {
        assertNull(greenTripdata.getTollsAmount());
        greenTripdata.setTollsAmount(0.0);
        assertEquals(0.0, greenTripdata.getTollsAmount());
    }

    @Test
    void testEhailFee() {
        assertNull(greenTripdata.getEhailFee());
        greenTripdata.setEhailFee(0.0);
        assertEquals(0.0, greenTripdata.getEhailFee());
    }

    @Test
    void testImprovementSurcharge() {
        assertNull(greenTripdata.getImprovementSurcharge());
        greenTripdata.setImprovementSurcharge(0.3);
        assertEquals(0.3, greenTripdata.getImprovementSurcharge());
    }

    @Test
    void testTotalAmount() {
        assertNull(greenTripdata.getTotalAmount());
        greenTripdata.setTotalAmount(15.30);
        assertEquals(15.30, greenTripdata.getTotalAmount());
    }

    @Test
    void testPaymentType() {
        assertNull(greenTripdata.getPaymentType());
        greenTripdata.setPaymentType(1);
        assertEquals(1, greenTripdata.getPaymentType());
    }

    @Test
    void testTripType() {
        assertNull(greenTripdata.getTripType());
        greenTripdata.setTripType(1);
        assertEquals(1, greenTripdata.getTripType());
    }

    @Test
    void testCongestionSurcharge() {
        assertNull(greenTripdata.getCongestionSurcharge());
        greenTripdata.setCongestionSurcharge(2.5);
        assertEquals(2.5, greenTripdata.getCongestionSurcharge());
    }

    @Test
    void testToString() {
        greenTripdata.setVendorId(1);
        greenTripdata.setPassengerCount(2);
        greenTripdata.setTripDistance(2.5);
        greenTripdata.setTotalAmount(15.30);
        
        String toString = greenTripdata.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("GreenTripdata"));
        assertTrue(toString.contains("vendorId=1"));
        assertTrue(toString.contains("passengerCount=2"));
        assertTrue(toString.contains("tripDistance=2.5"));
        assertTrue(toString.contains("totalAmount=15.3"));
    }

    @Test
    void testCompleteTripData() {
        LocalDateTime pickup = LocalDateTime.of(2023, 1, 15, 10, 30, 0);
        LocalDateTime dropoff = LocalDateTime.of(2023, 1, 15, 11, 0, 0);
        
        greenTripdata.setVendorId(1);
        greenTripdata.setLpepPickupDatetime(pickup);
        greenTripdata.setLpepDropoffDatetime(dropoff);
        greenTripdata.setStoreAndFwdFlag("N");
        greenTripdata.setRatecodeId(1);
        greenTripdata.setPuLocationId(100);
        greenTripdata.setDoLocationId(200);
        greenTripdata.setPassengerCount(2);
        greenTripdata.setTripDistance(2.5);
        greenTripdata.setFareAmount(10.50);
        greenTripdata.setExtra(1.0);
        greenTripdata.setMtaTax(0.5);
        greenTripdata.setTipAmount(2.0);
        greenTripdata.setTollsAmount(0.0);
        greenTripdata.setEhailFee(0.0);
        greenTripdata.setImprovementSurcharge(0.3);
        greenTripdata.setTotalAmount(15.30);
        greenTripdata.setPaymentType(1);
        greenTripdata.setTripType(1);
        greenTripdata.setCongestionSurcharge(2.5);
        
        assertEquals(1, greenTripdata.getVendorId());
        assertEquals(pickup, greenTripdata.getLpepPickupDatetime());
        assertEquals(dropoff, greenTripdata.getLpepDropoffDatetime());
        assertEquals("N", greenTripdata.getStoreAndFwdFlag());
        assertEquals(1, greenTripdata.getRatecodeId());
        assertEquals(100, greenTripdata.getPuLocationId());
        assertEquals(200, greenTripdata.getDoLocationId());
        assertEquals(2, greenTripdata.getPassengerCount());
        assertEquals(2.5, greenTripdata.getTripDistance());
        assertEquals(10.50, greenTripdata.getFareAmount());
        assertEquals(1.0, greenTripdata.getExtra());
        assertEquals(0.5, greenTripdata.getMtaTax());
        assertEquals(2.0, greenTripdata.getTipAmount());
        assertEquals(0.0, greenTripdata.getTollsAmount());
        assertEquals(0.0, greenTripdata.getEhailFee());
        assertEquals(0.3, greenTripdata.getImprovementSurcharge());
        assertEquals(15.30, greenTripdata.getTotalAmount());
        assertEquals(1, greenTripdata.getPaymentType());
        assertEquals(1, greenTripdata.getTripType());
        assertEquals(2.5, greenTripdata.getCongestionSurcharge());
    }

    @Test
    void testNullHandling() {
        // Test that all fields can be set to null
        greenTripdata.setVendorId(null);
        greenTripdata.setLpepPickupDatetime(null);
        greenTripdata.setLpepDropoffDatetime(null);
        greenTripdata.setStoreAndFwdFlag(null);
        greenTripdata.setRatecodeId(null);
        greenTripdata.setPuLocationId(null);
        greenTripdata.setDoLocationId(null);
        greenTripdata.setPassengerCount(null);
        greenTripdata.setTripDistance(null);
        greenTripdata.setFareAmount(null);
        greenTripdata.setExtra(null);
        greenTripdata.setMtaTax(null);
        greenTripdata.setTipAmount(null);
        greenTripdata.setTollsAmount(null);
        greenTripdata.setEhailFee(null);
        greenTripdata.setImprovementSurcharge(null);
        greenTripdata.setTotalAmount(null);
        greenTripdata.setPaymentType(null);
        greenTripdata.setTripType(null);
        greenTripdata.setCongestionSurcharge(null);
        
        assertNull(greenTripdata.getVendorId());
        assertNull(greenTripdata.getLpepPickupDatetime());
        assertNull(greenTripdata.getLpepDropoffDatetime());
        assertNull(greenTripdata.getStoreAndFwdFlag());
        assertNull(greenTripdata.getRatecodeId());
        assertNull(greenTripdata.getPuLocationId());
        assertNull(greenTripdata.getDoLocationId());
        assertNull(greenTripdata.getPassengerCount());
        assertNull(greenTripdata.getTripDistance());
        assertNull(greenTripdata.getFareAmount());
        assertNull(greenTripdata.getExtra());
        assertNull(greenTripdata.getMtaTax());
        assertNull(greenTripdata.getTipAmount());
        assertNull(greenTripdata.getTollsAmount());
        assertNull(greenTripdata.getEhailFee());
        assertNull(greenTripdata.getImprovementSurcharge());
        assertNull(greenTripdata.getTotalAmount());
        assertNull(greenTripdata.getPaymentType());
        assertNull(greenTripdata.getTripType());
        assertNull(greenTripdata.getCongestionSurcharge());
    }
}

