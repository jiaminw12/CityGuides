package com.orbital.cityguide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orbital.cityguide.adapter.DBAdapter;

public class MapsFragment extends Fragment implements LocationListener,
		OnItemSelectedListener {

	public static final String TAG = MapsFragment.class.getSimpleName();

	AutoCompleteTextView autocompleteView;
	Spinner mSprPlaceType;
	MapView mMapView;
	ImageView ins;
	private GoogleMap googleMap;

	protected ArrayAdapter<CharSequence> mapPlaceAdapter;

	String title, alertboxmsg;

	// GPS Location
	GPSTracker gps;
	// Progress Dialog
	private ProgressDialog pDialog;
	// instance variables for Marker icon drawable resources
	private int userIcon, foodIcon, drinkIcon, shopIcon, otherIcon;
	// location manager
	private LocationManager locMan;

	private Location loc;
	// user marker
	private Marker userMarker;
	// places of interest
	private Marker[] placeMarkers;

	// max
	private final int MAX_PLACES = 20;// most returned fom google
	// marker options
	private MarkerOptions[] places;

	DownloadTask placesDownloadTask;
	DownloadTask placeDetailsDownloadTask;
	ParserTask placesParserTask;
	ParserTask placeDetailsParserTask;

	final int PLACES = 0;
	final int PLACES_DETAILS = 1;

	String[] mPlaceType = null;
	String[] mPlaceTypeName = null;

	public MapsFragment() {
	}

	public static MapsFragment newInstance(String name_profile) {
		MapsFragment myFragment = new MapsFragment();
		Bundle args = new Bundle();
		args.putString("profile_username", name_profile);
		myFragment.setArguments(args);
		return myFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		gps = new GPSTracker(this.getActivity());
		// get drawable IDs
		userIcon = R.drawable.yellow_point;
		foodIcon = R.drawable.red_point;
		drinkIcon = R.drawable.blue_point;
		shopIcon = R.drawable.green_point;
		otherIcon = R.drawable.purple_point;

		final int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());
		if (status != ConnectionResult.SUCCESS) {
			title = "Alert";
			alertboxmsg = "Please update your google play services.";
			popupMessage(title, alertboxmsg);
		}

		View rootView = inflater.inflate(R.layout.fragment_maps, container,
				false);

		autocompleteView = (AutoCompleteTextView) rootView
				.findViewById(R.id.search_autocomplete);
		autocompleteView.setThreshold(1);
		autocompleteView.bringToFront();

		ins = (ImageView) rootView.findViewById(R.id.imageView1);
		ins.bringToFront();

		// Adding textchange listener
		autocompleteView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Creating a DownloadTask to download Google Places matching
				// "s"
				placesDownloadTask = new DownloadTask(PLACES);

				// Getting url to the Google Places Autocomplete api
				String url = getAutoCompleteUrl(s.toString());

				// Start downloading Google Places
				// This causes to execute doInBackground() of DownloadTask class
				placesDownloadTask.execute(url);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}

		});

		// Setting an item click listener for the AutoCompleteTextView dropdown
		// list
		autocompleteView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1,
					int position, long id) {

				ListView lv = (ListView) adapterView;

				HashMap<String, String> selectedValue = (HashMap<String, String>) (lv
						.getItemAtPosition(position));
				autocompleteView.setText(selectedValue.get("description"));

				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(autocompleteView.getWindowToken(),
						0);

				// Creating a DownloadTask to download Places details of the
				// selected place
				placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

				// Getting url to the Google Places details api
				String url = getPlaceDetailsUrl(selectedValue.get("reference"));

				// Start downloading Google Place Details
				// This causes to execute doInBackground() of DownloadTask class
				placeDetailsDownloadTask.execute(url);

			}
		});

		mMapView = (MapView) rootView.findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();// needed to get the map to display immediately

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap = mMapView.getMap();

		// create marker array
		placeMarkers = new Marker[MAX_PLACES];

		// check if GPS enabled
		if (!gps.canGetLocation()) {
			gps.showSettingsAlert();
		}

		googleMap.setMyLocationEnabled(true);
		// update location
		updatePlaces();

		return rootView;
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

	private String getAutoCompleteUrl(String place) {

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key=AIzaSyDkkR-XhiW9uVPhvM_T5GFrqrC-aeXas4U";

		// place to be be searched
		String input = "input=" + place;

		// Sensor enabled
		String sensor = "sensor=false";

		String country = "components=country:SG";

		// Building the parameters to the web service
		String parameters = input + "&" + sensor + "&" + key + "&" + country;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"
				+ output + "?" + parameters;

		return url;
	}

	private String getPlaceDetailsUrl(String ref) {

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key=AIzaSyDkkR-XhiW9uVPhvM_T5GFrqrC-aeXas4U";

		// reference of place
		String reference = "reference=" + ref;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = reference + "&" + sensor + "&" + key;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/details/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {

		private int downloadType = 0;

		// Constructor
		public DownloadTask(int type) {
			this.downloadType = type;
		}

		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			switch (downloadType) {
			case PLACES:
				// Creating ParserTask for parsing Google Places
				placesParserTask = new ParserTask(PLACES);

				// Start parsing google places json data
				// This causes to execute doInBackground() of ParserTask class
				placesParserTask.execute(result);

				break;

			case PLACES_DETAILS:
				// Creating ParserTask for parsing Google Places
				placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

				// Starting Parsing the JSON string
				// This causes to execute doInBackground() of ParserTask class
				placeDetailsParserTask.execute(result);
			}
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		int parserType = 0;

		public ParserTask(int type) {
			this.parserType = type;
		}

		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<HashMap<String, String>> list = null;

			try {
				jObject = new JSONObject(jsonData[0]);

				switch (parserType) {
				case PLACES:
					PlaceJSONParser placeJsonParser = new PlaceJSONParser();
					// Getting the parsed data as a List construct
					list = placeJsonParser.parse(jObject);
					break;
				case PLACES_DETAILS:
					PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
					// Getting the parsed data as a List construct
					list = placeDetailsJsonParser.parse(jObject);
				}

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return list;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {

			switch (parserType) {
			case PLACES:
				String[] from = new String[] { "description" };
				int[] to = new int[] { android.R.id.text1 };

				// Creating a SimpleAdapter for the AutoCompleteTextView
				SimpleAdapter adapter = new SimpleAdapter(getActivity(),
						result, android.R.layout.simple_list_item_1, from, to);

				// Setting the adapter
				autocompleteView.setAdapter(adapter);

				break;
			case PLACES_DETAILS:
				HashMap<String, String> hm = result.get(0);

				// Getting latitude from the parsed data
				double latitude = Double.parseDouble(hm.get("lat"));

				// Getting longitude from the parsed data
				double longitude = Double.parseDouble(hm.get("lng"));

				// Getting GoogleMap from SupportMapFragment
				googleMap = mMapView.getMap();

				// remove any existing marker
				if (userMarker != null)
					googleMap.clear();
				userMarker.remove();

				LatLng point = new LatLng(latitude, longitude);

				CameraUpdate cameraPosition = CameraUpdateFactory
						.newLatLng(point);
				CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

				// Showing the user input location in the Google Map
				googleMap.moveCamera(cameraPosition);
				googleMap.animateCamera(cameraZoom);

				MarkerOptions options = new MarkerOptions();
				options.position(point);
				options.title("Position");
				options.snippet("Latitude:" + latitude + ",Longitude:"
						+ longitude);

				// Adding the marker in the Google Map
				googleMap.addMarker(options);

				String latVal = String.valueOf(latitude);
				String lngVal = String.valueOf(longitude);
				String url;
				try {
					url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
							+ URLEncoder.encode(latVal, "UTF-8")
							+ ","
							+ URLEncoder.encode(lngVal, "UTF-8")
							+ "&radius="
							+ URLEncoder.encode("500", "UTF-8")
							+ "&sensor="
							+ URLEncoder.encode("true", "UTF-8")
							+ "&types="
							+ URLEncoder
									.encode("food|bar|shopping_mall|museum|art_gallery",
											"UTF-8")
							+ "&key="
							+ URLEncoder.encode(
									"AIzaSyDkkR-XhiW9uVPhvM_T5GFrqrC-aeXas4U",
									"UTF-8");
					new GetPlaces().execute(url);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
		}
	}

	private void updatePlaces() {

		double lat = 0;
		double lng = 0;

		gps = new GPSTracker(this.getActivity());

		loc = getLocation();
		if (loc == null) {
			// get location manager
			locMan = (LocationManager) getActivity().getSystemService(
					Context.LOCATION_SERVICE);
		}

		// remove any existing marker
		if (userMarker != null) {
			googleMap.clear();
			userMarker.remove();
		}

		if (!(gps.canGetLocation())) {
			locMan = (LocationManager) getActivity().getSystemService(
					Context.LOCATION_SERVICE);
			SharedPreferences pref = this.getActivity().getSharedPreferences(
					"PREFERENCE", Context.MODE_PRIVATE);
			float prev_lat = pref.getFloat("latitude", 0);
			float prev_lng = pref.getFloat("longtitude", 0);
			lat = prev_lat;
			lng = prev_lng;
		} else {
			lat = gps.getLatitude();
			lng = gps.getLongitude();

			Log.v("lat :: ", String.valueOf(lat));
			Log.v("long : ", String.valueOf(lng));

			SharedPreferences prefs = this.getActivity().getSharedPreferences(
					"PREFERENCE", Context.MODE_PRIVATE);
			prefs.edit().putFloat("latitude", (float) lat).commit();
			prefs.edit().putFloat("longtitude", (float) lng).commit();
		}

		// create LatLng
		LatLng lastLatLng = new LatLng(lat, lng);

		// create and set marker properties
		userMarker = googleMap.addMarker(new MarkerOptions().position(
				lastLatLng).title("You are here"));
		// move to location
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(lat, lng)).zoom(14).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

		// build places query string

		String latVal = String.valueOf(lat);
		String lngVal = String.valueOf(lng);
		String url;
		try {
			url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
					+ URLEncoder.encode(latVal, "UTF-8")
					+ ","
					+ URLEncoder.encode(lngVal, "UTF-8")
					+ "&radius="
					+ URLEncoder.encode("500", "UTF-8")
					+ "&sensor="
					+ URLEncoder.encode("true", "UTF-8")
					+ "&types="
					+ URLEncoder.encode("food|bar|bus_station", "UTF-8")
					+ "&key="
					+ URLEncoder.encode(
							"AIzaSyDkkR-XhiW9uVPhvM_T5GFrqrC-aeXas4U", "UTF-8");
			new GetPlaces().execute(url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// flag for GPS status
		boolean isGPSEnabled = false;

		// flag for network status
		boolean isNetworkEnabled = false;

		// getting GPS status
		isGPSEnabled = locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		isNetworkEnabled = locMan
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (isGPSEnabled) {
			locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000,
					1000, this);

		} else if (isNetworkEnabled) {
			locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					30000, 1000, this);
		}

	}

	class GetPlaces extends AsyncTask<String, String, String> {

		/* Before starting background thread Show Progress Dialog */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Loading the map. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... placesURL) {
			// fetch places

			// build result as string
			StringBuilder placesBuilder = new StringBuilder();
			// process search parameter string(s)
			for (String placeSearchURL : placesURL) {
				HttpClient placesClient = new DefaultHttpClient();
				try {
					// try to fetch the data

					// HTTP Get receives URL string
					HttpGet placesGet = new HttpGet(placeSearchURL);
					// execute GET with Client - return response
					HttpResponse placesResponse = placesClient
							.execute(placesGet);
					// check response status
					StatusLine placeSearchStatus = placesResponse
							.getStatusLine();
					// only carry on if response is OK
					if (placeSearchStatus.getStatusCode() == 200) {
						// get response entity
						HttpEntity placesEntity = placesResponse.getEntity();
						// get input stream setup
						InputStream placesContent = placesEntity.getContent();
						// create reader
						InputStreamReader placesInput = new InputStreamReader(
								placesContent);
						// use buffered reader to process
						BufferedReader placesReader = new BufferedReader(
								placesInput);
						// read a line at a time, append to string builder
						String lineIn;
						while ((lineIn = placesReader.readLine()) != null) {
							placesBuilder.append(lineIn);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return placesBuilder.toString();
		}

		/** After completing background task Dismiss the progress dialog **/
		protected void onPostExecute(String result) {
			// parse place data returned from Google Places
			// remove existing markers
			if (placeMarkers != null) {
				for (int pm = 0; pm < placeMarkers.length; pm++) {
					if (placeMarkers[pm] != null)
						placeMarkers[pm].remove();
				}
			}
			try {
				// parse JSON

				// create JSONObject, pass string returned from doInBackground
				JSONObject resultObject = new JSONObject(result);
				// get "results" array
				JSONArray placesArray = resultObject.getJSONArray("results");
				// marker options for each place returned
				places = new MarkerOptions[placesArray.length()];
				// loop through places
				for (int p = 0; p < placesArray.length(); p++) {
					// parse each place
					// if any values are missing we won't show the marker
					boolean missingValue = false;
					LatLng placeLL = null;
					String placeName = "";
					String vicinity = "";
					int currIcon = otherIcon;
					try {
						// attempt to retrieve place data values
						missingValue = false;
						// get place at this index
						JSONObject placeObject = placesArray.getJSONObject(p);
						// get location section
						JSONObject loc = placeObject.getJSONObject("geometry")
								.getJSONObject("location");
						// read lat lng
						placeLL = new LatLng(Double.valueOf(loc
								.getString("lat")), Double.valueOf(loc
								.getString("lng")));
						// get types
						JSONArray types = placeObject.getJSONArray("types");
						// loop through types
						for (int t = 0; t < types.length(); t++) {
							// what type is it
							String thisType = types.get(t).toString();
							// check for particular types - set icons
							if (thisType.contains("food")) {
								currIcon = foodIcon;
								break;
							} else if (thisType.contains("bar")) {
								currIcon = drinkIcon;
								break;
							} else if (thisType.contains("store")) {
								currIcon = shopIcon;
								break;
							}
						}
						// vicinity
						vicinity = placeObject.getString("vicinity");
						// name
						placeName = placeObject.getString("name");
					} catch (JSONException jse) {
						Log.v("PLACES", "missing value");
						missingValue = true;
						jse.printStackTrace();
					}
					// if values missing we don't display
					if (missingValue)
						places[p] = null;
					else
						places[p] = new MarkerOptions()
								.position(placeLL)
								.title(placeName)
								.icon(BitmapDescriptorFactory
										.fromResource(currIcon))
								.snippet(vicinity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (places != null && placeMarkers != null) {
				for (int p = 0; p < places.length && p < placeMarkers.length; p++) {
					// will be null if a value was missing
					if (places[p] != null)
						placeMarkers[p] = googleMap.addMarker(places[p]);
				}
			}

			// dismiss the dialog once got all details
			pDialog.dismiss();

		}

	}

	public void onLocationChanged(Location location) {
		// remove any existing marker
		if (userMarker != null) {
			googleMap.clear();
			userMarker.remove();
		}

		Log.v("MyMapActivity", "location changed");
		LatLng position = new LatLng(location.getLatitude(),
				location.getLongitude());
		// create and set marker properties
		userMarker = googleMap.addMarker(new MarkerOptions().position(position)
				.title("You are here"));
		updatePlaces();
		// Zoom parameter is set to 14
		CameraUpdate update = CameraUpdateFactory.newLatLng(position);

		// Use map.animateCamera(update) if you want moving effect
		googleMap.moveCamera(update);
		// mMapView.onResume();
	}

	public void onProviderDisabled(String provider) {
		Log.v("MyMapActivity", "provider disabled");
	}

	public void onProviderEnabled(String provider) {
		Log.v("MyMapActivity", "provider enabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.v("MyMapActivity", "status changed");
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		// locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		// 30000, 1000, this);
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000,
				1000, this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	public void popupMessage(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		builder.setTitle(title)
				.setMessage(msg)
				.setNegativeButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});

		AlertDialog alert = builder.create();
		alert.show();
	}

	public Location getLocation() {
		boolean gps_enabled = false;
		boolean network_enabled = false;

		LocationManager lm = (LocationManager) this.getActivity()
				.getSystemService(Context.LOCATION_SERVICE);

		gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		network_enabled = lm
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		Location net_loc = null, gps_loc = null, finalLoc = null;

		if (gps_enabled)
			gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (network_enabled)
			net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (gps_loc != null && net_loc != null) {

			if (gps_loc.getAccuracy() >= net_loc.getAccuracy())
				finalLoc = gps_loc;
			else
				finalLoc = net_loc;

		} else {

			if (gps_loc != null) {
				finalLoc = net_loc;
			} else if (net_loc != null) {
				finalLoc = gps_loc;
			}
		}

		return finalLoc;
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) this
				.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (mWifi != null && mWifi.isConnected()) {
			return true;
		}
		return false;
	}

}
