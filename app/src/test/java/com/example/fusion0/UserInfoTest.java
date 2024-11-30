package com.example.fusion0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.fusion0.models.UserInfo;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class UserInfoTest {
    private UserInfo user;

    /**
     * Before each run, initialize the user
     * @author Sehej Brar
     */
    @Before
    public void initialize() {
        user = new UserInfo(new ArrayList<String>(), "Mike",
                "Ross", "mross@psl.com", "4613217890", "1234",
                new ArrayList<String>(Collections.singletonList("Test Event ID")));

    }

    /**
     * Testing whether an arbitrarily chosen getter works.
     * @author Sehej Brar
     */
    @Test
    public void testGetAttribute() {
        assertEquals(user.getLastName(), "Ross");
    }

    /**
     * Testing whether an arbitrarily chosen setter works.
     * @author Sehej Brar
     */
    @Test
    public void testSetAttribute() {
        user.setFirstName("Harvey");
        assertEquals(user.getFirstName(), "Harvey");
    }

    /**
     * Test whether the custom equals method returns true
     * @author Sehej Brar
     */
    @Test
    public void testEquals() {
        UserInfo user2 = new UserInfo(new ArrayList<String>(), "Mike",
                "Ross", "mross@psl.com", "4613217890", "1234",
                new ArrayList<String>());
        assertTrue(user.equals(user2));
    }

    /**
     * Test whether the custom equals method returns false
     * @author Sehej Brar
     */
    @Test
    public void testNotEquals() {
        UserInfo user2 = new UserInfo(new ArrayList<String>(), "John",
                "Ross", "jross@psl.com", "4613217890", "4321",
                new ArrayList<String>());
        assertFalse(user.equals(user2));
    }

    /**
     * Ensure hashcode is equal
     * @author Sehej Brar
     */
    @Test
    public void testHash() {
        UserInfo user2 = new UserInfo(new ArrayList<String>(), "Mike",
                "Ross", "mross@psl.com", "4613217890", "1234",
                new ArrayList<String>());
        assertEquals(user.hashCode(), user2.hashCode());
    }

    /**
     * Test whether the event is successfully removed
     * @author Sehej Brar
     */
    @Test
    public void testEventList() {
        user.removeEventFromEventList("Test Event ID", user.getEvents());

        assertEquals(user.getEvents(), new ArrayList<String>());
    }
}
