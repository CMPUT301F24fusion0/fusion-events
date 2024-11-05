package com.example.fusion0;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.UUID;

public class QRTest {

    private static final String TEST_EVENT_ID = UUID.randomUUID().toString();
    private static final String TEST_HASH = QRCode.generateHash(TEST_EVENT_ID);


    // Test for generateHash(), checks if two equal event IDs
    // produce the same hash and a different hash for different event IDs
    @Test
    public void testGenerateHash() {
        String hash1 = QRCode.generateHash(TEST_EVENT_ID);
        String hash2 = QRCode.generateHash(TEST_EVENT_ID);
        String differentHash = QRCode.generateHash(UUID.randomUUID().toString());

        assertEquals(hash1, hash2); // Ensure hash consistency for the same input
        assertNotEquals(hash1, differentHash); // Different inputs should produce different hashes
    }

    // Test for getQrCode(), checks if the QR code is generated correctly
    @Test
    public void testGetQrCode() throws WriterException {
        QRCode qrCode = new QRCode(TEST_EVENT_ID);
        assertEquals(TEST_HASH, qrCode.getQrCode());
    }

    // Test for getQrImage(), checks if the QR image is generated correctly, and getter
    // returns the correct bitmap
    @Test
    public void testGetQrImage() throws WriterException {
        QRCode qrCode = new QRCode(TEST_EVENT_ID);
        Bitmap bitmap = qrCode.getQrImage();

        assertNotNull(bitmap);
        assertEquals(500, bitmap.getWidth());
        assertEquals(500, bitmap.getHeight());
    }

    // Test for generateQRCodeImage(), checks if image is generated correctly
    @Test
    public void testGenerateQRCodeImage() throws WriterException {
        QRCode qrCode = new QRCode(TEST_EVENT_ID);
        Bitmap bitmap = qrCode.generateQRCodeImage(300, 300, qrCode.getQrCode());

        assertNotNull(bitmap);
        assertEquals(300, bitmap.getWidth());
        assertEquals(300, bitmap.getHeight());
    }
}