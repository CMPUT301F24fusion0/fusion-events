package com.example.fusion0.models;

import androidx.lifecycle.ViewModel;

import com.example.fusion0.helpers.AddEventHelper;

public class AddEventViewModel extends ViewModel {
    private AddEventHelper helper;

    // Initialize helper with default values
    public void initializeHelper(AddEventHelper helper) {
        this.helper = helper;
    }

    public AddEventHelper getHelper() {
        return helper;
    }

    public void updateHelper(AddEventHelper newHelper) {
        this.helper = newHelper;
    }
}
