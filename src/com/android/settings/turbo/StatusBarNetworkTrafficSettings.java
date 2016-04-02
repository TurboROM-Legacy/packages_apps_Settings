/*
 * Copyright (C) 2015 DarkKat
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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNetworkTrafficSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_CAT_OPTIONS =
            "network_traffic_cat_options";
    private static final String PREF_CAT_COLORS =
            "network_traffic_cat_colors";
    private static final String PREF_ACTIVITY =
            "network_traffic_activity";
    private static final String PREF_TYPE =
            "network_traffic_type";
    private static final String PREF_BIT_BYTE =
            "network_traffic_bit_byte";
    private static final String PREF_HIDE =
            "network_traffic_hide_traffic";
    private static final String PREF_TEXT_COLOR =
            "network_traffic_text_color";
    private static final String PREF_ICON_COLOR =
            "network_traffic_icon_color";

    private static final int UP_DOWN        = 2;
    private static final int DISABLED       = 3;
    private static final int TYPE_TEXT      = 0;
    private static final int TYPE_ICON      = 1;
    private static final int TYPE_TEXT_ICON = 2;

    private static final int WHITE = 0xffffffff;
    private static final int HOLO_BLUE_LIGHT = 0xff33b5e5;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ListPreference mTrafficActivity;
    private ListPreference mType;
    private SwitchPreference mBitByte;
    private SwitchPreference mHide;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.statusbar_network_traffic_settings);
        mResolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        final int trafficActivity = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ACTIVITY, DISABLED);
        final boolean isTrafficEnabled = trafficActivity != DISABLED;

        mTrafficActivity =
                (ListPreference) findPreference(PREF_ACTIVITY);
        mTrafficActivity.setValue(String.valueOf(trafficActivity));
        mTrafficActivity.setSummary(mTrafficActivity.getEntry());
        mTrafficActivity.setOnPreferenceChangeListener(this);

        PreferenceCategory catOptions =
                (PreferenceCategory) findPreference(PREF_CAT_OPTIONS);
        PreferenceCategory catColors =
                (PreferenceCategory) findPreference(PREF_CAT_COLORS);

        if (isTrafficEnabled) {
            final int type = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TYPE, TYPE_TEXT_ICON);
            final boolean showText  = type == TYPE_TEXT || type == TYPE_TEXT_ICON;
            final boolean showIcon = type == TYPE_ICON || type == TYPE_TEXT_ICON;

            mType = (ListPreference) findPreference(PREF_TYPE);
            mType.setValue(String.valueOf(type));
            mType.setSummary(mType.getEntry());
            mType.setOnPreferenceChangeListener(this);

            if (showText) {
                mBitByte =
                        (SwitchPreference) findPreference(PREF_BIT_BYTE);
                mBitByte.setChecked((Settings.System.getInt(mResolver,
                        Settings.System.STATUS_BAR_NETWORK_TRAFFIC_BIT_BYTE, 0) == 1));
                mBitByte.setOnPreferenceChangeListener(this);

                mHide =
                        (SwitchPreference) findPreference(PREF_HIDE);
                mHide.setChecked((Settings.System.getInt(mResolver,
                        Settings.System.STATUS_BAR_NETWORK_TRAFFIC_HIDE_TRAFFIC, 1) == 1));
                mHide.setOnPreferenceChangeListener(this);

                mTextColor =
                        (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
                intColor = Settings.System.getInt(mResolver,
                        Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TEXT_COLOR, WHITE); 
                mTextColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mTextColor.setSummary(hexColor);
                mTextColor.setOnPreferenceChangeListener(this);
            } else {
                catOptions.removePreference(findPreference(PREF_BIT_BYTE));
                catOptions.removePreference(findPreference(PREF_HIDE));
                catColors.removePreference(findPreference(PREF_TEXT_COLOR));
            }

            if (showIcon) {
                mIconColor =
                        (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
                intColor = Settings.System.getInt(mResolver,
                        Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ICON_COLOR, WHITE); 
                mIconColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mIconColor.setSummary(hexColor);
                mIconColor.setOnPreferenceChangeListener(this);
            } else {
                catColors.removePreference(findPreference(PREF_ICON_COLOR));
            }
        } else {
            catOptions.removePreference(findPreference(PREF_TYPE));
            catOptions.removePreference(findPreference(PREF_BIT_BYTE));
            catOptions.removePreference(findPreference(PREF_HIDE));
            catColors.removePreference(findPreference(PREF_TEXT_COLOR));
            catColors.removePreference(findPreference(PREF_ICON_COLOR));
            removePreference(PREF_CAT_OPTIONS);
            removePreference(PREF_CAT_COLORS);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value;
        int intValue;
        int index;
        int intHex;
        String hex;

        if (preference == mTrafficActivity) {
            intValue = Integer.valueOf((String) newValue);
            index = mTrafficActivity.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ACTIVITY, intValue);
            mTrafficActivity.setSummary(mTrafficActivity.getEntries()[index]);
            refreshSettings();
            return true;
        } else if (preference == mType) {
            intValue = Integer.valueOf((String) newValue);
            index = mType.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TYPE, intValue);
            mType.setSummary(mType.getEntries()[index]);
            refreshSettings();
            return true;
        } else if (preference == mBitByte) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_BIT_BYTE,
                value ? 1 : 0);
            return true;
        } else if (preference == mHide) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_HIDE_TRAFFIC,
                value ? 1 : 0);
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(Integer.valueOf(
                    String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference ==  mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        StatusBarNetworkTrafficSettings getOwner() {
            return (StatusBarNetworkTrafficSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.dlg_reset_values_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.dlg_reset_android,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ACTIVITY, DISABLED);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TYPE, TYPE_TEXT_ICON);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_BIT_BYTE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_HIDE_TRAFFIC, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TEXT_COLOR,
                                WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ICON_COLOR,
                                WHITE);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.dlg_reset_turbo,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ACTIVITY, UP_DOWN);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TYPE, TYPE_TEXT_ICON);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_BIT_BYTE, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_HIDE_TRAFFIC, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_TEXT_COLOR,
                                HOLO_BLUE_LIGHT);
                            Settings.System.putInt(getOwner().mResolver,
                                Settings.System.STATUS_BAR_NETWORK_TRAFFIC_ICON_COLOR,
                                HOLO_BLUE_LIGHT);
                            getOwner().refreshSettings();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    @Override
    protected int getMetricsCategory() {
        return InstrumentedFragment.STATUSBAR;
    }
}
