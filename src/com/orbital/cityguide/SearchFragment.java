package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orbital.cityguide.adapter.AlphabetListAdapter;
import com.orbital.cityguide.adapter.AlphabetListAdapter.Item;
import com.orbital.cityguide.adapter.AlphabetListAdapter.Row;
import com.orbital.cityguide.adapter.AlphabetListAdapter.Section;

import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SearchFragment extends ListFragment implements
		OnItemSelectedListener {

	Spinner searchSpinner = null;
	ListView mListView;
	LinearLayout sideIndex;

	protected ArrayAdapter<CharSequence> searchAdapter;

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mAttractionsList;

	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private GestureDetector mGestureDetector;
	private List<Object[]> alphabet = new ArrayList<Object[]>();
	private HashMap<String, Integer> sections = new HashMap<String, Integer>();
	private int sideIndexHeight;
	private static float sideIndexX;
	private static float sideIndexY;
	private int indexListSize;

	private static final String RETRIEVEID_URL = "http://192.168.1.9/City_Guide/getAttractionIDByTitle.php";
	private static final String READATTR_URL = "http://192.168.1.9/City_Guide/getAllAttractions.php";
	private static final String READATTRBYCATS_URL = "http://192.168.1.9/City_Guide/getAllAttractionsCAT.php";
	private static final String READATTRBYAREA_URL = "http://192.168.1.9/City_Guide/getAllAttractionsAREA.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_TITLE = "attr_title";
	private static final String TAG_CATEGORY = "category_title";
	private static final String TAG_AREA = "location_title";
	private static final String TAG_ATTRACTION = "attractions";

	// An array of all of our attractions
	private JSONArray mAttractions = null;

	private JSONArray mAttrID = null;
	
	JSONParser jParser = new JSONParser();

	String name_profile;
	int success;
	
	DBAdapter dbAdaptor;

	public SearchFragment() {
	}

	class SideIndexGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			sideIndexX = sideIndexX - distanceX;
			sideIndexY = sideIndexY - distanceY;

			if (sideIndexX >= 0 && sideIndexY >= 0) {
				displayListItem();
			}

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_search, container,
				false);

		searchSpinner = (Spinner) rootView.findViewById(R.id.search_spinner);
		this.searchAdapter = ArrayAdapter.createFromResource(
				this.getActivity(), R.array.arraySearch,
				android.R.layout.simple_spinner_item);
		searchSpinner.setAdapter(this.searchAdapter);
		searchSpinner.setOnItemSelectedListener(this);
		
		mListView = (ListView)rootView.findViewById(R.id.list);

		Bundle bundle = this.getArguments();
		name_profile = bundle.getString("profile_username", name_profile);

		// Hashmap for ListView
		mAttractionsList = new ArrayList<HashMap<String, String>>();

		sideIndex = (LinearLayout) rootView.findViewById(R.id.sideIndex);
		sideIndex.bringToFront();

		setRetainInstance(true);
		
		return rootView;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		String value = searchSpinner.getSelectedItem().toString();

		if (value.equalsIgnoreCase("all")) {
			sideIndex.setVisibility(View.VISIBLE);
			new LoadAllProducts().execute();
		} else if (value.equalsIgnoreCase("category")) {
			sideIndex.setVisibility(View.GONE);
			new LoadProductsCAT().execute();
		} else if (value.equalsIgnoreCase("area")) {
			sideIndex.setVisibility(View.GONE);
			new LoadProductsAREA().execute();
		} 
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Get listview
		final ListView lv = getListView();

		// on seleting single attraction
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView text = (TextView) view.findViewById(R.id.name);
				String title = text.getText().toString();
				String key = retrieveId(title);
				if(key != null){
					Intent in = new Intent(getActivity(),
							AttractionDetails.class);
					// sending aid to next activity
					in.putExtra("AID", key);
					in.putExtra("profile_username", name_profile);
					startActivity(in);
				}
			}
		});
	}

	class LoadAllProducts extends AsyncTask<String, String, String> {

		/* getting All products from url */
		protected String doInBackground(String... args) {
			mAttractionsList.clear();
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			// getting JSON string from URL
			JSONObject json;

			try {
				json = jParser.makeHttpRequest(READATTR_URL, "GET", params);
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// attractions found
					// Getting Array of Products
					mAttractions = json.getJSONArray(TAG_ATTRACTION);

					// looping through All Attractions
					for (int i = 0; i < mAttractions.length(); i++) {
						JSONObject c = mAttractions.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_AID);
						String name = c.getString(TAG_TITLE);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(id, name);

						// adding HashList to ArrayList
						mAttractionsList.add(map);
					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			return null;
		}

		/* After completing background task Dismiss the progress dialog */
		protected void onPostExecute(String file_url) {
			mGestureDetector = new GestureDetector(
					new SideIndexGestureListener());
			List<Row> rows = new ArrayList<Row>();
			int start = 0;
			int end = 0;
			String previousLetter = null;
			Object[] tmpIndexItem = null;
			Pattern numberPattern = Pattern.compile("[0-9]");

			for (HashMap<String, String> map : mAttractionsList) {
				for (String str : map.keySet()) {
					String key = str;
					String value = map.get(key);
					String firstLetter = value.substring(0, 1);

					// Group numbers together in the scroller
					if (numberPattern.matcher(firstLetter).matches()) {
						firstLetter = "#";
					}

					// If we've changed to a new letter, add the previous letter
					// to the alphabet scroller
					if (previousLetter != null
							&& !firstLetter.equals(previousLetter)) {
						end = rows.size() - 1;
						tmpIndexItem = new Object[3];
						tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
						tmpIndexItem[1] = start;
						tmpIndexItem[2] = end;
						alphabet.add(tmpIndexItem);

						start = end + 1;
					}

					// Check if we need to add a header row
					if (!firstLetter.equals(previousLetter)) {
						rows.add(new Section(firstLetter));
						sections.put(firstLetter, start);
					}

					// Add the title to the list
					rows.add(new Item(value));
					previousLetter = firstLetter;
				}
			}

			if (previousLetter != null) {
				// Save the last letter
				tmpIndexItem = new Object[3];
				tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
				tmpIndexItem[1] = start;
				tmpIndexItem[2] = rows.size() - 1;
				alphabet.add(tmpIndexItem);
			}

			updateList(alphabet.size());
			adapter.setRows(rows);
			setListAdapter(adapter);
		}

		private Context getActivity() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class LoadProductsCAT extends AsyncTask<String, String, String> {

		/* getting All products from url */
		protected String doInBackground(String... args) {
			mAttractionsList.clear();
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			// getting JSON string from URL
			JSONObject json;

			try {
				json = jParser.makeHttpRequest(READATTRBYCATS_URL, "GET", params);
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// attractions found
					// Getting Array of Products
					mAttractions = json.getJSONArray(TAG_ATTRACTION);

					// looping through All Attractions
					for (int i = 0; i < mAttractions.length(); i++) {
						JSONObject c = mAttractions.getJSONObject(i);

						// Storing each json item in variable
						String name = c.getString(TAG_TITLE);
						String category = c.getString(TAG_CATEGORY);
						
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(category, name);

						// adding HashList to ArrayList
						mAttractionsList.add(map);

					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			return null;
		}

		/* After completing background task Dismiss the progress dialog */
		protected void onPostExecute(String file_url) {
			mGestureDetector = new GestureDetector(
					new SideIndexGestureListener());
			List<Row> rows = new ArrayList<Row>();
			int start = 0;
			int end = 0;
			String previousLetter = null;

			for (HashMap<String, String> map : mAttractionsList) {
				for (String str : map.keySet()) {
					String key = str;
					String value = map.get(key);
					String firstLetter = key.toUpperCase(Locale.US);

					// Check if we need to add a header row
					if (!firstLetter.equals(previousLetter)) {
						rows.add(new Section(firstLetter));
						sections.put(firstLetter, start);
					}

					// Add the title to the list
					rows.add(new Item(value));
					previousLetter = firstLetter;
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
	
	class LoadProductsAREA extends AsyncTask<String, String, String> {

		/* getting All products from url */
		protected String doInBackground(String... args) {
			mAttractionsList.clear();
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			// getting JSON string from URL
			JSONObject json;

			try {
				json = jParser.makeHttpRequest(READATTRBYAREA_URL, "GET", params);
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// attractions found
					// Getting Array of Products
					mAttractions = json.getJSONArray(TAG_ATTRACTION);

					// looping through All Attractions
					for (int i = 0; i < mAttractions.length(); i++) {
						JSONObject c = mAttractions.getJSONObject(i);

						// Storing each json item in variable
						String name = c.getString(TAG_TITLE);
						String area = c.getString(TAG_AREA);
						
						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(area, name);

						// adding HashList to ArrayList
						mAttractionsList.add(map);

					}
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			return null;
		}

		/* After completing background task Dismiss the progress dialog */
		protected void onPostExecute(String file_url) {
			mGestureDetector = new GestureDetector(
					new SideIndexGestureListener());
			List<Row> rows = new ArrayList<Row>();
			int start = 0;
			String previousLetter = null;

			for (HashMap<String, String> map : mAttractionsList) {
				for (String str : map.keySet()) {
					String key = str;
					String value = map.get(key);
					String firstLetter = key.toUpperCase(Locale.US);

					// Check if we need to add a header row
					if (!firstLetter.equals(previousLetter)) {
						rows.add(new Section(firstLetter));
						sections.put(firstLetter, start);
					}

					// Add the title to the list
					rows.add(new Item(value));
					previousLetter = firstLetter;
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

	public void displayListItem() {

		sideIndexHeight = sideIndex.getHeight();
		// compute number of pixels for every side index item
		double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

		// compute the item index for given event position belongs to
		int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

		// get the item (we can do it since we know item index)
		if (itemPosition < alphabet.size()) {
			Object[] indexItem = alphabet.get(itemPosition);
			int subitemPosition = sections.get(indexItem[0]);

			// ListView listView = (ListView) findViewById(android.R.id.list);
			getListView().setSelection(subitemPosition);
		}
	}

	public void updateList(int size) {
		sideIndex.removeAllViews();
		indexListSize = alphabet.size();

		if (indexListSize < 1) {
			return;
		}

		int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
		int tmpIndexListSize = indexListSize;
		while (tmpIndexListSize > indexMaxSize) {
			tmpIndexListSize = tmpIndexListSize / 2;
		}
		double delta;
		if (tmpIndexListSize > 0) {
			delta = indexListSize / tmpIndexListSize;
		} else {
			delta = 1;
		}

		TextView tmpTV;
		for (double i = 1; i <= indexListSize; i = i + delta) {
			Object[] tmpIndexItem = alphabet.get((int) i - 1);
			String tmpLetter = tmpIndexItem[0].toString();

			tmpTV = new TextView(getActivity());
			tmpTV.setText(tmpLetter);
			tmpTV.setGravity(Gravity.CENTER);
			tmpTV.setTextSize(15);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 1);
			tmpTV.setLayoutParams(params);
			sideIndex.addView(tmpTV);
		}

		sideIndexHeight = sideIndex.getHeight();

		sideIndex.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// now you know coordinates of touch
				sideIndexX = event.getX();
				sideIndexY = event.getY();

				// and can display a proper item it country list
				displayListItem();

				return false;
			}
		});
	}

	public String retrieveId(String attr_title) {
		String id = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("attr_title", attr_title));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jParser.makeHttpRequest(RETRIEVEID_URL,
					"POST", params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mAttrID = json.getJSONArray(TAG_ATTRACTION);
					JSONObject c = mAttrID.getJSONObject(0);
					id = c.getString(TAG_AID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return id;
	}


}
