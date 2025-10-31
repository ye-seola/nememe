package io.nememe.app.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.nememe.R;
import io.nememe.app.ui.fragment.SettingsFragment;
import io.nememe.databinding.ActivitySettingsBinding;

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