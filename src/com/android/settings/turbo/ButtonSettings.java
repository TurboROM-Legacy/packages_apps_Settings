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

import android.content.Context;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.view.IWindowManager;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ButtonSettings extends SettingsPreferenceFragment
       implements OnPreferenceChangeListener {

    private static final String KEY_HARDWARE_KEYS = "hardwarekeys_settings";
    private static final String KEY_ADVANCED_REBOOT = "advanced_reboot";

    private ListPreference mAdvancedReboot;

    private boolean mIsPrimary;
    private static final int MY_USER_ID = UserHandle.myUserId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }

        addPreferencesFromResource(R.xml.button_settings);
        root = getPreferenceScreen();
        
        // Hide Hardware Keys menu if device doesn't have any
        PreferenceScreen hardwareKeys = (PreferenceScreen) findPreference(KEY_HARDWARE_KEYS);
        int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        if (deviceKeys == 0 && hardwareKeys != null) {
            getPreferenceScreen().removePreference(hardwareKeys);
        }

        mIsPrimary = MY_USER_ID == UserHandle.USER_OWNER;

        mAdvancedReboot = (ListPreference) root.findPreference(KEY_ADVANCED_REBOOT);
        if (mIsPrimary) {
            mAdvancedReboot.setValue(String.valueOf(Settings.Secure.getInt(
                    getContentResolver(), Settings.Secure.ADVANCED_REBOOT, 0)));
            mAdvancedReboot.setOnPreferenceChangeListener(this);
        } else {
            root.removePreference(mAdvancedReboot);
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
        }
        return result;
    }
}
