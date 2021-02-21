package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        calculateFareTest(60, ParkingType.CAR, Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike() {
        calculateFareTest(60, ParkingType.BIKE, Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        calculateFareTest(45, ParkingType.BIKE, 0.75 * Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        calculateFareTest(45, ParkingType.CAR, 0.75 * Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanThirtyMinutes() {
        calculateFareTest(29, ParkingType.CAR, 0.0);
    }

    @Test
    public void calculateFareBikeWithLessThanThirtyMinutes() {
        calculateFareTest(29, ParkingType.BIKE, 0.0);
    }

    private void calculateFareTest(int parkingMinutes, ParkingType type, double attendedPrice) {
        calculateFareTest(parkingMinutes, type, attendedPrice,false);
    }

    private void calculateFareTest(int parkingMinutes, ParkingType type, double attendedPrice, boolean recurringUser) {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (parkingMinutes * 60 * 1000));//30 minutes hours parking time should give a free ticket
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, type, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket,recurringUser);
        if(recurringUser){
            assertEquals(attendedPrice * 0.95, ticket.getPrice());
        }else{
            assertEquals(attendedPrice, ticket.getPrice());
        }

    }
    // SE SOUVENIR DU REFACTOR !!! selection => click droit => refactor => extract method => preview

    @Test
    public void calculateFareCarIfRegNumberAlreadyExists() {
        calculateFareTest(60 , ParkingType.CAR, Fare.CAR_RATE_PER_HOUR, true);
    }

    @Test
    public void calculateFareCarIfRegNumberAlreadyExistsTwoHours() {
        calculateFareTest(120, ParkingType.CAR, Fare.CAR_RATE_PER_HOUR*2, true);
    }

    @Test
    public void calculateFareBikeIfRegNumberAlreadyExists() {
        calculateFareTest(60 , ParkingType.BIKE, Fare.BIKE_RATE_PER_HOUR, true);
    }

    @Test
    public void calculateFareBikeIfRegNumberAlreadyExistsTwoHours() {
        calculateFareTest(120, ParkingType.BIKE, Fare.BIKE_RATE_PER_HOUR*2, true);
    }

    @Test
    public void calculateFareCarIfRegNumberAlreadyExistsAndLessThan30Minutes() {
        calculateFareTest(29 , ParkingType.CAR, 0.0, true);
    }

    @Test
    public void calculateFareBikeIfRegNumberAlreadyExistsAndLessThan30Minutes() {
        calculateFareTest(29 , ParkingType.BIKE, 0.0, true);
    }



}
