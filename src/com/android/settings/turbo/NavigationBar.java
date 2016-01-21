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
import android.preference.SlimSeekBarPreference;
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
    private static final String KEY_CATEGORY_NAVIGATION_SLIMDIM = "dim_nav_buttons_cat";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String PREF_MENU_LOCATION = "pref_navbar_menu_location";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "pref_navbar_menu_display";
    private static final String PREF_BUTTON = "navbar_button_settings";
    private static final String PREF_BUTTON_STYLE = "nav_bar_button_style";
    private static final String PREF_STYLE_DIMEN = "navbar_style_dimen_settings";
    private static final String PREF_NAVIGATION_BAR_CAN_MOVE = "navbar_can_move";
    private static final String STATUS_BAR_IME_ARROWS = "status_bar_ime_arrows";
    private static final String DIM_NAV_BUTTONS = "dim_nav_buttons";
    private static final String DIM_NAV_BUTTONS_TOUCH_ANYWHERE = "dim_nav_buttons_touch_anywhere";
    private static final String DIM_NAV_BUTTONS_TIMEOUT = "dim_nav_buttons_timeout";
    private static final String DIM_NAV_BUTTONS_ALPHA = "dim_nav_buttons_alpha";
    private static final String DIM_NAV_BUTTONS_ANIMATE = "dim_nav_buttons_animate";
    private static final String DIM_NAV_BUTTONS_ANIMATE_DURATION = "dim_nav_buttons_animate_duration";

    private int mNavBarMenuDisplayValue;

    SwitchPreference mEnableNavigationBar;
    ListPreference mMenuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    SwitchPreference mNavigationBarCanMove;
    PreferenceScreen mButtonPreference;
    PreferenceScreen mButtonStylePreference;
    PreferenceScreen mStyleDimenPreference;
    SwitchPreference mStatusBarImeArrows;
    SwitchPreference mDimNavButtons;
    SwitchPreference mDimNavButtonsTouchAnywhere;
    SlimSeekBarPreference mDimNavButtonsTimeout;
    SlimSeekBarPreference mDimNavButtonsAlpha;
    SwitchPreference mDimNavButtonsAnimate;
    SlimSeekBarPreference mDimNavButtonsAnimateDuration;
    private PreferenceCategory mNavGeneral;
    private PreferenceCategory mNavAdvanced;
    private PreferenceCategory mNavSlimDim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigation_bar);

        PreferenceScreen prefs = getPreferenceScreen();

        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
        mNavAdvanced = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_ADVANCED);
        mNavSlimDim = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_SLIMDIM);
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

        mDimNavButtons = (SwitchPreference) findPreference(DIM_NAV_BUTTONS);
        mDimNavButtons.setOnPreferenceChangeListener(this);

        mDimNavButtonsTouchAnywhere = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_TOUCH_ANYWHERE);
        mDimNavButtonsTouchAnywhere.setOnPreferenceChangeListener(this);

        mDimNavButtonsTimeout = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_TIMEOUT);
        mDimNavButtonsTimeout.setDefault(3000);
        mDimNavButtonsTimeout.isMilliseconds(true);
        mDimNavButtonsTimeout.setInterval(1);
        mDimNavButtonsTimeout.minimumValue(100);
        mDimNavButtonsTimeout.multiplyValue(100);
        mDimNavButtonsTimeout.setOnPreferenceChangeListener(this);

        mDimNavButtonsAlpha = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ALPHA);
        mDimNavButtonsAlpha.setDefault(50);
        mDimNavButtonsAlpha.setInterval(1);
        mDimNavButtonsAlpha.setOnPreferenceChangeListener(this);

        mDimNavButtonsAnimate = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE);
        mDimNavButtonsAnimate.setOnPreferenceChangeListener(this);

        mDimNavButtonsAnimateDuration = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE_DURATION);
        mDimNavButtonsAnimateDuration.setDefault(2000);
        mDimNavButtonsAnimateDuration.isMilliseconds(true);
        mDimNavButtonsAnimateDuration.setInterval(1);
        mDimNavButtonsAnimateDuration.minimumValue(100);
        mDimNavButtonsAnimateDuration.multiplyValue(100);
        mDimNavButtonsAnimateDuration.setOnPreferenceChangeListener(this);

        if (mDimNavButtons != null) {
            mDimNavButtons.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS, 0) == 1);
        }

        if (mDimNavButtonsTouchAnywhere != null) {
            mDimNavButtonsTouchAnywhere.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_TOUCH_ANYWHERE, 0) == 1);
        }

        if (mDimNavButtonsTimeout != null) {
            final int dimTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_TIMEOUT, 3000);
            // minimum 100 is 1 interval of the 100 multiplier
            mDimNavButtonsTimeout.setInitValue((dimTimeout / 100) - 1);
        }

        if (mDimNavButtonsAlpha != null) {
            int alphaScale = Settings.System.getInt(getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_ALPHA, 50);
            mDimNavButtonsAlpha.setInitValue(alphaScale);
        }

        if (mDimNavButtonsAnimate != null) {
            mDimNavButtonsAnimate.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_ANIMATE, 0) == 1);
        }

        if (mDimNavButtonsAnimateDuration != null) {
            final int animateDuration = Settings.System.getInt(getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_ANIMATE_DURATION, 2000);
            // minimum 100 is 1 interval of the 100 multiplier
            mDimNavButtonsAnimateDuration.setInitValue((animateDuration / 100) - 1);
        }
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mEnableNavigationBar.setChecked(showing);
        mNavGeneral.setEnabled(mEnableNavigationBar.isChecked());
        mNavAdvanced.setEnabled(mEnableNavigationBar.isChecked());
        mNavSlimDim.setEnabled(mEnableNavigationBar.isChecked());
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
        } else if (preference == mDimNavButtons) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsTouchAnywhere) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_TOUCH_ANYWHERE,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsTimeout) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_TIMEOUT, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mDimNavButtonsAlpha) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_ALPHA, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mDimNavButtonsAnimate) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_ANIMATE,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsAnimateDuration) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_ANIMATE_DURATION,
                Integer.parseInt((String) newValue));
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
