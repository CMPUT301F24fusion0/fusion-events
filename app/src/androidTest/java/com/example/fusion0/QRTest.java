package com.example.fusion0;

import android.graphics.Bitmap;

import com.example.fusion0.helpers.QRCode;
import com.google.zxing.WriterException;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.UUID;


/**
 * Unit tests for the QR class.
 * This class tests various functionalities such as generating a hash,
 * retrieving the QR code string, generating a QR image, and ensuring
 * consistent behavior of the QR code generation.
 *
 * @author Malshaan
 */
public class QRTest {

    private static final String TEST_EVENT_ID = UUID.randomUUID().toString();
    private static final String TEST_HASH = QRCode.generateHash(TEST_EVENT_ID);


    /**
     * Tests the generateHash(String) method.
     * Ensures that:
     *     Two identical event IDs produce the same hash.
     *     Different event IDs produce different hashes.
     */
    @Test
    public void testGenerateHash() {
        String hash1 = QRCode.generateHash(TEST_EVENT_ID);
        String hash2 = QRCode.generateHash(TEST_EVENT_ID);
        String differentHash = QRCode.generateHash(UUID.randomUUID().toString());

        assertEquals(hash1, hash2); // Ensure hash consistency for the same input
        assertNotEquals(hash1, differentHash); // Different inputs should produce different hashes
    }

}