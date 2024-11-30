package com.example.fusion0;

import android.util.Log;

import com.example.fusion0.helpers.AddEventHelper;
import com.example.fusion0.models.AddEventViewModel;

import org.junit.Before;
import org.junit.Test;

public class AddEventVMTest {
    AddEventViewModel aevm;
    AddEventHelper aeh;

    @Before
    public void initialize() {
        aevm = new AddEventViewModel();
    }

    @Test
    public void getHelper() {
        aevm.initializeHelper(aeh);
        AddEventHelper getHelper = aevm.getHelper();
        Log.d("sd", getHelper.toString());
    }
}
