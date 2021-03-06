package com.jewelzqiu.when;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.IOException;

public class MainActivity extends Activity {

    private String[] mTriggerTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private final String DEFAULT_SHARED_PREFERENCE = "default";
    private final String IS_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTriggerTitles = getResources().getStringArray(R.array.triggers_entries);
        mDrawerTitle = getResources().getString(R.string.select_trigger);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.select_trigger, R.string.select_trigger) {

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
//                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                getActionBar().setTitle(mTitle);
//                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mTriggerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (savedInstanceState == null) {
            selectItem(0);
            SharedPreferences sharedPreferences =
                    getSharedPreferences(DEFAULT_SHARED_PREFERENCE, MODE_PRIVATE);
            boolean isInit = sharedPreferences.getBoolean(IS_FIRST_RUN, true);
            if (isInit) {
                // open drawer for the first time
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.openDrawer(mDrawerList);
                    }
                }, 500);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(IS_FIRST_RUN, false);
                editor.commit();
            }
        }

        checkRoot();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

//        switch (item.getItemId()) {
//            case R.id.add:
//                System.out.println("add");
//                return true;
//            case R.id.action_settings:
//
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new TimeEventsFragment(this);
        } else {
            fragment = new EventsFragment(this);
        }
//        fragment = new EventsFragment(this);
        Bundle args = new Bundle();
        args.putInt(EventsFragment.EVENT_TYPE, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerList.setItemChecked(position, true);
        setTitle(mTriggerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public void checkRoot() {
        final Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("su");
            if (process.getOutputStream() == null) {
                showErrorDialog();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog();
        }
    }

    public void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setMessage("It seems that your device doesn't have root permission. " +
                        "Some operations may not work.")
                .setTitle("Sorry")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Intent intent = new Intent(Intent.ACTION_MAIN);
//                        intent.addCategory(Intent.CATEGORY_HOME);
//                        startActivity(intent);
//                        finish();
                    }
                })
                .create().show();
    }
}
