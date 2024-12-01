package com.example.fusion0.models;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * An implementation of {@link TextWatcher}
 * @author Nimi Akinroye
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    /**
     * Called before text-changed
     * @param s char sequence being modified
     * @param start where to start
     * @param count number of characters to be replaced
     * @param after number of characters to replace with
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }

    /**
     * Called when text is being changed
     * @param s char sequence being modified
     * @param start where to start
     * @param count number of characters to be replaced
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // No-op
    }

    /**
     * Called after the text has been changed
     * @param s text after the change
     */
    @Override
    public abstract void afterTextChanged(Editable s);
}