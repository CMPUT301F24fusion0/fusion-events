package com.example.fusion0;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FirebaseTest {
    private UserInfo newUser() {
        return new UserInfo("Mike", "Ross", "mross@gmail.com", "123456789");
    }

    @Test
    public void testCheck() {
        assertTrue(true);
    }
}
