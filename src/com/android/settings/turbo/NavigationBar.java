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

import java.util.List;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.slim.DeviceUtils;
import com.android.internal.util.turbo.TurboActionUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class NavigationBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = NavigationBar.class.getSimpleName();

    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_CATEGORY_NAVIGATION_ADVANCED = "category_navbar_advanced";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String PREF_MENU_LOCATION = "pref_navbar_menu_location";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "pref_navbar_menu_display";
    private static final String PREF_BUTTON = "navbar_button_settings";
    private static final String PREF_BUTTON_STYLE = "nav_bar_button_style";
    private static final String PREF_STYLE_DIMEN = "navbar_style_dimen_settings";
    private static final String PREF_NAVIGATION_BAR_CAN_MOVE = "navbar_can_move";
    private static final String STATUS_BAR_IME_ARROWS = "status_bar_ime_arrows";

    private int mNavBarMenuDisplayValue;

    SwitchPreference mEnableNavigationBar;
    ListPreference mMenuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    SwitchPreference mNavigationBarCanMove;
    PreferenceScreen mButtonPreference;
    PreferenceScreen mButtonStylePreference;
    PreferenceScreen mStyleDimenPreference;
    SwitchPreference mStatusBarImeArrows;
    private PreferenceCategory mNavGeneral;
    private PreferenceCategory mNavAdvanced;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar);

        PreferenceScreen prefs = getPreferenceScreen();

        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
        mNavAdvanced = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_ADVANCED);
	mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAVIGATION_BAR);

        boolean showing = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW,
                TurboActionUtils.hasNavbarByDefault(getActivity()) ? 1 : 0) != 0;
        updateBarVisibleAndUpdatePrefs(showing);
	mEnableNavigationBar.setOnPreferenceChangeListener(this);

        // Navigation bar left-in-landscape
        // remove if not a phone
        if (!TurboActionUtils.isNormalScreen()) {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_BAR_LEFT));
        }
        mMenuDisplayLocation = (ListPreference) findPreference(PREF_MENU_LOCATION);
        mMenuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_LOCATION,
                0) + "");
        mMenuDisplayLocation.setOnPreferenceChangeListener(this);

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplayValue = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_VISIBILITY,
                2);
        mNavBarMenuDisplay.setValue(mNavBarMenuDisplayValue + "");
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);

        mButtonPreference = (PreferenceScreen) findPreference(PREF_BUTTON);
        mButtonStylePreference = (PreferenceScreen) findPreference(PREF_BUTTON_STYLE);
        mStyleDimenPreference = (PreferenceScreen) findPreference(PREF_STYLE_DIMEN);

        mNavigationBarCanMove = (SwitchPreference) findPreference(PREF_NAVIGATION_BAR_CAN_MOVE);
        mNavigationBarCanMove.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_CAN_MOVE,
                DeviceUtils.isPhone(getActivity()) ? 1 : 0) == 0);
        mNavigationBarCanMove.setOnPreferenceChangeListener(this);

        mStatusBarImeArrows = (SwitchPreference) findPreference(STATUS_BAR_IME_ARROWS);
        mStatusBarImeArrows.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_IME_ARROWS, 0) == 1);
        mStatusBarImeArrows.setOnPreferenceChangeListener(this);
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mEnableNavigationBar.setChecked(showing);
        mNavGeneral.setEnabled(mEnableNavigationBar.isChecked());
        mNavAdvanced.setEnabled(mEnableNavigationBar.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mEnableNavigationBar)) {
            boolean showing = ((Boolean)newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_SHOW,
                    showing ? 1 : 0);
            updateBarVisibleAndUpdatePrefs(showing);
            return true;
        } else if (preference == mMenuDisplayLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            mNavBarMenuDisplayValue = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_VISIBILITY, mNavBarMenuDisplayValue);
            mMenuDisplayLocation.setEnabled(mNavBarMenuDisplayValue != 1);
            return true;
        } else if (preference == mNavigationBarCanMove) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_CAN_MOVE,
                    ((Boolean) newValue) ? 0 : 1);
            return true;
        } else if (preference == mStatusBarImeArrows) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_IME_ARROWS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
        protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }
}
