package com.example.fusion0;

// Code and Javadocs provided by chatGPT

import android.graphics.Bitmap;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * The QRCode class represents a QR code that is generated based on an event ID.
 * It provides methods to generate a hashed QR code, store the QR code in Firestore,
 * and delete the QR code from Firestore.
 */
public class QRCode {
    private String qrCode;
    private Bitmap qrImage;  // The generated QR code


    /**
     * Constructor to create a QRCode object for the event using the event UUID.
     * It generates a unique hashed QR code based on the event ID and initializes the Firestore reference.
     *
     * @param eventId The ID of the event for which the QR code is generated.
     */
    public QRCode(String eventId) throws WriterException {
        this.qrCode = generateHash(eventId);
        this.qrImage = generateQRCodeImage(500, 500, this.qrCode);  // Generate QR code based on event ID
    }

    /**
     * Generates a hashed QR code using the SHA-256 algorithm.
     * This method takes an input (event ID) and returns a hashed string that acts as the QR code.
     *
     * @param input The string to be hashed (in this case, the event ID).
     * @return A hashed string representing the QR code.
     */
    public static String generateHash(String input) {
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
    public Bitmap getQrImage() {
        return this.qrImage;
    }


    /**
     * Returns the generated QR code.
     *s
     * @return The generated QR code bitmap Image.
     */
    public Bitmap getQrImage() { return this.qrImage;}



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

    /**
     * Retrieves the event ID associated with a given QR code hash from Firestore.
     *
     * @param hash The hashed QR code to search for.
     * @param callback Callback to handle the async Firestore response with event ID.
     */
    public static void getEventIdFromHash(String hash, EventIdCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("qrCode", hash)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            callback.onEventIdFound(document.getString("eventID"));
                            return;
                        }
                    } else {
                        callback.onEventIdNotFound();
                    }
                });
    }

    // Callback interface for Firestore query response
    public interface EventIdCallback {
        void onEventIdFound(String eventId);
        void onEventIdNotFound();
    }
}