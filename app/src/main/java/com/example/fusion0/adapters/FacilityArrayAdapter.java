package com.example.fusion0.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fusion0.R;
import com.example.fusion0.models.FacilitiesInfo;

import java.util.ArrayList;

public class FacilityArrayAdapter extends ArrayAdapter<FacilitiesInfo> {

    private final FacilityEditCallback editCallback;

    public FacilityArrayAdapter(Context context, ArrayList<FacilitiesInfo> facilities, FacilityEditCallback editCallback) {
        super(context, 0, facilities);
        this.editCallback = editCallback;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.browse_facility_item, parent, false);
        }

        FacilitiesInfo facility = getItem(position);

        TextView facilityName = view.findViewById(R.id.browseFacilityName);
        Button editButton = view.findViewById(R.id.editFacilityButton);

        if (facility != null) {
            facilityName.setText(facility.getFacilityName());
            editButton.setOnClickListener(v -> {
                if (facility.getFacilityID() == null) {
                    throw new NullPointerException("Facility ID is null for facility: " + facility.getFacilityName());
                }
                editCallback.onEdit(facility);
            });
        }
        return view;
    }

    public interface FacilityEditCallback {
        void onEdit(FacilitiesInfo facility);
    }
}
