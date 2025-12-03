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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements ColorPickerAdapter.OnColorSelectedListener {

    private SharedPreferences sharedPreferences;
    private EditText nameEditText;
    private RadioGroup distanceUnitsRadioGroup, weightUnitsRadioGroup;
    private ImageView profileImageView;
    private ActivityResultLauncher<String> selectImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
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
        changePhotoButton.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

        setupColorPicker();
        loadSettings();
    }

    private void setupColorPicker() {
        RecyclerView colorPickerRecyclerView = findViewById(R.id.color_picker_recycler_view);
        colorPickerRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        List<Integer> colors = Arrays.asList(
                R.color.theme_red, R.color.theme_pink, R.color.theme_purple, R.color.theme_deep_purple, R.color.theme_indigo,
                R.color.theme_blue, R.color.theme_teal, R.color.theme_green, R.color.theme_orange, R.color.theme_brown
        );

        int selectedColor = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        ColorPickerAdapter adapter = new ColorPickerAdapter(this, colors, selectedColor, this);
        colorPickerRecyclerView.setAdapter(adapter);
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
    public void onColorSelected(int colorResId) {
        sharedPreferences.edit().putInt("selected_theme_color", colorResId).apply();
        recreate();
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        int themeResId = getThemeResId(colorRes);
        setTheme(themeResId);
    }

    private int getThemeResId(int colorRes) {
        if (colorRes == R.color.theme_red) return R.style.Theme_WorkoutLogger_Red;
        if (colorRes == R.color.theme_pink) return R.style.Theme_WorkoutLogger_Pink;
        if (colorRes == R.color.theme_purple) return R.style.Theme_WorkoutLogger_Purple;
        if (colorRes == R.color.theme_deep_purple) return R.style.Theme_WorkoutLogger_DeepPurple;
        if (colorRes == R.color.theme_indigo) return R.style.Theme_WorkoutLogger_Indigo;
        if (colorRes == R.color.theme_blue) return R.style.Theme_WorkoutLogger_Blue;
        if (colorRes == R.color.theme_teal) return R.style.Theme_WorkoutLogger_Teal;
        if (colorRes == R.color.theme_green) return R.style.Theme_WorkoutLogger_Green;
        if (colorRes == R.color.theme_orange) return R.style.Theme_WorkoutLogger_Orange;
        if (colorRes == R.color.theme_brown) return R.style.Theme_WorkoutLogger_Brown;
        return R.style.Theme_WorkoutLogger;
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
