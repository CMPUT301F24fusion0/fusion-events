package com.example.fusion0;

import static org.junit.Assert.assertEquals;

import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.OrganizerInfo;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * Tests for the organizer
 * @author Sehej Brar
 */
public class OrganizerInfoTest {
    OrganizerInfo organizerInfo;
    ArrayList<String> facility;

    /**
     * Before each run, initialize the organizer
     * @author Sehej Brar
     */
    @Before
    public void initialize() {
        facility = new ArrayList<>(Arrays.asList("Facility 1", "Facility 2"));
        organizerInfo = new OrganizerInfo("Device ID", facility);
    }

    /**
     * Test setter for events
     * @author Sehej Brar
     */
    @Test
    public void testSetEvents() {
        Date startDate = new Date(1732936646);
        Date endDate = new Date(1735183046);
        EventInfo event = new EventInfo("Organizer", "eventName", "address",
                "facilityName", "10", "5",
                "This is an event", startDate, endDate,
                new Date(), "5:30", "6:30", "Event Poster");
        organizerInfo.setEvents(new ArrayList<>(Collections.singletonList(event)));

        assertEquals(organizerInfo.getEvents().get(0).getEventID(), event.getEventID());
    }

    /**
     * Test get device id
     * @author Sehej Brar
     */
    @Test
    public void testGetId() {
        assertEquals(organizerInfo.getDeviceId(), "Device ID");
    }

    /**
     * Test whether events are initialized to an empty ArrayList
     */
    @Test
    public void testEmpty() {
        assertEquals(organizerInfo.getEvents(), new ArrayList<EventInfo>());
    }
}
