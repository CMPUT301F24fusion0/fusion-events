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

public class QRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private Button cancelButton;
    private TextView instructionText;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

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

    private void startScanner() {
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String scannedData = rawResult.getText();
        String hash = QRCode.generateHash(scannedData);

        // Use the QRCode class to find the associated event by hash
        QRCode.getEventIdFromHash(hash, new QRCode.EventIdCallback() {
            @Override
            public void onEventIdFound(String eventId) {
                Toast.makeText(QRActivity.this, "Event ID: " + eventId, Toast.LENGTH_LONG).show();
                finishWithResult(eventId, hash);
            }

            @Override
            public void onEventIdNotFound() {
                Toast.makeText(QRActivity.this, "No event found for scanned QR", Toast.LENGTH_LONG).show();
                scannerView.resumeCameraPreview(QRActivity.this);
            }
        });
    }

    private void finishWithResult(String eventId, String qrCodeHash) {
        Intent intent = new Intent();
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("QR_CODE_HASH", qrCodeHash);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }
    }

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
