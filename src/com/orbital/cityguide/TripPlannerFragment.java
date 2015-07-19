package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Item;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Row;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Section;
import com.orbital.cityguide.adapter.DBAdapter;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class TripPlannerFragment extends ListFragment {

	protected ArrayAdapter<CharSequence> dayAdapter;

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mPlannerList = new ArrayList<HashMap<String, String>>();
	private ArrayList<String> mPlannerItem = new ArrayList<String>();
	private HashMap<String, Integer> mPlannerItem_Adapter = new HashMap<String, Integer>();

	private PlannerDragNDropListAdapter adapter;
	private HashMap<String, Integer> sections = new HashMap<String, Integer>();
	List<Row> rows = new ArrayList<Row>();

	private static final String GET_ATRR_TITLE_URL = "http://192.168.1.7/City_Guide/getAttractionByID.php";
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

		mPlannerItem.add("Waiting List");
		mPlannerItem_Adapter.put("Waiting List", 0);

		//new LoadPlannerList().execute();

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
					// addSectionHeader(1);
				} else if (value.equalsIgnoreCase("2")) {
					// addSectionHeader(2);
				} else if (value.equalsIgnoreCase("3")) {
					// addSectionHeader(3);
				} else if (value.equalsIgnoreCase("4")) {
					// addSectionHeader(4);
				} else if (value.equalsIgnoreCase("5")) {
					// addSectionHeader(5);
				} else if (value.equalsIgnoreCase("6")) {
					// addSectionHeader(6);
				} else if (value.equalsIgnoreCase("7")) {
					// addSectionHeader(7);
				} else if (value.equalsIgnoreCase("8")) {
					// addSectionHeader(8);
				} else if (value.equalsIgnoreCase("9")) {
					// addSectionHeader(9);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		
		try {
			dbAdaptor.open();
			cursor = dbAdaptor.getAllPlanner();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				int i = 1;
				do {
					String attr_title = retrieveTitleByID(cursor
							.getString(0));
					String tag_title = cursor.getString(1);

					HashMap<String, String> map = new HashMap<String, String>();
					map.put(tag_title, attr_title);
					mPlannerItem.add(attr_title);
					mPlannerItem_Adapter.put(attr_title, i);
					i++;
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
			
			adapter = new PlannerDragNDropListAdapter(getActivity(), mPlannerItem);
			adapter.setList(mPlannerItem_Adapter);
		}
		
		int start = 0;
		String previousLetter = null;

		for (HashMap<String, String> map : mPlannerList) {
			for (String str : map.keySet()) {
				String key = str;
				String value = map.get(key);
				String firstTitke = key;

				// Check if we need to add a header row
				if (!firstTitke.equals(previousLetter)) {
					rows.add(new Section(firstTitke));
					sections.put(firstTitke, start);
				}

				// Add the title to the list
				rows.add(new Item(value));
				previousLetter = firstTitke;
			}
		}

		adapter.setRows(rows);
		setListAdapter(adapter);
		//layerList.setList(mPlannerItem);
		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Get listview
		final DynamicListView lv = (DynamicListView) getListView();
		
		lv.setAdapter(adapter);
		lv.setList(mPlannerItem);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.invalidateViews();

	}

	class LoadPlannerList extends AsyncTask<String, String, String> {

		/* getting All Planner List Item */
		protected String doInBackground(String... args) {
			try {
				dbAdaptor.open();
				cursor = dbAdaptor.getAllPlanner();
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					int i = 1;
					do {
						String attr_title = retrieveTitleByID(cursor
								.getString(0));
						String tag_title = cursor.getString(1);

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(tag_title, attr_title);
						mPlannerItem.add(attr_title);
						mPlannerItem_Adapter.put(attr_title, i);
						i++;
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

				adapter.setList(mPlannerItem_Adapter);
			}

			return null;
		}

		/* After completing background task Dismiss the progress dialog */
		protected void onPostExecute(String file_url) {

			int start = 0;
			String previousLetter = null;

			for (HashMap<String, String> map : mPlannerList) {
				for (String str : map.keySet()) {
					String key = str;
					String value = map.get(key);
					String firstTitke = key;

					// Check if we need to add a header row
					if (!firstTitke.equals(previousLetter)) {
						rows.add(new Section(firstTitke));
						sections.put(firstTitke, start);
					}

					// Add the title to the list
					rows.add(new Item(value));
					previousLetter = firstTitke;
				}
			}

			adapter.setRows(rows);
			setListAdapter(adapter);

		}

		private Context getActivity() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public void addSectionHeader(int numofDay) {
		int start = 0;
		for (int i = 1; i < numofDay + 1; i++) {
			String title = "Day " + i;
			rows.add(new Section(title));
			sections.put(title, start);
		}

		adapter.setRows(rows);
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
