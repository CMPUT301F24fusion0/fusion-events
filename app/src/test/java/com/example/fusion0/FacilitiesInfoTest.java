package com.example.fusion0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.fusion0.models.FacilitiesInfo;

import org.junit.Before;
import org.junit.Test;

public class FacilitiesInfoTest {
    private FacilitiesInfo facilitiesInfo;

    /**
     * Before each run, initialize the facility
     * @author Sehej Brar
     */
    @Before
    public void initialize() {
        facilitiesInfo = new FacilitiesInfo("Address", "Name", "Owner", "image");
    }

    /**
     * See if the facility name we get back is correct
     * @author Sehej Brar
     */
    @Test
    public void testGetFacilityName() {
        assertEquals(facilitiesInfo.getAddress(), "Address");
    }

    /**
     * Check if setters work
     * @author Sehej Brar
     */
    @Test
    public void testSetOwner() {
        facilitiesInfo.setOwner("New Owner");
        assertEquals(facilitiesInfo.getOwner(), "New Owner");
    }

    /**
     * See if randomly setting the facility id is valid
     * @author Sehej Brar
     */
    @Test
    public void testNullId() {
        assertNotNull(facilitiesInfo.getFacilityID());
    }
}
