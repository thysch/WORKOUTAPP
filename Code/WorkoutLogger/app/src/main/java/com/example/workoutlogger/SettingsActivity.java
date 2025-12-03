package com.example.workoutlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private EditText nameEditText;
    private RadioGroup distanceUnitsRadioGroup, weightUnitsRadioGroup;
    private ImageView profileImageView;
    private ActivityResultLauncher<String> selectImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameEditText = findViewById(R.id.name_edit_text);
        distanceUnitsRadioGroup = findViewById(R.id.distance_units_radio_group);
        weightUnitsRadioGroup = findViewById(R.id.weight_units_radio_group);
        profileImageView = findViewById(R.id.profile_image_view);

        selectImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Persist permission to access the URI across device restarts
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);

                            profileImageView.setImageURI(uri);
                            sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to get permission for the image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Button changePhotoButton = findViewById(R.id.change_photo_button);
        changePhotoButton.setOnClickListener(v -> {
            selectImageLauncher.launch("image/*");
        });

        loadSettings();
    }

    private void loadSettings() {
        String name = sharedPreferences.getString("user_name", "Tim S");
        String distanceUnit = sharedPreferences.getString("distance_unit", "mi");
        String weightUnit = sharedPreferences.getString("weight_unit", "lbs");
        String imageUriString = sharedPreferences.getString("profile_image_uri", null);

        nameEditText.setText(name);

        if (imageUriString != null) {
            profileImageView.setImageURI(Uri.parse(imageUriString));
        }

        if ("mi".equals(distanceUnit)) {
            distanceUnitsRadioGroup.check(R.id.mi_radio_button);
        } else {
            distanceUnitsRadioGroup.check(R.id.km_radio_button);
        }

        if ("lbs".equals(weightUnit)) {
            weightUnitsRadioGroup.check(R.id.lbs_radio_button);
        } else {
            weightUnitsRadioGroup.check(R.id.kg_radio_button);
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("user_name", nameEditText.getText().toString());

        int selectedDistanceId = distanceUnitsRadioGroup.getCheckedRadioButtonId();
        if (selectedDistanceId == R.id.mi_radio_button) {
            editor.putString("distance_unit", "mi");
        } else {
            editor.putString("distance_unit", "km");
        }

        int selectedWeightId = weightUnitsRadioGroup.getCheckedRadioButtonId();
        if (selectedWeightId == R.id.lbs_radio_button) {
            editor.putString("weight_unit", "lbs");
        } else {
            editor.putString("weight_unit", "kg");
        }

        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
