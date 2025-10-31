package io.nememe.app.ui.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import io.nememe.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
