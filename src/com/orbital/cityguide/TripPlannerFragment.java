package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.orbital.cityguide.SearchFragment.SideIndexGestureListener;
import com.orbital.cityguide.adapter.AlphabetListAdapter;
import com.orbital.cityguide.adapter.AlphabetListAdapter.Item;
import com.orbital.cityguide.adapter.AlphabetListAdapter.Row;
import com.orbital.cityguide.adapter.AlphabetListAdapter.Section;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class TripPlannerFragment extends ListFragment implements OnItemSelectedListener {
	
	Spinner searchSpinner = null;
	ListView mListView;
	LinearLayout sideIndex;

	protected ArrayAdapter<CharSequence> searchAdapter;

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mAttractionsList;

	private AlphabetListAdapter adapter = new AlphabetListAdapter();
	private List<Object[]> alphabet = new ArrayList<Object[]>();
	private HashMap<String, Integer> sections = new HashMap<String, Integer>();

	private static final String RETRIEVEID_URL = "http://192.168.1.9/City_Guide/getAttractionByTitle.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_TITLE = "attr_title";
	private static final String TAG_ATTRACTION = "attractions";

	// An array of all of our attractions
	private JSONArray mAttractions = null;

	private JSONArray mAttrID = null;
	
	JSONParser jParser = new JSONParser();

	DBAdapter dbAdaptor;
	
	String name_profile;
	int success;
	
	public TripPlannerFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_planner, container, false);
        
        Bundle bundle = this.getArguments();
		name_profile = bundle.getString("profile_username", name_profile);
         
        return rootView;
    }
	
	class LoadProductsCAT extends AsyncTask<String, String, String> {

		/* getting All products from url */
		protected String doInBackground(String... args) {
			mAttractionsList.clear();
			
			return null;
		}

		/* After completing background task Dismiss the progress dialog */
		protected void onPostExecute(String file_url) {
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
