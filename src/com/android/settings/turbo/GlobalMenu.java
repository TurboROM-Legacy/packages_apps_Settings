/*
 * Copyright (C) 2016 TurboROM
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.Utils;

public class GlobalMenu extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String KEY_ADVANCED_REBOOT = "advanced_reboot";

    private ListPreference mAdvancedReboot;
    private static final int MY_USER_ID = UserHandle.myUserId();
    private boolean mIsPrimary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.global_menu);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

 	mIsPrimary = MY_USER_ID == UserHandle.USER_OWNER;

        mAdvancedReboot = (ListPreference) findPreference(KEY_ADVANCED_REBOOT);
        if (mIsPrimary) {
            mAdvancedReboot.setValue(String.valueOf(Settings.Secure.getInt(
                    getContentResolver(), Settings.Secure.ADVANCED_REBOOT, 2)));
            mAdvancedReboot.setSummary(mAdvancedReboot.getEntry());
            mAdvancedReboot.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mAdvancedReboot);
        }
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
    public boolean onPreferenceChange(Preference preference, Object value) {
 	boolean result = true;
        if (preference == mAdvancedReboot) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADVANCED_REBOOT,
                    Integer.valueOf((String) value));
            mAdvancedReboot.setValue(String.valueOf(value));
            mAdvancedReboot.setSummary(mAdvancedReboot.getEntry());
        }
        return result;
    }
}
