package com.example.fusion0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.fusion0.models.EventInfo;
import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventInfoTest {
    EventInfo event;

    /**
     * Before each run, initialize the organizer
     * @author Sehej Brar
     */
    @Before
    public void initialize() throws WriterException {
        Date startDate = new Date(1732936646);
        Date endDate = new Date(1735183046);

        event = new EventInfo("Organizer", "eventName", "address",
                "facilityName", "10", "5",
                "This is an event", startDate, endDate,
                new Date(), "5:30", "6:30", "Event Poster");
    }

    /**
     * Testing whether an arbitrarily chosen getter works.
     * @author Sehej Brar
     */
    @Test
    public void testGetAttribute() {
        assertEquals(event.getEventName(), "eventName");
    }

    /**
     * Testing whether an arbitrarily chosen setter works.
     * @author Sehej Brar
     */
    @Test
    public void testSetAttribute() {
        event.setCapacity("20");
        assertEquals(event.getCapacity(), "20");
    }

    /**
     * See if we can remove a user from the waiting list
     * @author Sehej Brar
     */
    @Test
    public void testRemoveUser() {
        ArrayList<Map<String, String>> waitlist = new ArrayList<Map<String, String>>();
        Map<String, String> user = new HashMap<>();
        user.put("did", "1234");
        waitlist.add(user);
        Map<String, String> user2 = new HashMap<>();
        user2.put("did", "5678");
        waitlist.add(user2);

        event.setWaitinglist(waitlist);

        event.removeUserFromWaitingList("1234", waitlist);

        assertEquals(1, event.getWaitinglist().size());
        assertTrue(event.getWaitinglist().get(0).containsValue("5678"));
    }

    /**
     * See if we get an error when the remove user off fragment_waitlist does not hold
     * @author Sehej Brar
     */
    @Test
    public void testRemoveFakeUser() {
        ArrayList<Map<String, String>> waitlist = new ArrayList<Map<String, String>>();
        Map<String, String> user = new HashMap<>();
        user.put("did", "1234");
        waitlist.add(user);
        Map<String, String> user2 = new HashMap<>();
        user2.put("did", "5678");
        waitlist.add(user2);

        event.setWaitinglist(waitlist);
        event.removeUserFromWaitingList("91011", waitlist);

        assertEquals(2, event.getWaitinglist().size());
    }
}
