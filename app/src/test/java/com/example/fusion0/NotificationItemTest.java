package com.example.fusion0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.fusion0.models.NotificationItem;

import org.junit.Before;
import org.junit.Test;

public class NotificationItemTest {
    NotificationItem notificationItem;

    /**
     * Before each run, initialize the user
     * @author Sehej Brar
     */
    @Before
    public void initialize() {
        notificationItem = new NotificationItem("Title", "Body", "0", "EventId");

    }

    /**
     * Tests to see if the correct title is returned.
     * @author Sehej Brar
     */
    @Test
    public void testGetTitle() {
        assertEquals(notificationItem.getTitle(), "Title");
    }

    /**
     * Tests to see if the correct body is returned by comparing it to a wrong body
     * @author Sehej Brar
     */
    @Test
    public void testGetBody() {
        assertNotEquals(notificationItem.getBody(), "not Body");
    }

    /**
     * Tests to see if the correct flag is returned.
     * @author Sehej Brar
     */
    @Test
    public void testGetFlag() {
        assertEquals(notificationItem.getFlag(), "0");
    }
}
