package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.util.concurrent.TimeUnit;

public class FareCalculatorService {


    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean recurringUser) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long diff = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);



        double duration = minutes / 60d; // I've changed the duration calculation by adding the minutes (before the calculation was only by hour)

        if (minutes < 30) {
            ticket.setPrice(0);
            return ;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {

                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

        if (recurringUser){
            ticket.setPrice(ticket.getPrice()*0.95);
        }

    }
}
