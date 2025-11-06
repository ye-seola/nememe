package io.nememe.app.ui.fragment;

import android.os.Bundle;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import io.nememe.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        ((EditTextPreference) findPreference("port")).setOnBindEditTextListener((e) -> {
            e.setInputType(InputType.TYPE_CLASS_NUMBER);
        });
    }
}
