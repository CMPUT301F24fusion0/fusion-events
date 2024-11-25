package com.example.fusion0.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fusion0.R;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.EventInfo;
import com.example.fusion0.models.FacilitiesInfo;
import com.example.fusion0.adapters.FacilityArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminFacilityActivity extends AppCompatActivity {

    private ListView facilityListView;
    private FacilityArrayAdapter facilityArrayAdapter;
    private Button backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_admin_facilities);

        facilityListView = findViewById(R.id.facilityListView);
        backButton = findViewById(R.id.goBackButton);

        ArrayList<FacilitiesInfo> facilitiesList = new ArrayList<>();

        // Set up the EventArrayAdapter
        facilityArrayAdapter = new FacilityArrayAdapter(this, facilitiesList, this::onEditFacility);
        facilityListView.setAdapter(facilityArrayAdapter);

        getFacilities();



        // Set up the Go Back button
        backButton.setOnClickListener(v -> finish()); // Close this activity and return to the previous screen
    }


        @Override
        protected void onResume() {
            super.onResume();
            //Refresh the events list when returning to this activity
            getFacilities();
        }

        private void getFacilities() {
            EventFirebase.getAllFacilities(new EventFirebase.FacilityListCallBack() {
                @Override
                public void onSuccess(List<FacilitiesInfo> facilities) {
                    facilityArrayAdapter.clear();
                    facilityArrayAdapter.addAll(facilities);
                    facilityArrayAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(AdminFacilityActivity.this, "Failed to load facilities: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void onEditFacility(FacilitiesInfo facility) {
            String facilityId = facility.getFacilityID();
            if (facilityId == null) {
                throw new NullPointerException("Facility ID is null when attempting to edit facility: " + facility.getFacilityName());
            }
            Intent intent = new Intent(AdminFacilityActivity.this, ViewFacilityActivity.class);
            intent.putExtra("facilityId", facilityId);
            startActivity(intent);
        }

}


