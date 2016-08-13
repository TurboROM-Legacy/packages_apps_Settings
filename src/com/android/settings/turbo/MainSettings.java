/*
 * Copyright (C) 2016 Turbo ROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.turbo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;

public class MainSettings extends SettingsPreferenceFragment {

    private static final String LOCKCLOCK_WEATHER = "lockclock_weather";

    private Preference mWeatherSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.turbo_main_settings);

        mWeatherSettings = (Preference) findPreference(LOCKCLOCK_WEATHER);
    }

    @Override
    protected int getMetricsCategory() {
	return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mWeatherSettings) {
            launchWeatherSettings();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchWeatherSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.cyanogenmod.lockclock", "com.cyanogenmod.lockclock.preference.Preferences"));
        intent.putExtra(":android:show_fragment", "com.cyanogenmod.lockclock.preference.WeatherPreferences");
        startActivity(intent);
    }
}
