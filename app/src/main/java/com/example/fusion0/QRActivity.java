package com.example.fusion0;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * QRActivity is an activity that uses the device's camera to scan QR codes.
 * It utilizes the ZXing library for scanning and processing QR codes, returning
 * the hashed QR code string and the associated event ID if found in Firestore.
 */
public class QRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private Button cancelButton;
    private TextView instructionText;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    /**
     * Called when the activity is starting. Initializes the layout and views,
     * and requests camera permission if not already granted.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the most recent data. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner); // Make sure this matches your actual layout file name

        // Initialize views
        scannerView = findViewById(R.id.camera_preview);
        instructionText = findViewById(R.id.instruction_text);
        cancelButton = findViewById(R.id.cancel_button);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanner();
        }

        // Set cancel button action
        cancelButton.setOnClickListener(view -> finish());
    }

    /**
     * Starts the camera and sets up the scanner view to handle QR code scanning results.
     */
    private void startScanner() {
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    /**
     * Handles the result of the QR code scan. This method is triggered when a QR code
     * is successfully scanned.
     *
     * @param rawResult The result of the QR scan, containing the QR code data.
     */
    @Override
    public void handleResult(Result rawResult) {
        String scannedData = rawResult.getText();
        String hash = QRCode.generateHash(scannedData);

        // Use the QRCode class to find the associated event by hash
        QRCode.getEventIdFromHash(scannedData, new QRCode.EventIdCallback() {
            @Override
            public void onEventIdFound(String eventId) {
                Toast.makeText(QRActivity.this, "Event ID: " + eventId, Toast.LENGTH_LONG).show();
                finishWithResult(eventId, hash);

                Intent intent = new Intent(QRActivity.this, ViewEventActivity.class);
                intent.putExtra("eventID", eventId);
                startActivity(intent);
            }

            @Override
            public void onEventIdNotFound() {
                Toast.makeText(QRActivity.this, "No event found for scanned QR", Toast.LENGTH_LONG).show();
                scannerView.resumeCameraPreview(QRActivity.this);
            }
        });
    }

    /**
     * Finishes the activity and returns the scanned QR code data and event ID as a result.
     *
     * @param eventId     The ID of the event associated with the QR code.
     * @param qrCodeHash  The hashed QR code data.
     */
    private void finishWithResult(String eventId, String qrCodeHash) {
        Intent intent = new Intent();
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("QR_CODE_HASH", qrCodeHash);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when the activity is paused. Stops the camera to free resources.
     */
    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    /**
     * Called when the activity is resumed. Restarts the camera if the necessary
     * permission has been granted.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }
    }

    /**
     * Called when the user responds to the camera permission request.
     * If permission is granted, the camera scanner starts; otherwise, the activity finishes.
     *
     * @param requestCode  The request code passed in requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the requested permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Ensure super is called
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
