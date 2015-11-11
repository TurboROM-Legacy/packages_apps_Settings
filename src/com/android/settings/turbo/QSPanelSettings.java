/*
 * Copyright (C) 2014 The CyanogenMod Project
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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.turbo.qs.QSTiles;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.MetricsLogger;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

public class QSPanelSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PRE_QUICK_PULLDOWN = "quick_pulldown";
    private static final String QS_ORDER = "qs_order";

    private ListPreference mQuickPulldown;
    private Preference mQSTiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_settings_panel);

        PreferenceScreen prefSet = getPreferenceScreen();

        mQuickPulldown = (ListPreference) findPreference(PRE_QUICK_PULLDOWN);
        if (!Utils.isPhone(getActivity()) && !Utils.isTablet(getActivity())) {
            prefSet.removePreference(mQuickPulldown);
        } else {
            // Quick Pulldown
            mQuickPulldown.setOnPreferenceChangeListener(this);
            int statusQuickPulldown = Settings.System.getInt(getContentResolver(),
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1);
            mQuickPulldown.setValue(String.valueOf(statusQuickPulldown));
            updateQuickPulldownSummary(statusQuickPulldown);
        }

    }

    @Override
    protected int getMetricsCategory() {
        return -1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        mQSTiles = prefSet.findPreference(QS_ORDER);

        ContentResolver resolver = getActivity().getContentResolver();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
		if (preference == mQuickPulldown) {
            int statusQuickPulldown = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    statusQuickPulldown);
            updateQuickPulldownSummary(statusQuickPulldown);
            return true;
        }
        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        Resources res = getResources();

       if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else {
            Locale l = Locale.getDefault();
            boolean isRtl = TextUtils.getLayoutDirectionFromLocale(l) == View.LAYOUT_DIRECTION_RTL;
            String direction = res.getString(value == 2
                    ? (isRtl ? R.string.quick_pulldown_right : R.string.quick_pulldown_left)
                    : (isRtl ? R.string.quick_pulldown_left : R.string.quick_pulldown_right));
            mQuickPulldown.setSummary(res.getString(R.string.summary_quick_pulldown, direction));
        }
    }
}
