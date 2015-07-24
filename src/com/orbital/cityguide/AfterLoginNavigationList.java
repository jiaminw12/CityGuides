package com.orbital.cityguide;

import com.orbital.cityguide.adapter.NavDrawerListAdapter;
import com.orbital.cityguide.model.NavDrawerItem;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AfterLoginNavigationList extends Activity {

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

	String title, alertboxmsg;
	int success;
	String name_profile;
	Bundle bundle;

	ImageView mImage;

	private static final String GETUSR_URL = "http://192.168.1.9/City_Guide/getUser.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_USER = "userprofile";
	private static final String TAG_IMAGE = "image";

	// An array of all of our attractions
	private JSONArray mUsers = null;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navigation_list);

		Bundle extras = getIntent().getExtras();
		name_profile = extras.getString("profile_username");

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		LayoutInflater inflater = getLayoutInflater();

		View listHeaderView = inflater.inflate(R.layout.header_list, null,
				false);
		mImage = (ImageView) listHeaderView.findViewById(R.id.photo);
		updateImage();
		TextView mName = (TextView) listHeaderView.findViewById(R.id.name);
		mName.setText(name_profile);

		mDrawerList.addHeaderView(listHeaderView);

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
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons
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

	public void updateImage() {
		String username = name_profile;

		if (!(username.matches(""))) {
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));

				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);

				JSONObject json = jsonParser.makeHttpRequest(GETUSR_URL,
						"POST", params);
				if (json != null) {
					success = json.getInt(TAG_SUCCESS);
					if (success == 1) {

						mUsers = json.getJSONArray(TAG_USER);

						// looping through All Attractions
						for (int i = 0; i < mUsers.length(); i++) {
							JSONObject c = mUsers.getJSONObject(i);

							// Storing each json item in variable
							String image_pic = c.getString(TAG_IMAGE);

							if (!image_pic.equalsIgnoreCase("null")) {
								byte[] image = Base64.decode(image_pic,
										Base64.DEFAULT);
								Bitmap bitmap = BitmapFactory.decodeByteArray(
										image, 0, image.length);
								mImage.setImageBitmap(bitmap);
							} else {
								mImage.setVisibility(View.GONE);
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/* Slide menu item click listener */
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
		updateImage();
		return super.onPrepareOptionsMenu(menu);
	}

	/* Diplaying fragment view for selected nav drawer list item */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new ViewProfileFragment();
			bundle = new Bundle();
			bundle.putString("profile_username", name_profile);
			fragment.setArguments(bundle);
			break;
		case 1:
			fragment = new HomeFragment();
			bundle = new Bundle();
			bundle.putString("profile_username", name_profile);
			fragment.setArguments(bundle);
			break;
		case 2:
			fragment = new SearchFragment();
			bundle = new Bundle();
			bundle.putString("profile_username", name_profile);
			fragment.setArguments(bundle);
			break;
		case 3:
			fragment = new MapsFragment();
			bundle = new Bundle();
			bundle.putString("profile_username", name_profile);
			fragment.setArguments(bundle);
			break;
		case 4:
			fragment = new TripPlannerFragment();
			bundle = new Bundle();
			bundle.putString("profile_username", name_profile);
			fragment.setArguments(bundle);
			break;
		case 5:
			Intent logout = new Intent();
			logout.setClass(this, MainActivity.class);
			startActivity(logout);
			finish();
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
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

}
