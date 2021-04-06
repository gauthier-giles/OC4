package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * This class set the price and the "inTime" and "OutTime" when
 * users process to entering or exiting a vehicle and
 * displays to users, these informations
 */

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * proceed in setting the vehicle information in the prod database
     * when an incoming vehicle park or leave.
     * linked with TicketDAO, and ParkingSpotDao.
     * @see com.parkit.parkingsystem.dao.TicketDAO
     * @see com.parkit.parkingsystem.dao.ParkingSpotDAO
     * it verify that the vehicle is not already inside the parking,
     * and display if user already coming.
     * update the parkingSpot.
     * @throws logger exception.
     */
    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                String vehicleRegNumber = getVehichleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);//alloy this parking space to the vehicle and mark it's availability as false
                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                if (ticketDAO.isUserNotAlreadyArrive(vehicleRegNumber)) {
                    ticketDAO.saveTicket(ticket);
                    System.out.println("Generated Ticket and saved in DB");

                    boolean recurringCustomer = ticketDAO.isUserAlreadyComing(vehicleRegNumber);
                    if (recurringCustomer) {
                        System.out.println("welcome back at our parking, as a recurring user you will have a 5%% discount on your ticket");
                        System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                        System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
                    } else {
                        System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                        System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    private String getVehichleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    /**
     *
     * @return the next available parking slot
     * if parking is full it return an error
     * @throws logger error if parking is full
     * @throws logger error if parking is full for this vehicle type
     * (bike or car)
     **/

    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber = 0;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    /**
     * get vehicle type
     * @return vehicle type
     */
    private ParkingType getVehichleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    /**
     * Exiting the vehicle from the database
     * saving information and permit to produce the ticket
     * with fare, time inside the parking
     * and free the parking spot for another customer.
     * linked with TicketDAO, and ParkingSpotDao and fareCalculatorService
     * @see com.parkit.parkingsystem.service.FareCalculatorService
     * @see com.parkit.parkingsystem.dao.TicketDAO
     * @see com.parkit.parkingsystem.dao.ParkingSpotDAO
     *
     * display the price, the vehicleRegNumber, and if user was a recurring user.
     *
     * @throws logger error
     * if it's unable to exit a vehicle park in the database.
     */
    public void processExitingVehicle() {
        try {
            String vehicleRegNumber = getVehichleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            boolean reccuringUser = ticketDAO.recurringUser(vehicleRegNumber);

            Date outTime = new Date();
            ticket.setOutTime(outTime); // mettre une exception
            fareCalculatorService.calculateFare(ticket, reccuringUser);
            ticketDAO.updateTicket(ticket);
            ParkingSpot parkingSpot = ticket.getParkingSpot();
            parkingSpot.setAvailable(true);
            parkingSpotDAO.updateParking(parkingSpot);
            if (reccuringUser) {
                System.out.println("as a recuring user, you will pay with a 5% discount, the price will be " + (ticket.getPrice()));
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            } else {
                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            }

        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }


}