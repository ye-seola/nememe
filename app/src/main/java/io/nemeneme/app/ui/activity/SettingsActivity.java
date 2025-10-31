package io.nemeneme.app.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.nemeneme.R;
import io.nemeneme.app.ui.fragment.SettingsFragment;
import io.nemeneme.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_content, new SettingsFragment())
                .commit();
    }
}