package com.jewelzqiu.when;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    private static final int[] DAY_MASK = {
            0x00000001, // SUNDAY
            0x00000002, // MONDAY
            0x00000004, // TUESDAY
            0x00000008, // WEDNESDAY
            0x00000010, // THURSDAY
            0x00000020, // FRIDAY
            0x00000040, // SATURDAY
    };

    public TimeEventsFragment() {

    }

    public TimeEventsFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView rootView =
                (ListView) inflater.inflate(R.layout.fragment_events, container, false);

        mEventType = getArguments().getInt(EventsFragment.ARG_TRIGGER_NUMBER);
        String trigger = getResources().getStringArray(R.array.triggers_entries)[mEventType];
        getActivity().setTitle(trigger);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(mContext, EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.ADD_NEW_EVENT, true);
                intent.putExtra(EventsFragment.ARG_TRIGGER_NUMBER, EVENT_TYPE_TIME);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int action_no = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ACTION));
            String action = context.getResources().getStringArray(R.array.actions_entries)[action_no];
            int minute = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_MINUTE));
            int second = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_SECOND));
            String time = new Formatter().format("%02d:%02d", minute, second).toString();
            int repeat = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_REPEAT));
            String repeat_day = "";
            int count = 0;
            for (int i = 0; i < 7; i++) {
                if ((DAY_MASK[i] & repeat) != 0) {
                    repeat_day += context.getResources().getStringArray(R.array.days_of_week)[i] + " ";
                    count++;
                }
            }
            if (count == 7) {
                repeat_day = context.getString(R.string.every_day) + " ";
            }
            time = repeat_day + time;
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
