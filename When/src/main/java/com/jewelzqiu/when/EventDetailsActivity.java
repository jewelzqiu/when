package com.jewelzqiu.when;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Formatter;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class EventDetailsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private ListPreference mTriggerPreference;
    private ListPreference mActionPreference;
    private Preference mTimePreference;
    private SwitchPreference mRepeatPreference;
    private CheckBoxPreference[] mDaysPreference = new CheckBoxPreference[7];

    private int mEventType;
    private int mEventID;
    private int mActionType;
    private boolean mIsAddingEvent;
    private int mHour = -1;
    private int mMinute = -1;
    private int mRepeat;

    public static final String ADD_NEW_EVENT = "add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.pref_event_details);
        mIsAddingEvent = getIntent().getBooleanExtra(ADD_NEW_EVENT, true);
        mEventType = getIntent().getIntExtra(EventsFragment.EVENT_TYPE, -1);
        mTriggerPreference = (ListPreference) findPreference(getString(R.string.key_trigger));
        mActionPreference = (ListPreference) findPreference(getString(R.string.key_action));
        mTimePreference = findPreference(getString(R.string.key_time));
        mRepeatPreference = (SwitchPreference) findPreference(getString(R.string.key_repeat));
        String[] keyDays = getResources().getStringArray(R.array.days);
        for (int i = 0; i < 7; i++) {
            mDaysPreference[i] = (CheckBoxPreference) findPreference(keyDays[i]);
        }

        if (mEventType == -1) {
            mEventType = TimeEventsFragment.EVENT_TYPE_TIME;
        }

        if (mEventType != TimeEventsFragment.EVENT_TYPE_TIME) {
            mTimePreference.setEnabled(false);
            mRepeatPreference.setChecked(false);
            mRepeatPreference.setEnabled(false);
            for (int i = 0; i < 7; i++) {
                mDaysPreference[i].setChecked(false);
                mDaysPreference[i].setEnabled(false);
            }
        }

        String[] triggers = getResources().getStringArray(R.array.triggers_entries);
        mTriggerPreference.setValueIndex(mEventType);
        mTriggerPreference.setSummary(triggers[mEventType]);
        if (mIsAddingEvent) {
            mActionPreference.setValue("-1");
            if (mEventType == TimeEventsFragment.EVENT_TYPE_TIME) {
                mRepeatPreference.setChecked(false);
                for (int i = 0; i < 7; i++) {
                    mDaysPreference[i].setChecked(false);
                }
            }
            mEventID = -1;
        } else {
            mEventID = getIntent().getIntExtra(EventsFragment.EVENT_ID, -1);
            if (mEventID > -1) {
                DataBaseHelper DBHelper = new DataBaseHelper(this, DataBaseHelper.DB_NAME, null, 1);
                Cursor cursor = DBHelper.queryByID(mEventType, mEventID);
                if (cursor.moveToFirst()) {
                    mActionType = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ACTION));
                    String[] actions = getResources().getStringArray(R.array.actions_entries);
                    mActionPreference.setValueIndex(mActionType);
                    mActionPreference.setSummary(actions[mActionType]);
                    if (mEventType == TimeEventsFragment.EVENT_TYPE_TIME) {
                        mHour = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_HOUR));
                        mMinute = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_MINUTE));
                        mRepeat = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_REPEAT));
                        String time = new Formatter().format("%02d:%02d", mHour, mMinute).toString();
                        mTimePreference.setSummary(time);
                        if (mRepeat == 0) {
                            mRepeatPreference.setChecked(false);
                        } else {
                            mRepeatPreference.setChecked(true);
                            for (int i = 0; i < 7; i++) {
                                if ((TimeEventsFragment.DAY_MASK[i] & mRepeat) != 0) {
                                    mDaysPreference[i].setChecked(true);
                                } else {
                                    mDaysPreference[i].setChecked(false);
                                }
                            }
                        }
                    }
                    return;
                }
            }

            // add event
            mIsAddingEvent = true;
            mActionPreference.setValue("-1");
            if (mEventType == TimeEventsFragment.EVENT_TYPE_TIME) {
                mRepeatPreference.setChecked(false);
                for (int i = 0; i < 7; i++) {
                    mDaysPreference[i].setChecked(false);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DataBaseHelper DBHelper;
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.cancel:
                finish();
                return true;

            case R.id.save:
                DBHelper = new DataBaseHelper(this, DataBaseHelper.DB_NAME, null, 1);
                if (mIsAddingEvent) {
                    if (mEventType == TimeEventsFragment.EVENT_TYPE_TIME) {
//                        int repeat_mask = 0;
//                        for (int i = 0; i < 7; i++) {
//                            if (mDaysPreference[i].isEnabled() && mDaysPreference[i].isChecked()) {
//                                repeat_mask |= TimeEventsFragment.DAY_MASK[i];
//                            }
//                        }
                        DBHelper.addTimeEvent(mHour, mMinute,
                                Integer.parseInt(mActionPreference.getValue()), mRepeat);
                    } else {
                        DBHelper.addEvent(mEventType, Integer.parseInt(mActionPreference.getValue()));
                    }
                } else {
                    if (mEventType == TimeEventsFragment.EVENT_TYPE_TIME) {
                        DBHelper.updateTimeEvent(mEventType, mEventID, mActionType, mHour, mMinute, mRepeat);
                    } else {
                        DBHelper.updateEvent(mEventType, mEventID, mActionType);
                    }
                }
                finish();
                return true;

            case R.id.delete:
                if (mEventID == -1) {
                    finish();
                } else {
                    DBHelper = new DataBaseHelper(this, DataBaseHelper.DB_NAME, null, 1);
                    DBHelper.deleteEvent(mEventType, mEventID);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTimePreference) {
            if (mHour == -1 || mMinute == -1) {
                Calendar calendar = Calendar.getInstance();
                mHour = calendar.get(Calendar.HOUR_OF_DAY);
                mMinute = calendar.get(Calendar.MINUTE);
            }
            ContentResolver cv = getContentResolver();
            String timeFormat = Settings.System.getString(cv, Settings.System.TIME_12_24);
            boolean is24HourFormat;
            if (timeFormat != null && timeFormat.equals("24")) {
                is24HourFormat = true;
            } else {
                is24HourFormat = false;
            }
            new TimePickerDialog(this, new OnEventTimeSetListener(), mHour, mMinute, is24HourFormat).show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    class OnEventTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            String time = new Formatter().format("%02d:%02d", mHour, mMinute).toString();
            mTimePreference.setSummary(time);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String[] triggers = getResources().getStringArray(R.array.triggers_entries);
        String[] actions = getResources().getStringArray(R.array.actions_entries);
        int index = new Integer(mTriggerPreference.getValue());
        mEventType = index;
        mTriggerPreference.setSummary(triggers[mEventType]);
        String action = mActionPreference.getValue();
        if (action != null) {
            index = new Integer(mActionPreference.getValue());
            if (index > -1) {
                mActionPreference.setSummary(actions[index]);
            }
        }

        if (mEventType == TimeEventsFragment.EVENT_TYPE_TIME) {
            if (mMinute != -1 && mHour != -1) {
                String time = new Formatter().format("%02d:%02d", mHour, mMinute).toString();
                mTimePreference.setSummary(time);
            }
            mRepeat = 0;
            for (int i = 0; i < 7; i++) {
                if (mDaysPreference[i].isEnabled() && mDaysPreference[i].isChecked()) {
                    mRepeat |= TimeEventsFragment.DAY_MASK[i];
                }
            }
        }

        if (key.equals(getString(R.string.key_trigger))) {
            int type = new Integer(mTriggerPreference.getValue());
            if (type == TimeEventsFragment.EVENT_TYPE_TIME) {
                mTimePreference.setEnabled(true);
                mRepeatPreference.setEnabled(true);
                for (int i = 0; i < 7; i++) {
                    mDaysPreference[i].setEnabled(true);
                }
            } else {
                mTimePreference.setEnabled(false);
                mRepeatPreference.setEnabled(false);
                for (int i = 0; i < 7; i++) {
                    mDaysPreference[i].setEnabled(false);
                }
            }
        }
    }
}
