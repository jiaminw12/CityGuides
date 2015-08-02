package com.orbital.cityguide;

import com.orbital.cityguide.adapter.DBAdapter;
import com.orbital.cityguide.adapter.NavDrawerListAdapter;
import com.orbital.cityguide.model.NavDrawerItem;

import java.io.File;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;
	// used to store app title
	private CharSequence mTitle;
	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	String name_profile;
	Bundle bundle;

	DBAdapter dbAdaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigation_list);

		if (!doesDatabaseExist(this, "cityGuideSingapore")) {
			SharedPreferences prefs = getSharedPreferences("PREFERENCE",
					MODE_PRIVATE);
			boolean firstRun = prefs.getBoolean("firstRun", true);
			if (firstRun) {
				// set your flag to false
				prefs.edit().putBoolean("firstRun", false).commit();
				prefs.edit().putString("currentLocation", "Singapore").commit();
				prefs.edit().putFloat("latitude", (float) 1.29324).commit();
				prefs.edit().putFloat("longtitude", (float) 103.852219).commit();
				prefs.edit().putInt("NumOfDays", 2).commit();
				prefs.edit().putInt("NumOfAdult", 2).commit();
				prefs.edit().putInt("NumOfChild", 0).commit();
				dbAdaptor = new DBAdapter(this);
				dbAdaptor.insertTag();
			}
		}

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Search
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// Maps
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));
		// Planer
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons
				.getResourceId(3, -1)));
		// Logout
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons
				.getResourceId(4, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Called when invalidateOptionsMenu() is triggered */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.frame_container, HomeFragment.newInstance(null),
							HomeFragment.TAG).commit();
			break;
		case 1:
			getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_container, SearchFragment.newInstance(null),
					SearchFragment.TAG).commit();
			break;
		case 2:
			getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_container, MapsFragment.newInstance(null),
					MapsFragment.TAG).commit();
			break;
		case 3:
			getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_container, TripPlannerFragment.newInstance(null),
					TripPlannerFragment.TAG).commit();
			break;
		case 4:
			getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_container, LoginFragment.newInstance(),
					LoginFragment.TAG).commit();
			break;
		default:
			break;
		}
		
		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);
		setTitle(navMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
		
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	private static boolean doesDatabaseExist(ContextWrapper context,
			String dbName) {
		File dbFile = context.getDatabasePath(dbName);
		return dbFile.exists();
	}

}
