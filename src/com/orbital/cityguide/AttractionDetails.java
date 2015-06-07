package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class AttractionDetails extends Activity {

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
	private GoogleMap googleMap;
	// Progress Dialog
	private ProgressDialog pDialog;

	String attr_id, title, alertboxmsg;
	int success;
	
	static final LatLng TutorialsPoint = new LatLng(21 , 57);

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
		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();

		try {
			MapsInitializer.initialize(getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap = mMapView.getMap();

		new GetAttrDetails().execute();
	}

	class GetAttrDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AttractionDetails.this);
			pDialog.setMessage("Loading attraction details. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**Getting product details in background thread**/
		protected String doInBackground(String... params) {

			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {

					try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("attr_id", attr_id));

						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.permitAll().build();
						StrictMode.setThreadPolicy(policy);

						JSONObject json = jsonParser.makeHttpRequest(
								READATTR_URL, "POST", params);
						if (json != null) {
							success = json.getInt(TAG_SUCCESS);
							if (success == 1) {
								mAttractions = json
										.getJSONArray(TAG_ATTRACTION);

								// looping through All Attractions
								for (int i = 0; i < mAttractions.length(); i++) {
									JSONObject c = mAttractions
											.getJSONObject(i);

									// Storing each json item in variable
									String id = c.getString(TAG_AID);
									String title = c.getString(TAG_TITLE);
									String desc = c.getString(TAG_DESC);
									String addr = c.getString(TAG_ADDR);
									String mlat = c.getString(TAG_LAT);
									String mlong = c.getString(TAG_LONG);
									String openHrs = c.getString(TAG_OHRS);
									String img = c.getString(TAG_IMG);
									String link = c.getString(TAG_LINK);

									byte[] image = Base64.decode(img,
											Base64.DEFAULT);
									Bitmap bitmap = BitmapFactory
											.decodeByteArray(image, 0,
													image.length);
									mImageView.setImageBitmap(bitmap);

									mTitle.setText(title);
									mDetail.setText(desc);
									mOpenHrs.setText(openHrs);
									mLink.setText(link);
									mLink.setMovementMethod(LinkMovementMethod
											.getInstance());

									setMap(mlat, mlong, title, addr);
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
			});
			return null;
		}

		/**After completing background task Dismiss the progress dialog**/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once got all details
			pDialog.dismiss();
		}
	}

	public void setMap(String mLatitude, String mLongtitude, String mTitle,
			String mAddress) {
		double latitude = Double.parseDouble(mLatitude);
		double longitude = Double.parseDouble(mLongtitude);
		// create marker
		MarkerOptions marker = new MarkerOptions()
				.position(new LatLng(latitude, longitude)).title(mTitle)
				.snippet(mAddress);
		googleMap.addMarker(marker);
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(latitude, longitude)).zoom(12).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

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