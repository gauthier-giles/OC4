package com.parkit.parkingsystem.constants;

public class DBConstants {
    /**
     * This class prepared the Statement
     * it's linked with the database "prod" and "test"
     */

    public static final String GET_NEXT_PARKING_SPOT = "select min(PARKING_NUMBER) from parking where AVAILABLE = true and TYPE = ?";
    public static final String UPDATE_PARKING_SPOT = "update parking set available = ? where PARKING_NUMBER = ?";

    public static final String SAVE_TICKET = "insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)";
    public static final String UPDATE_TICKET = "update ticket set PRICE=?, OUT_TIME=? where ID=?";
    public static final String GET_TICKET = "select t.PARKING_NUMBER, t.ID, t.PRICE, t.IN_TIME, t.OUT_TIME, p.TYPE from ticket t,parking p where p.parking_number = t.parking_number and t.VEHICLE_REG_NUMBER=? and OUT_TIME IS NULL order by t.IN_TIME  limit 1";

    public static final String IS_REG_NUMBER_EXIST = "SELECT ID, VEHICLE_REG_NUMBER, timediff(OUT_TIME, IN_TIME) FROM ticket WHERE VEHICLE_REG_NUMBER=? and timediff(OUT_TIME, IN_TIME)>'00:30:00'";
    public static final String VEHICLE_ALREADY_ARRIVE = "SELECT VEHICLE_REG_NUMBER, IN_TIME, OUT_TIME FROM ticket WHERE VEHICLE_REG_NUMBER=? and IN_TIME IS NOT NULL and OUT_TIME IS NULL";
    public static final String RECURRING_USER = "SELECT VEHICLE_REG_NUMBER, OUT_TIME FROM ticket WHERE VEHICLE_REG_NUMBER=? and OUT_TIME IS NOT NULL";

}
