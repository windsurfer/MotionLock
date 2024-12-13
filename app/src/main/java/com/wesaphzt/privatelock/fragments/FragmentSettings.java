package com.wesaphzt.privatelock.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.wesaphzt.privatelock.R;

public class FragmentSettings extends PreferenceFragmentCompat {

    private Context context;

    private SwitchPreference runConstant;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        setHasOptionsMenu(true);
        context = getContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //this static call will reset default values only on the first read
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        runConstant = (SwitchPreference) findPreference("RUN_CONSTANT");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set title
        getActivity().setTitle("Settings");

        TypedArray array = context.getTheme().obtainStyledAttributes(new int[] {
                android.R.attr.colorBackground,
        });
        int backgroundColor = array.getColor(0, 0xFF00FF);
        array.recycle();

        view.setBackgroundColor(backgroundColor);

        sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals("RUN_CONSTANT") && runConstant.isChecked()) {
                    Toast.makeText(context, getString(R.string.settings_restart_service_toast), Toast.LENGTH_LONG).show();
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //hide action bar menu
        menu.setGroupVisible(R.id.menu_main, false);

        super.onPrepareOptionsMenu(menu);
    }
}