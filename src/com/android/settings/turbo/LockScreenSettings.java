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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.IWindowManager;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockScreenSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEYGUARD_CLOCK_FONT = "keyguard_clock_font";

    private ListPreference mKeyguardClockFont;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.lock_screen_settings);
        prefSet = getPreferenceScreen();

        mKeyguardClockFont = (ListPreference) findPreference(KEYGUARD_CLOCK_FONT);
        mKeyguardClockFont.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.LOCK_SCREEN_CLOCK_FONT, 4)));
        mKeyguardClockFont.setSummary(mKeyguardClockFont.getEntry());
        mKeyguardClockFont.setOnPreferenceChangeListener(this);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mKeyguardClockFont) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_SCREEN_CLOCK_FONT,
                    Integer.valueOf((String) newValue));
            mKeyguardClockFont.setValue(String.valueOf(newValue));
            mKeyguardClockFont.setSummary(mKeyguardClockFont.getEntry());
            return true;
         }
         return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
