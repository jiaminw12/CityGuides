package com.orbital.cityguide;

import com.orbital.cityguide.adapter.ExpandableListAdapter;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Item;
import com.orbital.cityguide.adapter.PlannerDragNDropListAdapter.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class PackageFragment extends Fragment {

	protected ArrayAdapter<CharSequence> typeAdapter;

	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();

	private static final String GETPACKAGE_URL = "http://" + ipadress
			+ "/getPackageList.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PACKAGE_TITLE = "package_title";
	private static final String TAG_ATRTITLE = "attr_title";
	private static final String TAG_PACKAGE = "packages";
	private JSONArray mPackage = null;

	JSONParser jParser = new JSONParser();
	SharedPreferences pref;
	static String name_profile;

	Spinner typeSpinner = null;

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader = new ArrayList<String>();
	HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
	private ArrayList<HashMap<String, String>> mPackageList = new ArrayList<HashMap<String, String>>();

	View rootView;

	public PackageFragment() {
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		pref = this.getActivity().getSharedPreferences("PREFERENCE",
				Context.MODE_PRIVATE);
		rootView = inflater
				.inflate(R.layout.package_fragment, container, false);

		typeSpinner = (Spinner) rootView
				.findViewById(R.id.typeTraveller_spinner);
		this.typeAdapter = ArrayAdapter.createFromResource(this.getActivity(),
				R.array.arrayTypeOfTraveller,
				android.R.layout.simple_spinner_item);
		typeSpinner.setAdapter(this.typeAdapter);
		final String type = pref.getString("typeOfTraveller", null);
		int pposition = typeAdapter.getPosition(type);
		typeSpinner.setSelection(pposition);
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String mType = typeSpinner.getSelectedItem().toString();
				pref.edit().putString("typeOfTraveller", mType).commit();
				mPackageList.clear();
				prepareListData(String.valueOf(position + 1));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});
		expListView = (ExpandableListView) rootView
				.findViewById(R.id.expListView);
		prepareListData(String.valueOf(pposition + 1));
		

		return rootView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	/*
	 * Preparing the list data
	 */
	private void prepareListData(String traveller_id) {

		int success;
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();
		
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("traveller_id", traveller_id));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jParser.makeHttpRequest(GETPACKAGE_URL, "POST",
					params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					// attractions found
					// Getting Array of Products
					mPackage = json.getJSONArray(TAG_PACKAGE);

					// looping through All Attractions
					for (int i = 0; i < mPackage.length(); i++) {
						JSONObject c = mPackage.getJSONObject(i);
						String mPackageTitle = c.getString(TAG_PACKAGE_TITLE);
						String mAttrTitle = c.getString(TAG_ATRTITLE);
						listDataHeader.add(mPackageTitle);
						HashMap<String, String> packageList = new HashMap<String, String>();
						packageList.put(mPackageTitle, mAttrTitle);
						mPackageList.add(packageList);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		List<String> myChildData = new ArrayList<String>();
		int num = 0;
		for (HashMap<String, String> map : mPackageList) {
			for (int i = 0; i < map.size(); i++) {
				for (String str : map.keySet()) {
					String key = str;
					String value = map.get(key);
					String[] parts = value.split(",");

					for (int k = 0; k < parts.length; k++) {
						myChildData.add(parts[k]);
					}
					listDataChild.put(listDataHeader.get(num), myChildData);
					num++;
					myChildData = new ArrayList<String>();
				}
			}
		}
		
		listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader,
				listDataChild);
		// setting list adapter
		expListView.setAdapter(listAdapter);
	}

}
