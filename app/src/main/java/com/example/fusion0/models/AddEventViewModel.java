package com.example.fusion0.models;

import androidx.lifecycle.ViewModel;

import com.example.fusion0.helpers.AddEventHelper;

/**
 * ViewModel for AddEventHelper for adding events
 * @author Nimi Akinroye
 */
public class AddEventViewModel extends ViewModel {
    private AddEventHelper helper;

    /**
     * Initialize with default values
     * @author Nimi Akinroye
     * @param helper the helper object to initialize
     */
    public void initializeHelper(AddEventHelper helper) {
        this.helper = helper;
    }

    /**
     * Gets helper
     * @author Nimi Akinroye
     * @return the helper
     */
    public AddEventHelper getHelper() {
        return helper;
    }

    /**
     * Updates helper
     * @author Nimi Akinroye
     * @param newHelper helper to change
     */
    public void updateHelper(AddEventHelper newHelper) {
        this.helper = newHelper;
    }
}
