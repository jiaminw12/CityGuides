package com.orbital.cityguide;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements LocationListener {

	EditText mSearch;
	MapView mMapView;
	private GoogleMap googleMap;

	String title, alertboxmsg;

	// Progress Dialog
	private ProgressDialog pDialog;

	// instance variables for Marker icon drawable resources
	private int userIcon, foodIcon, drinkIcon, shopIcon, otherIcon;

	// location manager
	private LocationManager locMan;

	// user marker
	private Marker userMarker;

	// places of interest
	private Marker[] placeMarkers;
	// max
	private final int MAX_PLACES = 20;// most returned from google
	// marker options
	private MarkerOptions[] places;

	public MapsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// get drawable IDs
		userIcon = R.drawable.yellow_point;
		foodIcon = R.drawable.red_point;
		drinkIcon = R.drawable.blue_point;
		shopIcon = R.drawable.green_point;
		otherIcon = R.drawable.purple_point;

		boolean result = isNetworkAvailable();

		if (result == false) {
			title = "Message";
			alertboxmsg = "No internet connection!";
			popupMessage(title, alertboxmsg);
		}

		View rootView = inflater.inflate(R.layout.fragment_maps, container,
				false);
		mSearch = (EditText) rootView.findViewById(R.id.editTextLocation);
		mMapView = (MapView) rootView.findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);

		mSearch.bringToFront();
		mMapView.onResume();// needed to get the map to display immediately

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap = mMapView.getMap();
		// latitude and longitude
		double latitude = 37.826237;
		double longitude = -122.156982;

		// create marker array
		placeMarkers = new Marker[MAX_PLACES];
		// update location
		updatePlaces();

		// http://thuongnh.com/google-maps-android-v2-tutorial/
		/*
		 * //Enable GPS map.setMyLocationEnabled(true);
		 * 
		 * //Set the map to current location
		 * map.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
		 * 
		 * @Override public void onMyLocationChange(Location location) { LatLng
		 * position = new LatLng(location.getLatitude(),
		 * location.getLongitude());
		 * 
		 * //Zoom parameter is set to 14 CameraUpdate update =
		 * CameraUpdateFactory.newLatLngZoom(position, 14);
		 * 
		 * //Use map.animateCamera(update) if you want moving effect
		 * map.moveCamera(update); mapView.onResume(); } });
		 */

		return rootView;

	}

	// location listener functions

	public void onLocationChanged(Location location) {
		Log.v("MyMapActivity", "location changed");
		updatePlaces();
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

	private void updatePlaces() {
		// get location manager
		locMan = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		// get last location
		Location lastLoc = locMan
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		// double lat = lastLoc.getLatitude();
		// double lng = lastLoc.getLongitude();

		double lat = -33.8670522;
		double lng = 151.1957362;
		// create LatLng
		LatLng lastLatLng = new LatLng(lat, lng);

		// remove any existing marker
		if (userMarker != null)
			userMarker.remove();
		// create and set marker properties
		userMarker = googleMap.addMarker(new MarkerOptions()
				.position(lastLatLng).title("You are here")
				.icon(BitmapDescriptorFactory.fromResource(userIcon))
				.snippet("Your last recorded location"));
		// move to location
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(lat, lng)).zoom(20).build();
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
					+ URLEncoder.encode("5000", "UTF-8")
					+ "&sensor="
					+ URLEncoder.encode("true", "UTF-8")
					+ "&types="
					+ URLEncoder.encode("food|bar|church|museum|art_gallery",
							"UTF-8")
					+ "&key="
					+ URLEncoder.encode(
							"AIzaSyDkkR-XhiW9uVPhvM_T5GFrqrC-aeXas4U", "UTF-8");
			new GetPlaces().execute(url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000,
				100, this);
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

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000,
				100, this);
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
}
