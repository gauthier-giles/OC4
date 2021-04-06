package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * This class create, save, and update the parking's ticket
 * @see DataBaseConfig
 *
 * verify if customer left the parking
 * verify if customer don't occupy and existing place
 * and verify if customer is a recurring user or no
 *
 * @throws logger.error
 */

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();


    /**
     *
     * @param ticket
     * @return the query result, and save the ticket information
     * the ID of the parkingSpot
     * the vehicleRegNumber (primary key of ticket table in "Prod" and "test" Database)
     *
     */
    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)

            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : (new Timestamp(ticket.getOutTime().getTime())));
            return ps.execute();
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
            return false;
        }
    }

    /**
     *
     * @param vehicleRegNumber
     * @return the ticket, with vehicleRegNumber, the parkingSpot, the ID, the Price, the entry and the exit
     * show to the user or customer the price
     * @see com.parkit.parkingsystem.service.FareCalculatorService
     * @see com.parkit.parkingsystem.constants.Fare
     * @see ParkingType
     * @see ParkingSpotDAO
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)), false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error fetching next available slot", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    /**
     *
     * @param ticket
     * @return the updated ticket
     * @see com.parkit.parkingsystem.dao.TicketDAO
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3, ticket.getId());
            ps.execute();
            return true;
        } catch (Exception ex) {
            logger.error("Error saving ticket info", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /**
     *
     * @param vehicleRegNumber vehicle registration number (as a numberplate)
     * @return true if the vehicle ever coming in the parking as a paying user
     * verify if customer have paid last time he's coming (more than 30 minutes)
     * if yes, customer will have a 5% discount
     * @see com.parkit.parkingsystem.constants.DBConstants
     */
    public boolean isUserAlreadyComing(String vehicleRegNumber) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.IS_REG_NUMBER_EXIST);
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String reg_Number = rs.getString("VEHICLE_REG_NUMBER");
                if (vehicleRegNumber.equals(reg_Number)) {
                    //System.out.println("voiture deja venue 5%% de remise");
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.error("Error returning recurrent customer", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /**
     *
     * @param vehicleRegNumber
     * @return false if user select a vehicleRegNumber already inside the parking
     * because before, customer could entry twice the same vehicle in the parking
     * so now this function verify that user can't put the same vehicle twice
     * if true the program continue without warning
     */
    public boolean isUserNotAlreadyArrive(String vehicleRegNumber) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.VEHICLE_ALREADY_ARRIVE);
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String reg_Number = rs.getString("VEHICLE_REG_NUMBER");
                if (vehicleRegNumber.equals(reg_Number)) {
                    System.out.println("vehicle already inside the parking, please select another vehicle registration number");
                    return false;
                }
            }
        } catch (Exception ex) {
            logger.error("Error, connection with database is impossible", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return true;
    }

    /**
     *
     * @param vehicleRegNumber
     * @return true if vehicle is a recurringUser
     * verify that the user already came but not verify if user
     * paid the first time as we should consider 5% discount
     * only if customer park was not free the first time
     * @see TicketDAO.isUserAlreadyComing
     */
    public boolean recurringUser(String vehicleRegNumber) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.RECURRING_USER);
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String reg_Number = rs.getString("VEHICLE_REG_NUMBER");
                if (vehicleRegNumber.equals(reg_Number)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            logger.error("Error, connection with database is impossible", ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }


}
