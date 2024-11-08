package com.example.fusion0;

//Javadocs provided by chatGPT

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
 * /**
 *  * Represents a QR code object that is uniquely generated based on an event ID.
 *  * The QR code can be used to identify specific events and retrieve associated
 *  * event data from Firestore. This class provides methods for generating the
 *  * QR code as a hashed string and an image, as well as retrieving an event ID
 *  * from a QR code hash.
 * @author Malshaan
 */
public class QRCode {
    private String qrCode;
    private Bitmap qrImage;  // The generated QR code


    /**
     * Constructs a QRCode object for a specified event ID.
     * This constructor initializes the QR code as a hashed string and generates
     * the QR code image as a Bitmap.
     *
     * @param eventId The ID of the event for which the QR code is generated.
     * @throws WriterException If an error occurs during QR code image generation.
     */
    public QRCode(String eventId) throws WriterException {
        this.qrCode = generateHash(eventId);
        this.qrImage = generateQRCodeImage(500, 500, this.qrCode);  // Generate QR code based on event ID
    }

    /**
     * Generates a hashed QR code string using the SHA-256 algorithm.
     * This method takes an input string, typically an event ID, and returns a
     * hashed representation to uniquely identify the event.
     *
     * @param input The input string to be hashed (e.g., an event ID).
     * @return A hashed string representing the QR code.
     * @throws RuntimeException if SHA-256 algorithm is unavailable.
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
     * Returns the hashed QR code string.
     *
     * @return The hashed string that represents the QR code.
     */
    public String getQrCode() {
        return this.qrCode;
    }




    /**
     * Returns the generated QR code image as a  Bitmap.
     *
     * @return The QR code image as a Bitmap.
     */
    public Bitmap getQrImage() { return this.qrImage;}

    /**
     * Generates a QR code image from the given QR code string.
     * This method uses ZXing to create a Bitmap from the hashed QR code string.
     *
     * @param width  The width of the QR code image.
     * @param height The height of the QR code image.
     * @param qrCode The hashed string to be encoded into a QR code image.
     * @return A Bitmap representation of the QR code.
     * @throws WriterException If an error occurs during QR code image generation.
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
     * Retrieves the event ID associated with a specified QR code hash from Firestore.
     * This method performs an asynchronous Firestore query to find the event associated
     * with the given QR code hash. The result is provided via the EventIdCallback.
     *
     * @param hash     The hashed QR code string to search for in Firestore.
     * @param callback Callback interface to handle the Firestore response.
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

    /**
     * Callback interface for handling the Firestore response for an event ID lookup.
     * Implement this interface to define actions to take when an event ID is found
     * or not found in Firestore.
     */
    public interface EventIdCallback {
        void onEventIdFound(String eventId);
        void onEventIdNotFound();
    }
}