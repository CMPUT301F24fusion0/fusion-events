package com.example.fusion0;
// Javadocs from chatGPT

import android.graphics.Bitmap;

import com.example.fusion0.helpers.QRCode;
import com.google.zxing.WriterException;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.UUID;

/**
 * Unit tests for the QRCode class.
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
     * - Two identical event IDs produce the same hash.
     * - Different event IDs produce different hashes.
     */
    @Test
    public void testGenerateHash() {
        String hash1 = QRCode.generateHash(TEST_EVENT_ID);
        String hash2 = QRCode.generateHash(TEST_EVENT_ID);
        String differentHash = QRCode.generateHash(UUID.randomUUID().toString());

        assertEquals(hash1, hash2); // Ensure hash consistency for the same input
        assertNotEquals(hash1, differentHash); // Different inputs should produce different hashes
    }

    /**
     * Tests the getQrCode() method.
     * Ensures that the hashed QR code string is correctly generated and retrieved.
     *
     * @throws WriterException If an error occurs during QR code image generation.
     */
    @Test
    public void testGetQrCode() throws WriterException {
        QRCode qrCode = new QRCode(TEST_EVENT_ID);
        assertEquals(TEST_HASH, qrCode.getQrCode());
    }

    /**
     * Tests the generateQRCodeImage(int, int, String) method.
     * Ensures that a QR code image is generated with the specified dimensions
     * and verifies that the generated Bitmap is not null.
     *
     * @throws WriterException If an error occurs during QR code image generation.
     */
    @Test
    public void testGenerateQRCodeImage() throws WriterException {
        QRCode qrCode = new QRCode(TEST_EVENT_ID);
        Bitmap qrImage = qrCode.getQrImage();

        assertNotNull(qrImage); // Ensure the generated QR image is not null
        assertEquals(500, qrImage.getWidth()); // Ensure the QR code width is as specified
        assertEquals(500, qrImage.getHeight()); // Ensure the QR code height is as specified
    }

    /**
     * Tests the getQrImage() method.
     * Ensures that the QR code image is correctly generated and retrievable.
     *
     * @throws WriterException If an error occurs during QR code image generation.
     */
    @Test
    public void testGetQrImage() throws WriterException {
        QRCode qrCode = new QRCode(TEST_EVENT_ID);
        Bitmap qrImage = qrCode.getQrImage();

        assertNotNull(qrImage); // Ensure the QR image is not null
    }

    /**
     * Tests the getEventIdFromHash(String, EventIdCallback) method.
     * Ensures that:
     * - The correct event ID is retrieved for a given hash if it exists.
     * - The callback handles the case where the hash is not found in Firestore.
     */
    @Test
    public void testGetEventIdFromHash() {
        QRCode.getEventIdFromHash(TEST_HASH, new QRCode.EventIdCallback() {
            @Override
            public void onEventIdFound(String eventId) {
                assertEquals(TEST_EVENT_ID, eventId); // Verify the correct event ID is returned
            }

            @Override
            public void onEventIdNotFound() {
                fail("Event ID should be found for a valid hash.");
            }
        });

        QRCode.getEventIdFromHash("invalid_hash", new QRCode.EventIdCallback() {
            @Override
            public void onEventIdFound(String eventId) {
                fail("Event ID should not be found for an invalid hash.");
            }

            @Override
            public void onEventIdNotFound() {
                assertTrue(true); // Callback correctly handles the case where the hash is not found
            }
        });
    }
}
