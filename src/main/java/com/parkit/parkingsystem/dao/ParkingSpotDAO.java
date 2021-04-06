package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This class verify the ParkingSpot.
 * there is two functions here
 * the getNextAvailableSlot create the next ParkingSpot
 * the updateParking reset a ParkingSpot (if it is free or occupied)
 */

public class ParkingSpotDAO {
    /**
     * @see DataBaseConfig
     * connection to database prod and
     * return specific value to
     * each functions
     */

    @SuppressWarnings("checkstyle:ConstantName")
    private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");

    @SuppressWarnings("checkstyle:VisibilityModifier")
    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     *
     * @param parkingType
     * @return create a next parking slot indicating if
     * it's a car slot or a bike slot in the parking
     * close the connexion to the database
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    public int getNextAvailableSlot(ParkingType parkingType) {
        Connection con = null;
        int result = -1;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
            ps.setString(1, parkingType.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                result = rs.getInt(1);
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        } catch (Exception ex) {
            logger.error("Error fetching next available slot",ex);
        } finally {
            dataBaseConfig.closeConnection(con);
        }
        return result;
    }

    /**
     * @param parkingSpot
     * @return the value and the Int of the parking spot:
     * 1 if it's available
     * 0 if it's occupied
     * and the type of the parking (BIKE or CAR)
     * @see ParkingType
     * and update the sum of parking place
     */
    @SuppressWarnings("checkstyle:FinalParameters")
    public boolean updateParking(ParkingSpot parkingSpot) {
        //update the availability for that parking slot
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            dataBaseConfig.closePreparedStatement(ps);
            return (updateRowCount == 1);
        } catch (Exception ex) {
            logger.error("Error updating parking info", ex);
            return false;
        } finally {
            dataBaseConfig.closeConnection(con);
        }
    }

}
