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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNotificationSettings extends SettingsPreferenceFragment 
	implements Preference.OnPreferenceChangeListener {

    private static final String BREATHING_NOTIFICATIONS = "breathing_notifications";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String SMS_BREATH = "sms_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";

    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mSmsBreath;
    private SwitchPreference mVoicemailBreath;

    private boolean mCheckPreferences;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCustomView();
    }

    private PreferenceScreen createCustomView() {
        mCheckPreferences = false;
        PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.status_bar_notification_settings);
        prefSet = getPreferenceScreen();

        mResolver = getActivity().getContentResolver();

        PreferenceCategory mBreathingNotifications = (PreferenceCategory) 
            findPreference(BREATHING_NOTIFICATIONS);

        mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
        mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);
        mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

        Context context = getActivity();
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {

            mMissedCallBreath.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
            mMissedCallBreath.setOnPreferenceChangeListener(this);

            mVoicemailBreath.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
            mVoicemailBreath.setOnPreferenceChangeListener(this);

            mSmsBreath.setChecked(Settings.Global.getInt(mResolver,
                    Settings.Global.KEY_SMS_BREATH, 0) == 1);
            mSmsBreath.setOnPreferenceChangeListener(this);
        } else {
            removePreference(BREATHING_NOTIFICATIONS);
        }

        mCheckPreferences = true;
        return prefSet;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mMissedCallBreath) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver, Settings.System.KEY_MISSED_CALL_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver, Settings.System.KEY_VOICEMAIL_BREATH, value ? 1 : 0);
            return true;
        } else if (preference == mSmsBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(mResolver, Settings.Global.KEY_SMS_BREATH, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
       return MetricsLogger.DONT_TRACK_ME_BRO;
    }
}
