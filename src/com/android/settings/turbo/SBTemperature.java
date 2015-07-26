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
import com.android.settings.preference.SeekBarPreference;
import com.android.settings.Utils;

import com.android.internal.logging.MetricsLogger;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.internal.logging.MetricsLogger;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class SBTemperature extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String STATUS_BAR_TEMPERATURE_STYLE = "status_bar_temperature_style";
    private static final String STATUS_BAR_TEMPERATURE = "status_bar_temperature";
    private static final String PREF_STATUS_BAR_WEATHER_COLOR = "status_bar_weather_color";
    private static final String PREF_STATUS_BAR_WEATHER_SIZE = "status_bar_weather_size";

    private ListPreference mStatusBarTemperature;
    private ListPreference mStatusBarTemperatureStyle;
    private ColorPickerPreference mStatusBarTemperatureColor;
    private SeekBarPreference mStatusBarTemperatureSize;
     
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

        mStatusBarTemperatureColor =
            (ColorPickerPreference) findPreference(PREF_STATUS_BAR_WEATHER_COLOR);
        mStatusBarTemperatureColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_WEATHER_COLOR, 0xffffffff);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mStatusBarTemperatureColor.setSummary(hexColor);
            mStatusBarTemperatureColor.setNewPreviewColor(intColor);

        mStatusBarTemperatureSize = (SeekBarPreference) findPreference(PREF_STATUS_BAR_WEATHER_SIZE);
        mStatusBarTemperatureSize.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_WEATHER_SIZE, 14));
        mStatusBarTemperatureSize.setOnPreferenceChangeListener(this);

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
        } else if (preference == mStatusBarTemperatureColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_COLOR, intHex);
            return true;
        } else if (preference == mStatusBarTemperatureSize) {
            int width = ((Integer)objValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_WEATHER_SIZE, width);
            return true;
        }
        return false;
    }

    private void enableStatusBarTemperatureDependents() {
        int temperatureShow = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP,0);
        if (temperatureShow == 0) {
            mStatusBarTemperatureStyle.setEnabled(false);
            mStatusBarTemperatureColor.setEnabled(false);
            mStatusBarTemperatureSize.setEnabled(false);
        } else {
            mStatusBarTemperatureStyle.setEnabled(true);
            mStatusBarTemperatureColor.setEnabled(true);
            mStatusBarTemperatureSize.setEnabled(true);
        }
    }
}
