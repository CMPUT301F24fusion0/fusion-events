package com.example.fusion0;

import static org.junit.Assert.assertEquals;

import com.example.fusion0.helpers.AddEventHelper;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Helper class to allow for organizer to add event
 * @author Sehej Brar
 */
public class AddEventHelperTest {
    static AddEventHelper addEventHelper;

    /**
     * Set up the class
     * @author Sehej Brar
     */
    @BeforeClass
    public static void init() {
        addEventHelper = new AddEventHelper();
    }

    /**
     * Testing whether the class sets the facility's ID
     * @author Sehej Brar
     */
    @Test
    public void testSetFacilityId() {
        addEventHelper.setFacilityID("facility ID");
        assertEquals(addEventHelper.getFacilityID(), "facility ID");
    }

    /**
     * Testing whether the class sets the description
     * @author Sehej Brar
     */
    @Test
    public void testSetDescription() {
        addEventHelper.setDescription("description");
        assertEquals(addEventHelper.getDescription(), "description");
    }

    /**
     * Testing whether the class sets the geolocation
     * @author Sehej Brar
     */
    @Test
    public void testSetGeolocation() {
        addEventHelper.setGeolocation(false);
        assertEquals(addEventHelper.getGeolocation(), false);
    }
}
