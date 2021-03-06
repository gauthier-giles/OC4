package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)

class ParkingSpotDAOTest {

    @Mock
    private static DataBaseConfig dataBaseConfig;

    @Test
    public void slotIsnotEmpty(){
        ParkingSpotDAO parkingSpotDAO  = new ParkingSpotDAO();
        assertNotNull(parkingSpotDAO);
    }

    @Test
    void updateParkingTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1,ParkingType.CAR,false);



    }


}