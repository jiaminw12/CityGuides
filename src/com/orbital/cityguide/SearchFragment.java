package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchFragment extends ListFragment implements OnItemSelectedListener {
	
	Button mBtnPlanner;
	Spinner searchSpinner = null;
	
	protected ArrayAdapter<CharSequence> searchAdapter;

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mAttractionsList;

	JSONParser jParser = new JSONParser();

	private static final String READATTR_URL = "http://192.168.1.5/City_Guide/getAllAttractions.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_TITLE = "attr_title";
	private static final String TAG_ATTRACTION = "attractions";
	// An array of all of our attractions
	private JSONArray mAttractions = null;
	
	String name_profile;

	public SearchFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_search, container,
				false);
		
		searchSpinner = (Spinner) rootView.findViewById(R.id.search_spinner);
		this.searchAdapter = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.arraySearch, android.R.layout.simple_spinner_item);
		searchSpinner.setAdapter(this.searchAdapter);
		searchSpinner.setOnItemSelectedListener(this);

		Bundle bundle = this.getArguments();
		name_profile = bundle.getString("profile_username", name_profile);

		// Hashmap for ListView
		mAttractionsList = new ArrayList<HashMap<String, String>>();
		// Loading products in Background Thread
		new LoadAllProducts().execute();

		setRetainInstance(true);
		return rootView;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		String value = searchSpinner.getSelectedItem().toString();

		if (value.equalsIgnoreCase("all")) {
		} else if (value.equalsIgnoreCase("category")) {
		} else if (value.equalsIgnoreCase("area")) {
		} else if (value.equalsIgnoreCase("price")) {
		} 
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Get listview
		ListView lv = getListView();

		// on seleting single attraction
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem
				String aid = ((TextView) view.findViewById(R.id.aid)).getText()
						.toString();

				// Starting new intent
				Intent in = new Intent(getActivity(), AttractionDetails.class);
				// sending aid to next activity
				in.putExtra("AID", aid);
				in.putExtra("profile_username", name_profile);
				startActivity(in);
			}
		});
	}

	/**
	 * Background Async Task to Load all product by making HTTP Request
	 * */
	class LoadAllProducts extends AsyncTask<String, String, String> {

		/**
		 * getting All products from url
		 * */
		protected String doInBackground(String... args) {
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

						// adding each child node to HashMap key => value
						map.put(TAG_AID, id);
						map.put(TAG_TITLE, name);

						// adding HashList to ArrayList
						mAttractionsList.add(map);
					}
				} else {
					// no products found
					// Launch Add New product Activity
					// Intent i = new Intent(getApplicationContext(),
					// NewProductActivity.class);
					// Closing all previous activities
					// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// startActivity(i);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			/**
			 * Updating parsed JSON data into ListView
			 * */
			ListAdapter adapter = new SimpleAdapter(getActivity(),
					mAttractionsList, R.layout.list_item, new String[] {
							TAG_AID, TAG_TITLE }, new int[] { R.id.aid,
							R.id.name });
			// updating listview
			setListAdapter(adapter);

		}

	}

	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
