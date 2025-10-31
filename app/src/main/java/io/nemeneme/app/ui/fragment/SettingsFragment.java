package io.nemeneme.app.ui.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import io.nemeneme.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
