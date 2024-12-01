package com.example.fusion0;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.example.fusion0.helpers.AddEventHelper;
import com.example.fusion0.models.AddEventViewModel;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the AddEventViewModel class that contains info about the event
 * @author Sehej Brar
 */
public class AddEventVMTest {
    AddEventViewModel aevm;
    AddEventHelper aeh;

    /**
     * Initialize the class and mock anything that results in a PID error
     * @author Sehej Brar
     */
    @Before
    public void initialize() {
        aevm = new AddEventViewModel();
        aeh = mock(AddEventHelper.class);
    }

    /**
     * AddEventHelper should be null by default
     * @author Sehej Brar
     */
    @Test
    public void testNullHelper() {
        assertEquals(null, aevm.getHelper());
    }


    /**
     * AddEventHelper class should give us back the helper we instantiated with it
     * @author Sehej Brar
     */
    @Test
    public void testEqualHelper() {
        aevm.initializeHelper(aeh);
        assertEquals(aevm.getHelper(), aeh);
    }
}
