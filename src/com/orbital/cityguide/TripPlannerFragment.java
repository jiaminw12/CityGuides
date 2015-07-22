package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Row;
import com.orbital.cityguide.adapter.DBAdapter;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Item;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Section;

import android.app.Fragment;
import android.app.ListFragment;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class TripPlannerFragment extends ListFragment {

	protected ArrayAdapter<CharSequence> dayAdapter;

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mPlannerList = new ArrayList<HashMap<String, String>>();
	final PlannerDragNDropListAdapter adapter = new PlannerDragNDropListAdapter();

	private HashMap<String, Integer> sections = new HashMap<String, Integer>();
	List<Row> rows = new ArrayList<Row>();

	private static final String GET_ATRR_TITLE_URL = "http://192.168.1.5/City_Guide/getAttractionByID.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_TITLE = "attr_title";
	private static final String TAG_ATTRACTION = "attractions";

	// An array of all of our attractions
	private JSONArray mAttrID = null;

	JSONParser jParser = new JSONParser();

	DBAdapter dbAdaptor;
	Cursor cursor = null;

	String name_profile;
	int success;
	int start = 0;
	String previousLetter = null;

	Spinner daySpinner = null;

	public TripPlannerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_planner, container,
				false);

		dbAdaptor = new DBAdapter(getActivity());
		Bundle bundle = this.getArguments();
		name_profile = bundle.getString("profile_username", name_profile);

		daySpinner = (Spinner) rootView.findViewById(R.id.day_spinner);
		this.dayAdapter = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.arrayDay, android.R.layout.simple_spinner_item);
		daySpinner.setAdapter(this.dayAdapter);
		daySpinner.setSelection(2);
		daySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String value = daySpinner.getSelectedItem().toString();

				if (value.equalsIgnoreCase("1")) {
					addSectionHeader(1);
				} else if (value.equalsIgnoreCase("2")) {
					addSectionHeader(2);
				} else if (value.equalsIgnoreCase("3")) {
					addSectionHeader(3);
				} else if (value.equalsIgnoreCase("4")) {
					addSectionHeader(4);
				} else if (value.equalsIgnoreCase("5")) {
					addSectionHeader(5);
				} else if (value.equalsIgnoreCase("6")) {
					addSectionHeader(6);
				} else if (value.equalsIgnoreCase("7")) {
					addSectionHeader(7);
				} else if (value.equalsIgnoreCase("8")) {
					addSectionHeader(8);
				} else if (value.equalsIgnoreCase("9")) {
					addSectionHeader(9);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		for (int i = 1; i < 3 + 1; i++) {
			String title = "Day " + i;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(String.valueOf(i), title);
			// adding HashList to ArrayList
			mPlannerList.add(map);
		}
		System.out.println("Testing : " + mPlannerList);

		try {
			dbAdaptor.open();
			cursor = dbAdaptor.getAllPlanner();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String attr_title = retrieveTitleByID(cursor.getString(0));
					String tag_title = cursor.getString(1);

					HashMap<String, String> map = new HashMap<String, String>();
					map.put(tag_title, attr_title);
					// adding HashList to ArrayList
					mPlannerList.add(map);
				} while (cursor.moveToNext());
			} else {
			}
		} catch (Exception e) {
			Log.e("City Guide", e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();

			if (dbAdaptor != null)
				dbAdaptor.close();
		}

		int start = 0;
		String previousLetter = null;

		System.out.println(mPlannerList);

		for (HashMap<String, String> map : mPlannerList) {
			for (String str : map.keySet()) {
				String key = str;
				String value = map.get(key);
				String firstTitle = key;

				// Check if we need to add a header row
				if (!firstTitle.equals(previousLetter)) {
					rows.add(new Section(firstTitle));
					sections.put(firstTitle, start);
				}

				// Add the title to the list
				rows.add(new Item(value));
				previousLetter = firstTitle;
			}
		}
		adapter.setRows(rows);
		setListAdapter(adapter);

		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SwipeMenuListView mListView = (SwipeMenuListView) getView()
				.findViewById(android.R.id.list);

		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				switch (menu.getViewType()) {
				case 0:
					// create menu of type 0
					// create "edit" item
					SwipeMenuItem openItem = new SwipeMenuItem(getActivity()
							.getApplicationContext());
					// set item background
					openItem.setBackground(new ColorDrawable(Color.rgb(0xC9,
							0xC9, 0xCE)));
					// set item width
					openItem.setWidth(dp2px(100));
					// set item title
					openItem.setTitle("Edit");
					// set item title fontsize
					openItem.setTitleSize(18);
					// set item title font color
					openItem.setTitleColor(Color.WHITE);
					// add to menu
					menu.addMenuItem(openItem);

					// create "delete" item
					SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity()
							.getApplicationContext());
					// set item background
					deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
							0x3F, 0x25)));
					// set item width
					deleteItem.setWidth(dp2px(100));
					// set a icon
					deleteItem.setIcon(R.drawable.trash);
					// add to menu
					menu.addMenuItem(deleteItem);
					break;
				case 1:
					// section
					// create menu of type 1
					break;
				}
			}
		};
		
		// set creator
		mListView.setMenuCreator(creator);
		mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				switch (index) {
				case 0:
					// open
					Toast.makeText(getActivity(), "Open", Toast.LENGTH_SHORT)
							.show();
					break;
				case 1:
					// delete
					Toast.makeText(getActivity(), "Delete", Toast.LENGTH_SHORT)
							.show();
					// mAppList.remove(position);
					// mAdapter.notifyDataSetChanged();
					break;
				}
				// false : close the menu; true : not close
				// the menu
				return false;
			}
		});
	}

	public void addSectionHeader(int numofDay) {

	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

	public String retrieveTitleByID(String attr_id) {
		String title = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("attr_id", attr_id));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jParser.makeHttpRequest(GET_ATRR_TITLE_URL,
					"POST", params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mAttrID = json.getJSONArray(TAG_ATTRACTION);
					JSONObject c = mAttrID.getJSONObject(0);
					title = c.getString(TAG_TITLE);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return title;
	}

}
