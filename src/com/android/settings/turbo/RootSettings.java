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

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.AppOpsManager.PackageOps;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.HardwareRenderer;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class RootSettings extends SettingsPreferenceFragment implements 
	DialogInterface.OnClickListener, DialogInterface.OnDismissListener, 
	OnPreferenceChangeListener {

    private static final String TAG = "RootSettings";

    private static final String ROOT_ACCESS_KEY = "root_access";
    private static final String ROOT_ACCESS_PROPERTY = "persist.sys.root_access";

    private boolean mDialogClicked;
    private boolean mLastEnabledState;
    private boolean mUnavailable;

    private ListPreference mRootAccess;
    private Object mSelectedRootValue;

    private Dialog mRootDialog;

    private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();

    private final ArrayList<SwitchPreference> mResetSwitchPrefs
            = new ArrayList<SwitchPreference>();

    private final HashSet<Preference> mDisabledPrefs = new HashSet<Preference>();

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DONT_TRACK_ME_BRO;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

	addPreferencesFromResource(R.xml.root_settings);

        mRootAccess = (ListPreference) findPreference(ROOT_ACCESS_KEY);
        mRootAccess.setOnPreferenceChangeListener(this);
    }

    private ListPreference addListPreference(String prefKey) {
        ListPreference pref = (ListPreference) findPreference(prefKey);
        mAllPrefs.add(pref);
        pref.setOnPreferenceChangeListener(this);
        return pref;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void removePreference(Preference preference) {
        getPreferenceScreen().removePreference(preference);
        mAllPrefs.remove(preference);
        mResetSwitchPrefs.remove(preference);
    }

    private void setPrefsEnabledState(boolean enabled) {
        for (int i = 0; i < mAllPrefs.size(); i++) {
            Preference pref = mAllPrefs.get(i);
            pref.setEnabled(enabled && !mDisabledPrefs.contains(pref));
        }
        updateAllOptions();
    }

    @Override
    public void onResume() {
        super.onResume();

        final ContentResolver cr = getActivity().getContentResolver();
        setPrefsEnabledState(mLastEnabledState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void updateSwitchPreference(SwitchPreference switchPreference, boolean value) {
        switchPreference.setChecked(value);
    }

    private void updateAllOptions() {
        final Context context = getActivity();
        final ContentResolver cr = context.getContentResolver();
        updateRootAccessOptions();
    }

    private void updateRootAccessOptions() {
        String value = SystemProperties.get(ROOT_ACCESS_PROPERTY, "3");
        mRootAccess.setValue(value);
        mRootAccess.setSummary(getResources()
                .getStringArray(R.array.root_access_entries)[Integer.valueOf(value)]);
    }

    public static boolean isRootForAppsEnabled() {
        int value = SystemProperties.getInt(ROOT_ACCESS_PROPERTY, 3);
        boolean daemonState =
                SystemProperties.get("init.svc.su_daemon", "absent").equals("running");
        return daemonState && (value == 1 || value == 3);
    }

    private void writeRootAccessOptions(Object newValue) {
        String oldValue = SystemProperties.get(ROOT_ACCESS_PROPERTY, "3");
        SystemProperties.set(ROOT_ACCESS_PROPERTY, newValue.toString());
        if (Integer.valueOf(newValue.toString()) < 2 && !oldValue.equals(newValue)
                && "1".equals(SystemProperties.get("service.adb.root", "0"))) {
            SystemProperties.set("service.adb.root", "0");
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 0);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 1);
        }
        updateRootAccessOptions();
    }


    private void resetRootAccessOptions() {
        String oldValue = SystemProperties.get(ROOT_ACCESS_PROPERTY, "0");
        SystemProperties.set(ROOT_ACCESS_PROPERTY, "0");
        if (!oldValue.equals("0") && "1".equals(SystemProperties.get("service.adb.root", "0"))) {
            SystemProperties.set("service.adb.root", "0");
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 0);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 1);
        }
        updateRootAccessOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRootAccess) {
            if ("0".equals(SystemProperties.get(ROOT_ACCESS_PROPERTY, "0"))
                    && !"0".equals(newValue)) {
                mSelectedRootValue = newValue;
                mDialogClicked = false;
                if (mRootDialog != null) {
                    dismissDialogs();
                }
                mRootDialog = new AlertDialog.Builder(getActivity())
                        .setMessage(getResources().getString(R.string.root_access_warning_message))
                        .setTitle(R.string.root_access_warning_title)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this).show();
                mRootDialog.setOnDismissListener(this);
            } else {
                writeRootAccessOptions(newValue);
            }
            return true;
        }
        return false;
    }

    private void dismissDialogs() {
	if (mRootDialog != null) {
	    mRootDialog.dismiss();
	    mRootDialog = null;
	}
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mRootDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                writeRootAccessOptions(mSelectedRootValue);
            } else {
                // Reset the option
                writeRootAccessOptions("0");
            }
        }
    }
	    
    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
	if (dialog == mRootDialog) {
            updateRootAccessOptions();
            mRootDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissDialogs();
        super.onDestroy();
    }

}
