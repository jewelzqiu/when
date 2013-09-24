package com.jewelzqiu.when;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Formatter;

/**
 * Created by jewelzqiu on 8/6/13.
 */
public class EventsFragment extends Fragment {

    public static final String EVENT_TYPE = "event_type";
    public static final String ACTION_TYPE = "action_type";
    public static final String EVENT_ID = "event_id";
    private EventsAdapter mAdapter;
    private Context mContext;

    private int mEventType;

    public EventsFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView rootView = (ListView) inflater.inflate(R.layout.fragment_events, container, false);
        mEventType = getArguments().getInt(EVENT_TYPE);
        String trigger = getResources().getStringArray(R.array.triggers_entries)[mEventType];

        DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext, DataBaseHelper.DB_NAME, null, 1);
        mAdapter = new EventsAdapter(mContext, dataBaseHelper.query(mEventType), false);
        rootView.setAdapter(mAdapter);
        rootView.setOnItemClickListener(new OnEventClickListener());

        getActivity().setTitle(trigger);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(mContext, DataBaseHelper.DB_NAME, null, 1);
        mAdapter.changeCursor(dataBaseHelper.query(mEventType));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(mContext, EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.ADD_NEW_EVENT, true);
                intent.putExtra(EventsFragment.EVENT_TYPE, mEventType);
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
            intent.putExtra(EventsFragment.EVENT_TYPE, mEventType);
            intent.putExtra(EventsFragment.EVENT_ID, view.getId());
            startActivity(intent);
        }
    }

    private class EventsAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public EventsAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.time_events_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.actionView = (TextView) view.findViewById(R.id.event_text);
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
                }
            });
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int action_no = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ACTION));
            String action = context.getResources().getStringArray(R.array.actions_entries)[action_no];
            boolean enabled = cursor.getInt(cursor.getColumnIndex(DataBaseHelper.COLUMN_ENABLED)) == 1;

            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.actionView.setText(action);
            viewHolder.switchView.setChecked(enabled);
        }
    }

    private class ViewHolder {
        TextView actionView;
        Switch switchView;
    }
}
