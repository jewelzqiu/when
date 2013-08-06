package com.jewelzqiu.when;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jewelzqiu on 8/6/13.
 */
public class EventsFragment extends Fragment {

    public static final String ARG_TRIGGER_NUMBER = "trigger_number";

    public EventsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);
        int i = getArguments().getInt(ARG_TRIGGER_NUMBER);
        String trigger = getResources().getStringArray(R.array.triggers_array)[i];
        TextView textView = (TextView) rootView.findViewById(R.id.fragment_text);
        textView.setText(trigger);
        getActivity().setTitle(trigger);
        return rootView;
    }
}
