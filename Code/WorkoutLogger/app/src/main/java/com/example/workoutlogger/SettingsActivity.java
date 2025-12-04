package com.example.workoutlogger;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SettingsActivity extends AppCompatActivity implements ColorPickerAdapter.OnColorSelectedListener {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;

    private SharedPreferences sharedPreferences;
    private EditText nameEditText;
    private RadioGroup distanceUnitsRadioGroup, weightUnitsRadioGroup;
    private ImageView profileImageView;
    private ActivityResultLauncher<Intent> galleryLauncher;
    // Simple set of example bad words for profanity filtering
    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
            "bad", "word", "example", "profanity"
    ));

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

        Button changePhotoButton = findViewById(R.id.change_photo_button);
        changePhotoButton.setOnClickListener(v -> checkPermissionAndOpenGallery());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();

                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                        sharedPreferences.edit().putString("profile_image_uri", imageUri.toString()).apply();

                        loadAvatar(imageUri);
                    }
                });

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
            Uri imageUri = Uri.parse(imageUriString);
            loadAvatar(imageUri);
        } else {
            profileImageView.setImageResource(R.drawable.ic_default_profile);
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
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty() || isProfane(name)) {
            Toast.makeText(this, "Invalid name. Please choose an appropriate name without profanity.", Toast.LENGTH_SHORT).show();
            // Revert to previous name
            nameEditText.setText(sharedPreferences.getString("user_name", "Tim S"));
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("user_name", name);

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

    private boolean isProfane(String text) {
        String lowerText = text.toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (lowerText.contains(badWord)) {
                return true;
            }
        }
        return false;
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

    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select a profile picture.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadAvatar(Uri uri) {
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile)
                .into(profileImageView);
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