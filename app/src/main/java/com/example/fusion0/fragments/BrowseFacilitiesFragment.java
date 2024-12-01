package com.example.fusion0.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.fusion0.R;
import com.example.fusion0.adapters.FacilityArrayAdapter;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.FacilitiesInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Browse all facilities
 * @author Malshaan
 */
public class BrowseFacilitiesFragment extends Fragment {

    private ListView facilityListView;
    private FacilityArrayAdapter facilityArrayAdapter;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_facilities, container, false);

        facilityListView = view.findViewById(R.id.facilityListView);
        backButton = view.findViewById(R.id.goBackButton);

        ArrayList<FacilitiesInfo> facilitiesList = new ArrayList<>();

        // Set up the FacilityArrayAdapter
        facilityArrayAdapter = new FacilityArrayAdapter(requireContext(), facilitiesList, this::onEditFacility);
        facilityListView.setAdapter(facilityArrayAdapter);

        getFacilities();

        // Set up the Go Back button
        backButton.setOnClickListener(v -> navigateUp());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the facilities list when returning to this fragment
        getFacilities();
    }

    /**
     * Get all facilities
     */
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
                Toast.makeText(requireContext(), "Failed to load facilities: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Allow facility to be edited
     * @param facility facility to be added
     */
    private void onEditFacility(FacilitiesInfo facility) {
        String facilityId = facility.getFacilityID();
        if (facilityId == null) {
            throw new NullPointerException("Facility ID is null when attempting to edit facility: " + facility.getFacilityName());
        }

        // Navigate to EditFacilityFragment using the NavController
        Bundle bundle = new Bundle();
        bundle.putString("facilityID", facilityId); // Pass facility ID to the fragment
        Navigation.findNavController(requireView()).navigate(R.id.action_browseFacilitiesFragment_to_EditFacilityFragment, bundle);
    }

    /**
     * Go up
     */
    private void navigateUp() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            requireActivity().onBackPressed();
        }
    }
}
