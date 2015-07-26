/*
 * Copyright (C) 2016 TurboROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.turbo;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.internal.logging.MetricsLogger;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.internal.logging.MetricsLogger;

public class SBTemperature extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String STATUS_BAR_TEMPERATURE_STYLE = "status_bar_temperature_style";
    private static final String STATUS_BAR_TEMPERATURE = "status_bar_temperature";

    private ListPreference mStatusBarTemperature;
    private ListPreference mStatusBarTemperatureStyle;
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_temperature);

        PreferenceScreen prefSet = getPreferenceScreen();
        
        // Temperature
        mStatusBarTemperature = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE);
        int temperatureShow = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0);
        mStatusBarTemperature.setValue(String.valueOf(temperatureShow));
        mStatusBarTemperature.setSummary(mStatusBarTemperature.getEntry());
        mStatusBarTemperature.setOnPreferenceChangeListener(this);
        
        mStatusBarTemperatureStyle = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE_STYLE);
        int temperatureStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_WEATHER_TEMP_STYLE, 0);
        mStatusBarTemperatureStyle.setValue(String.valueOf(temperatureStyle));
        mStatusBarTemperatureStyle.setSummary(mStatusBarTemperatureStyle.getEntry());
        mStatusBarTemperatureStyle.setOnPreferenceChangeListener(this);

        enableStatusBarTemperatureDependents();
    }
    
    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mStatusBarTemperature) {
            int temperatureShow = Integer.valueOf((String) objValue);
            int index = mStatusBarTemperature.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 
                    temperatureShow);
            mStatusBarTemperature.setSummary(
                    mStatusBarTemperature.getEntries()[index]);
            enableStatusBarTemperatureDependents();
            return true;
        } else if (preference == mStatusBarTemperatureStyle) {
            int temperatureStyle = Integer.valueOf((String) objValue);
            int index = mStatusBarTemperatureStyle.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_TEMP_STYLE,
                    temperatureStyle);
            mStatusBarTemperatureStyle.setSummary(
                    mStatusBarTemperatureStyle.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void enableStatusBarTemperatureDependents() {
        int temperatureShow = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP,0);
        if (temperatureShow == 0) {
            mStatusBarTemperatureStyle.setEnabled(false);
        } else {
            mStatusBarTemperatureStyle.setEnabled(true);
        }
    }
}
