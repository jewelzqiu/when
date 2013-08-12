package com.jewelzqiu.when;

import android.app.Fragment;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jewelzqiu on 8/6/13.
 */
public class EventsFragment extends Fragment {

    public static final String ARG_TRIGGER_NUMBER = "trigger_number";
    private EventsAdapter mAdapter;
    private Context mContext;

    private int mEventType;

    public EventsFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView rootView = (ListView) inflater.inflate(R.layout.fragment_events, container, false);
        mEventType = getArguments().getInt(ARG_TRIGGER_NUMBER);
        String trigger = getResources().getStringArray(R.array.triggers_entries)[mEventType];

        ArrayList<Event> list = new ArrayList<Event>();
        Event event1 = new Event(trigger, "action1");
        Event event2 = new Event(trigger, "action2");
        list.add(event1);
        list.add(event2);

        mAdapter = new EventsAdapter(mContext, list);
        rootView.setAdapter(mAdapter);
        rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        getActivity().setTitle(trigger);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                System.out.println("add");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class EventsAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<Event> eventList;

        public EventsAdapter(Context context, ArrayList<Event> list) {
            this.mContext = context;
            eventList = list;
        }

        @Override
        public int getCount() {
            if (eventList == null) {
                return 0;
            } else {
                return eventList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (eventList == null) {
                return null;
            } else {
                return eventList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final Event event = (Event) getItem(position);
            ViewHolder viewHolder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.events_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.mTextView = (TextView) view.findViewById(R.id.event_text);
                viewHolder.mSwitch = (Switch) view.findViewById(R.id.event_switch);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.mTextView.setText(event.getAction());
            viewHolder.mSwitch.setChecked(event.isEnabled());
            viewHolder.mSwitch.setId(position);

//            viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    System.out.println(((TextView) v).getText());
//                }
//            });
            viewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = buttonView.getId();
                    Event event = (Event) getItem(position);
                    event.setEnabled(isChecked);
                }
            });

            return view;
        }
    }

    public class ViewHolder {
        TextView mTextView;
        Switch mSwitch;
    }
}
