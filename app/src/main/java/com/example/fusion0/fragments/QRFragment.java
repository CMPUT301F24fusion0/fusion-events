package com.example.fusion0.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.helpers.QRCode;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * QRFragment is a fragment that uses the device's camera to scan QR codes.
 * It utilizes the ZXing library for scanning and processing QR codes, returning
 * the hashed QR code string and the associated event ID if found in Firestore.
 * @author Malshaan
 */
public class QRFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private Button cancelButton;
    private TextView instructionText;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    public QRFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_q_r, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        scannerView = view.findViewById(R.id.camera_preview);
        instructionText = view.findViewById(R.id.instruction_text);
        cancelButton = view.findViewById(R.id.cancel_button);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanner(context);
        }

        cancelButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_qrFragment_to_mainFragment);
        });

    }

    /**
     * Starts the camera and sets up the scanner view to handle QR code scanning results.
     */
    private void startScanner(Context context) {
        scannerView.setResultHandler(this); // Fix later
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
        Context context = requireContext();
        String scannedData = rawResult.getText();

        // Use the QRCode class to find the associated event by hash
        QRCode.getEventIdFromHash(scannedData, new QRCode.EventIdCallback() {
            @Override
            public void onEventIdFound(String eventId) {
                Toast.makeText(context, "Event ID: " + eventId, Toast.LENGTH_LONG).show();
                finishWithResult(eventId, scannedData);
            }

            @Override
            public void onEventIdNotFound() {
                Toast.makeText(context, "No event found for scanned QR", Toast.LENGTH_LONG).show();
                scannerView.resumeCameraPreview(QRFragment.this);
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
        Activity activity = requireActivity();
        Intent intent = new Intent();
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("QR_CODE_HASH", qrCodeHash);
        activity.setResult(RESULT_OK, intent);
        activity.finish();
    }

    /**
     * Called when the activity is paused. Stops the camera to free resources.
     */
    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    /**
     * Called when the activity is resumed. Restarts the camera if the necessary
     * permission has been granted.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Activity activity = requireActivity();
        Context context = requireContext();

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner(context);
            } else {
                Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        }
    }

}