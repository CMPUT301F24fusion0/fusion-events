package com.example.fusion0;

// Code and Javadocs provided by chatGPT
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;


/**
 * The QRCode class represents a QR code that is generated based on an event ID.
 * It provides methods to generate a hashed QR code, store the QR code in Firestore,
 * and delete the QR code from Firestore.
 */
public class QRCode {
    private String qrCode;
    private Bitmap qrImage;  // The generated QR code
    private final CollectionReference qrRef;  // Reference to the Firestore "qrCodes" collection

    /**
     * Constructor to create a QRCode object for the event using the event ID.
     * It generates a unique hashed QR code based on the event ID and initializes the Firestore reference.
     *
     * @param eventId The ID of the event for which the QR code is generated.
     */
    public QRCode(String eventId) throws WriterException {
        this.qrCode = generateHash(eventId);
        this.qrImage = generateQRCodeImage(500, 500, this.qrCode);  // Generate QR code based on event ID

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        qrRef = db.collection("qrCodes");  // Reference to the "qrCodes" collection in Firestore
    }

    /**
     * Generates a hashed QR code using the SHA-256 algorithm.
     * This method takes an input (event ID) and returns a hashed string that acts as the QR code.
     *
     * @param input The string to be hashed (in this case, the event ID).
     * @return A hashed string representing the QR code.
     */
    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));  // Convert input to bytes and hash it
            StringBuilder hexString = new StringBuilder();  // StringBuilder to store the hashed QR code

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);  // Convert byte to hexadecimal
                if (hex.length() == 1) hexString.append('0');  // Ensure two characters for each byte
                hexString.append(hex);  // Append the hex string
            }
            return hexString.toString();  // Return the full hashed QR code
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating QR code hash", e);  // Handle error if SHA-256 is not available
        }
    }

    /**
     * Returns the generated QR code.
     *
     * @return The generated QR code string.
     */
    public String getQrCode() {
        return this.qrCode;
    }

    /**
     * Stores the QR code in Firestore.
     * The QR code is stored in the "qrCodes" collection with the event ID as the document ID.
     *
     * @param eventId The ID of the event associated with the QR code.
     */
    public void storeQRCode(String eventId) {
        HashMap<String, Object> qrData = new HashMap<>();  // Create a HashMap to store QR code data
        qrData.put("eventId", eventId);  // Store the event ID
        qrData.put("qrCode", this.qrCode);  // Store the QR code

        // Store the QR code data in Firestore using the event ID as the document ID
        qrRef.document(eventId).set(qrData)
                .addOnSuccessListener(aVoid -> System.out.println("QR Code added successfully"))
                .addOnFailureListener(e -> System.out.println("Error adding QR Code: " + e.getMessage()));
    }

    /**
     * Deletes the QR code from Firestore using the event ID.
     * This method removes the QR code associated with the event from the "qrCodes" collection.
     *
     * @param eventId The ID of the event whose QR code should be deleted.
     */
    public void deleteQRCode(String eventId) {
        // Delete the document from Firestore that matches the event ID
        qrRef.document(eventId).delete()
                .addOnSuccessListener(aVoid -> System.out.println("QR Code deleted successfully"))
                .addOnFailureListener(e -> System.out.println("Error deleting QR Code: " + e.getMessage()));
    }

    /**
     * Generates a QR code image from the QR code string.
     * This method uses ZXing to convert the hashed QR code string into a Bitmap.
     *
     * @param width The width of the generated QR code image.
     * @param height The height of the generated QR code image.
     * @return A Bitmap representation of the QR code.
     * @throws WriterException If an error occurs during QR code generation.
     */
    public Bitmap generateQRCodeImage(int width, int height, String qrCode) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }
        return bitmap;
    }
}

