package com.cjdell.podclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends FragmentActivity implements
    FeedListFragment.OnFragmentInteractionListener,
    PlaylistListFragment.OnFragmentInteractionListener,
    PlaylistFragment.OnFragmentInteractionListener,
    ViewPager.OnPageChangeListener {

    private final static String TAG = "MainActivity";
    private final static String ACTION_BAR_IS_SHOWING = "ActionBarIsShowing";

    private final Handler handler = new Handler();

    private RelativeLayout          root;
    private PagerSlidingTabStrip    tabs;
    private ViewPager               pager;
    private SlidingUpPanelLayout    sliding_layout;
    private View                    player;

    private MyPagerAdapter          adapter;

    private int                     currentColor = 0xFF3F9FE0;

    private PlaylistFragment        mPlaylistFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        root            = (RelativeLayout)          findViewById(R.id.activity_main_layout);
        tabs            = (PagerSlidingTabStrip)    findViewById(R.id.tabs);
        pager           = (ViewPager)               findViewById(R.id.pager);
        sliding_layout  = (SlidingUpPanelLayout)    findViewById(R.id.sliding_layout);
        player          = (View)                    findViewById(R.id.player);

        adapter = new MyPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
        tabs.setOnPageChangeListener(this);

        changeColor(currentColor);

        sliding_layout.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));

        sliding_layout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset < 0.2) {
                    if (getActionBar().isShowing()) {
                        getActionBar().hide();
                        mPlaylistFragment.shown();
                    }
                }
                else {
                    if (!getActionBar().isShowing()) {
                        getActionBar().show();
                    }
                }
            }

            @Override
            public void onPanelCollapsed(View panel) {

            }

            @Override
            public void onPanelExpanded(View panel) {

            }

            @Override
            public void onPanelAnchored(View panel) {

            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(ACTION_BAR_IS_SHOWING, false)) {
                getActionBar().show();
            }
            else {
                getActionBar().hide();
            }
        }

        if (savedInstanceState == null) {
            // Create the playlist fragment - but only when the activity is FIRST CREATED
            mPlaylistFragment = PlaylistFragment.newInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.playlist_container, mPlaylistFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        else {
            // Recall whether or not the action bar was showing (likely an orientation change)
            if (savedInstanceState.getBoolean(ACTION_BAR_IS_SHOWING, false)) {
                getActionBar().show();
            }
            else {
                getActionBar().hide();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Player can change in height, we need to update the sliding layout so that the correct amount remains on show
        if (hasFocus) sliding_layout.setPanelHeight(player.getMeasuredHeight());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                openAboutFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openAboutFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.show(ft, "about_dialog");
    }

    private void changeColor(int newColor) {
        tabs.setIndicatorColor(newColor);

        currentColor = newColor;
    }

    public void onColorClicked(View v) {
        int color = Color.parseColor(v.getTag().toString());
        changeColor(color);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentColor", currentColor);
        outState.putBoolean(ACTION_BAR_IS_SHOWING, getActionBar().isShowing());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentColor = savedInstanceState.getInt("currentColor");
        changeColor(currentColor);
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    @Override
    public void onFragmentInteraction(Fragment fragment, Object args) {
        if (fragment instanceof FeedListFragment) {
            openPlaylist();
        }
        else if (fragment instanceof PlaylistListFragment) {
            openPlaylist();
        }
        else if (fragment instanceof PlaylistFragment) {
            if (args.equals("Attached")) {
                mPlaylistFragment = (PlaylistFragment) fragment;
            }
        }
    }

    private void openPlaylist() {
        sliding_layout.expandPane();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if (sliding_layout.isExpanded()) {
                sliding_layout.collapsePane();
                return true;
            }
            else {
                //Ask the user if they want to quit
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Quit?")
                        .setMessage("Do you wish to exit the program?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Stop the activity
                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
        //Toast.makeText(getApplicationContext(), "onPageScrolled " + i + " " + v + " " + i2, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPageSelected(int i) {
        PageFragmentInterface fragment = (PageFragmentInterface) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + i);
        fragment.shown();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        //Toast.makeText(getApplicationContext(), "onPageScrollStateChanged " + i, Toast.LENGTH_LONG).show();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public final String[] TITLES = { "Subscriptions", "Playlists" };

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (TITLES[position].equals("Subscriptions")) {
                return FeedListFragment.newInstance();
            }
            else if (TITLES[position].equals("Playlists")) {
                return PlaylistListFragment.newInstance();
            }
            else {
                return null;
            }
        }

    }

}