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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarLogoSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "StatusBarLogoSettings";

    private static final String KEY_TURBO_LOGO_COLOR = "status_bar_turbo_logo_color";
    private static final String KEY_TURBO_LOGO_STYLE = "status_bar_turbo_logo_style";

    private ColorPickerPreference mTurboLogoColor;
    private ListPreference mTurboLogoStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_logo);
        PreferenceScreen prefSet = getPreferenceScreen();
		ContentResolver resolver = mContext.getContentResolver();
         // Turbo logo Style
	mTurboLogoStyle = (ListPreference) findPreference(KEY_TURBO_LOGO_STYLE);
	    int turboLogoStyle = Settings.System.getIntForUser(resolver,
	    Settings.System.STATUS_BAR_TURBO_LOGO_STYLE, 0, UserHandle.USER_CURRENT);
	mTurboLogoStyle.setValue(String.valueOf(turboLogoStyle));
	mTurboLogoStyle.setSummary(mTurboLogoStyle.getEntry());
	mTurboLogoStyle.setOnPreferenceChangeListener(this);

        // Turbo logo color
	mTurboLogoColor =
	    (ColorPickerPreference) prefSet.findPreference(KEY_TURBO_LOGO_COLOR);
	mTurboLogoColor.setOnPreferenceChangeListener(this);
	int intColor = Settings.System.getInt(getContentResolver(),
	    Settings.System.STATUS_BAR_TURBO_LOGO_COLOR, 0xffffffff);
	String hexColor = String.format("#%08x", (0xffffffff & intColor));
	    mTurboLogoColor.setSummary(hexColor);
            mTurboLogoColor.setNewPreviewColor(intColor);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = mContext.getContentResolver();
	if (preference == mTurboLogoColor) {
            String hex = ColorPickerPreference.convertToARGB(
		Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
		Settings.System.STATUS_BAR_TURBO_LOGO_COLOR, intHex);
            return true;
	} else if (preference == mTurboLogoStyle) {
	    int turboLogoStyle = Integer.valueOf((String) newValue);
	    int index = mTurboLogoStyle.findIndexOfValue((String) newValue);
	    Settings.System.putIntForUser(resolver, 
		Settings.System.STATUS_BAR_TURBO_LOGO_STYLE, turboLogoStyle, UserHandle.USER_CURRENT);
	    mTurboLogoStyle.setSummary(
	        mTurboLogoStyle.getEntries()[index]);
	    return true;
        }
        return false;
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
