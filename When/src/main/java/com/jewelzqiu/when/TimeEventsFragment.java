package com.jewelzqiu.when;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.drm.DrmStore;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Formatter;

/**
 * Created by jewelzqiu on 8/6/13.
 */
public class TimeEventsFragment extends Fragment {

    private Context mContext;
    private TimeEventsAdapter mAdapter;

    private int mEventType;
    private static final String TABLE_NAME = DataBaseHelper.TABLE_NAME_PREFIX + 0;

    public static final int EVENT_TYPE_TIME = 0;
    public static final String EVENT_TIME = "event_time";

    public static final int[] DAY_MASK = {
            0x00000001, // SUNDAY
            0x00000002, // MONDAY
            0x00000004, // TUESDAY
            0x00000008, // WEDNESDAY
            0x00000010, // THURSDAY
            0x00000020, // FRIDAY
            0x00000040, // SATURDAY
    };

    public TimeEventsFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView rootView =
                (ListView) inflater.inflate(R.layout.fragment_events, container, false);

        mEventType = getArguments().getInt(EventsFragment.EVENT_TYPE);
        String trigger = getResources().getStringArray(R.array.triggers_entries)[mEventType];
        getActivity().setTitle(trigger);
        setHasOptionsMenu(true);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext, DataBaseHelper.DB_NAME, null, 1);
        mAdapter = new TimeEventsAdapter(mContext, dataBaseHelper.query(mEventType), false);
        rootView.setAdapter(mAdapter);
        rootView.setOnItemClickListener(new OnEventClickListener());
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext, DataBaseHelper.DB_NAME, null, 1);
        mAdapter.changeCursor(dataBaseHelper.query(mEventType));
        mAdapter.notifyDataSetChanged();
        calculateNextEvent(mContext);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(mContext, EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.ADD_NEW_EVENT, true);
                intent.putExtra(EventsFragment.EVENT_TYPE, EVENT_TYPE_TIME);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class OnEventClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent  = new Intent(mContext, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.ADD_NEW_EVENT, false);
            intent.putExtra(EventsFragment.EVENT_TYPE, EVENT_TYPE_TIME);
            intent.putExtra(EventsFragment.EVENT_ID, view.getId());
            startActivity(intent);
        }
    }

    public static void calculateNextEvent(Context context) {
        DataBaseHelper DBHelper = new DataBaseHelper(context, DataBaseHelper.DB_NAME, null, 1);
        Cursor cursor = DBHelper.query(EVENT_TYPE_TIME);
        if (!cursor.moveToFirst()) {
            return;
        }

        long time = Long.MAX_VALUE;
        int action = -1;
        boolean eventPending = false;

        do {
            int enabled = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ENABLED));
            if (enabled == 0) {
                continue;
            }

            int hour = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_MINUTE));
            int actionNo = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ACTION));
            int repeat = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_REPEAT));

            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            if (repeat != 0) {
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                // TODO
            } else {
                if (hour < currentHour || (hour == currentHour && minute <= currentMinute)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                long eventTime = calendar.getTimeInMillis();
                if (eventTime < time) {
                    time = eventTime;
                    action = actionNo;
                    eventPending = true;
                }
            }
        } while (cursor.moveToNext());

        Intent intent = new Intent(context, ActionService.class);
        intent.putExtra(EventsFragment.EVENT_TYPE, EVENT_TYPE_TIME);
        intent.putExtra(EventsFragment.ACTION_TYPE, action);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                eventPending ? time : System.currentTimeMillis(), pendingIntent);
        System.out.println((time - System.currentTimeMillis()) / 1000);
    }

    private class TimeEventsAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public TimeEventsAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.time_events_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.actionView = (TextView) view.findViewById(R.id.event_text);
            viewHolder.timeView = (TextView) view.findViewById(R.id.event_time);
            viewHolder.switchView = (Switch) view.findViewById(R.id.event_switch);
            view.setTag(viewHolder);

            int id = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ID));
            view.setId(id);
            viewHolder.switchView.setId(id);
            viewHolder.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext, DataBaseHelper.DB_NAME, null, 1);
                    dataBaseHelper.setEnabled(mEventType, buttonView.getId(), isChecked);
                    calculateNextEvent(mContext);
                }
            });
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int action_no = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ACTION));
            String action = context.getResources().getStringArray(R.array.actions_entries)[action_no];
            int minute = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_MINUTE));
            int hour = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_HOUR));
            String time = new Formatter().format("%02d:%02d", hour, minute).toString();
            int repeat = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_REPEAT));
            if (repeat != 0) {
                String repeat_day = "";
                int count = 0;
                for (int i = 0; i < 7; i++) {
                    if ((DAY_MASK[i] & repeat) != 0) {
                        if (count > 0) {
                            repeat_day += ", ";
                        }
                        repeat_day += context.getResources().getStringArray(R.array.days_of_week)[i];
                        count++;
                    }
                }
                if (count == 7) {
                    repeat_day = context.getString(R.string.every_day);
                }
                time = time + " on " + repeat_day;
            }
            boolean enabled = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ENABLED)) == 1;

            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.actionView.setText(action);
            viewHolder.timeView.setText(time);
            viewHolder.switchView.setChecked(enabled);
        }
    }

    private class ViewHolder {
        TextView actionView;
        TextView timeView;
        Switch switchView;
    }
}
