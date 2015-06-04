package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AttractionDetails extends Activity {

	// manages all of our attractions in a list.
	private ArrayList<HashMap<String, String>> mAttractionsList;

	private static final String READATTR_URL = "http://192.168.1.5/City_Guide/getAttraction.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_ATTRACTION = "attractions";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_TITLE = "attr_title";
	private static final String TAG_DESC = "attr_description";
	private static final String TAG_PADULT = "price_adult";
	private static final String TAG_PCHILD = "price_child";
	private static final String TAG_ADDR = "address";
	private static final String TAG_LAT = "latitude";
	private static final String TAG_LONG = "longitude";
	private static final String TAG_OHRS = "opening_hrs";
	private static final String TAG_CID = "category_id";
	private static final String TAG_IMG = "attr_image";
	private static final String TAG_LINK = "attr_link";
	private static final String TAG_APOI = "attr_POI";

	// An array of all of our attractions
	private JSONArray mAttractions = null;
	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	ImageView mImageView;
	TextView mTitle;
	TextView mDetail;
	TextView mLink;
	TextView mOpenHrs;
	MapView mMapView;
	//private GoogleMap googleMap;

	String attr_id, title, alertboxmsg;
	int success;

	// http://localhost/city_guide/images

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attr_details);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		attr_id = extras.getString("AID");

		mImageView = (ImageView) findViewById(R.id.imageViewId);
		mTitle = (TextView) findViewById(R.id.title_Attr);
		mDetail = (TextView) findViewById(R.id.details);
		mLink = (TextView) findViewById(R.id.weblink);
		mOpenHrs = (TextView) findViewById(R.id.open_hrs);
		mMapView = (MapView) findViewById(R.id.mapView);

		getAttrDetails();
	}

	public void getAttrDetails() {

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("attr_id", attr_id));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jsonParser.makeHttpRequest(READATTR_URL, "POST",
					params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mAttractions = json.getJSONArray(TAG_ATTRACTION);

					// looping through All Attractions
					for (int i = 0; i < mAttractions.length(); i++) {
						JSONObject c = mAttractions.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_AID);
						String title = c.getString(TAG_TITLE);
						String desc = c.getString(TAG_DESC);
						String addr = c.getString(TAG_ADDR);
						String lat = c.getString(TAG_LAT);
						String mlong = c.getString(TAG_LONG);
						String openHrs = c.getString(TAG_OHRS);
						String img = c.getString(TAG_IMG);
						String link = c.getString(TAG_LINK);

						// mImageView.setImageBitmap(BitmapFactory.decodeFile("img"));
						mTitle.setText(title);
						mDetail.setText(desc);
						mLink.setText(link);
						mLink.setMovementMethod(LinkMovementMethod.getInstance());
						
						//setMap(lat, mlong, addr);

					}
				} else if (success == 0) {
					title = "Message";
					alertboxmsg = json.getString("Error!");
					popupMessage(title, alertboxmsg);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setMap(String mLatitude, String mLongtitude, String mAddress) {

		/*mMapView.onResume();// needed to get the map to display immediately

		try {
			MapsInitializer.initialize(this.getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap = mMapView.getMap();
		// latitude and longitude
		double latitude = Double.parseDouble(mLatitude);
		double longitude = Double.parseDouble(mLongtitude);

		// create marker
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(latitude, longitude)).title(mAddress);

		// Changing marker icon
		marker.icon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

		// adding marker
		googleMap.addMarker(marker);
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(1.300661, 103.874389)).zoom(12).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));*/
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void popupMessage(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				AttractionDetails.this);
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