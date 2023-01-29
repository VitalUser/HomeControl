package com.vital.homecontrol;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Objects;

// https://developer.android.com/guide/topics/ui/settings


public class SettingActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(this).getString("key_theme", ""))){
            case "Dark":
                setTheme(R.style.AppThemeDark);
                break;
            case "Light":
                setTheme(R.style.AppTheme);
                break;
        }
        super.onCreate(savedInstanceState);

        SettingsFragment stFr = new SettingsFragment();

        Bundle arg = getIntent().getExtras();
        if (arg != null) {
            boolean isDev = arg.getBoolean("IsDev");
            Bundle bundle = new Bundle();
            bundle.putBoolean("IsDev", isDev);
            stFr.setArguments(bundle);
        }

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, stFr)
                .commit();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}



