/*
 * Copyright (C) 2016 Turbo ROM
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

import com.android.internal.logging.MetricsLogger;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.preference.SystemSettingSwitchPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockScreenSettings extends SettingsPreferenceFragment 
	implements OnPreferenceChangeListener  {

    private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String LSWEATHER = "ls_weather";

    ListPreference mLockClockFonts;
    private PreferenceScreen mLsWeather;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.lockscreen_settings);

        mLsWeather = (PreferenceScreen)findPreference(LSWEATHER);

        ContentResolver resolver = getActivity().getContentResolver();

        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
	    resolver, Settings.System.LOCK_CLOCK_FONTS, 4)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);   
     }

     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mLockClockFonts) {
            Settings.System.putInt(resolver, Settings.System.LOCK_CLOCK_FONTS,
		Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        }
        return false;
    }
    
    @Override
    protected int getMetricsCategory()
    {
	return MetricsLogger.APPLICATION;
    }
}
