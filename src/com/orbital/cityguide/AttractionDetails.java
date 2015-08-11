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
import com.orbital.cityguide.adapter.CommentListAdapter;
import com.orbital.cityguide.adapter.DBAdapter;
import com.orbital.cityguide.model.CommentItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RatingBar.*;

public class AttractionDetails extends Activity {
	
	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();

	private static final String READATTR_URL = "http://" + ipadress +"/getAttraction.php";
	private static final String ADDCOM_URL = "http://" + ipadress +"/addComment.php";
	private static final String READCOM_URL = "http://" + ipadress +"/getComment.php";

	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	private static final String TAG_ATTRACTION = "attractions";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_TITLE = "attr_title";
	private static final String TAG_DESC = "attr_description";
	private static final String TAG_ADDR = "address";
	private static final String TAG_LAT = "latitude";
	private static final String TAG_LONG = "longitude";
	private static final String TAG_OHRS = "opening_hrs";
	private static final String TAG_CID = "category_id";
	private static final String TAG_PADULT = "price_adult";
	private static final String TAG_PCHILD = "price_child";
	private static final String TAG_IMG = "attr_image";
	private static final String TAG_LINK = "attr_link";
	private static final String TAG_APOI = "attr_POI";

	private static final String TAG_COMMENT = "comments";
	private static final String TAG_CT = "comment_title";
	private static final String TAG_TEXT = "comment_text";
	private static final String TAG_RATE = "rating";
	private static final String TAG_DATE = "date_created";
	private static final String TAG_USR_IMG = "image";
	private static final String TAG_USR_NAME = "username";

	// An array of all of our attractions
	private JSONArray mAttractions = null;

	// An array of all of comments
	private JSONArray mComments = null;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	ImageView mImageView;
	Button mPlanner;
	TextView mTitle;
	TextView mDetail;
	TextView mLink;
	TextView mOpenHrs;
	TextView mAdultPrice;
	TextView mChildPrice;
	MapView mMapView;
	TextView mReview;
	ListView mList;
	private GoogleMap googleMap;
	// Progress Dialog
	private ProgressDialog pDialog;

	Button mComment;
	EditText mCommentTitle;
	EditText mCommentText;
	RatingBar mRatingbar;

	String mAttr_id, title, alertboxmsg;
	String name_profile;
	int success;
	String url;
	Bundle bundle;

	ArrayList<CommentItem> commentItem = null;
	CommentListAdapter commentListAdapter;
	
	DBAdapter dbAdaptor;
	Cursor cursor = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attr_details);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		mAttr_id = extras.getString("AID");
		name_profile = extras.getString("profile_username");

		mImageView = (ImageView) findViewById(R.id.imageViewId);
		mTitle = (TextView) findViewById(R.id.title_Attr);
		mPlanner = (Button) findViewById(R.id.btnPlanner);
		setmPlannerText();
		mPlanner.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updatePlanner();
			}
		});
		mDetail = (TextView) findViewById(R.id.details);
		mLink = (TextView) findViewById(R.id.weblink);
		mOpenHrs = (TextView) findViewById(R.id.open_hrs);
		mAdultPrice = (TextView) findViewById(R.id.adult_price);
		mChildPrice = (TextView) findViewById(R.id.child_price);
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);
		mMapView.onResume();

		try {
			MapsInitializer.initialize(getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		googleMap = mMapView.getMap();

		mRatingbar = (RatingBar) findViewById(R.id.ratingBar);
		mComment = (Button) findViewById(R.id.addComment);
		mComment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (name_profile.equals("null")) {
					title = "Error Message";
					alertboxmsg = "Please login!";
					popupMessage(title, alertboxmsg);
				} else {
					commentButton();
				}
			}
		});

		mReview = (TextView) findViewById(R.id.title_review);

		new GetAttrDetails().execute();
	}
	
	public void setmPlannerText() {
		dbAdaptor = new DBAdapter(getApplicationContext());
		try {
			dbAdaptor.open();
			cursor = dbAdaptor.getAttrID();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String mAttrID = cursor.getString(0);
					if (mAttr_id.equals(mAttrID)) {
						mPlanner.setText("-");
						break;
					} else {
						mPlanner.setText("+");
					}
				} while (cursor.moveToNext());
			} else {
			}
		} catch (Exception e) {
			Log.d("City Guide", e.getMessage());
		} finally {
			if (cursor != null)
				cursor.close();

			if (dbAdaptor != null)
				dbAdaptor.close();
		}
	}
	
	public void updatePlanner(){
		dbAdaptor = new DBAdapter(getApplicationContext());
		if (mPlanner.getText().toString().equalsIgnoreCase("+")) {
			mPlanner.setText("-");

			try {
				dbAdaptor.open();
				dbAdaptor.insertPlannerList(mAttr_id, "1");
				Toast.makeText(getApplicationContext(),
						"Successfully Added!!! Attraction Details", Toast.LENGTH_SHORT)
						.show();
			} catch (Exception e) {
				Log.e("CityGuideSingapore", e.getMessage());
			} finally {
				if (dbAdaptor != null) {
					dbAdaptor.close();
				}
			}
		} else if (mPlanner.getText().toString().equalsIgnoreCase("-")) {
			mPlanner.setText("+");

			dbAdaptor = new DBAdapter(getApplicationContext());
			try {
				dbAdaptor.open();
				dbAdaptor.deletePlannerItem(mAttr_id);
				Toast.makeText(getApplicationContext(),
						"Successfully Removed!!! Attraction Details",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.d("CityGuideSingapore", e.getMessage());
			} finally {
				if (dbAdaptor != null) {
					dbAdaptor.close();
				}
			}
		}
	}
	
	public void commentButton() {

		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.insert_comment, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setView(promptsView);

		mCommentTitle = (EditText) promptsView.findViewById(R.id.mainTitle);
		mCommentText = (EditText) promptsView.findViewById(R.id.comment_box);
		mRatingbar = (RatingBar) promptsView.findViewById(R.id.ratingBar);
		mRatingbar
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
					}
				});

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// get user input and set it to result
						String attr_id = mAttr_id;
						String comment_title = mCommentTitle.getText()
								.toString();
						String comment_text = mCommentText.getText().toString();
						String rating = String.valueOf(mRatingbar.getRating());
						String username = name_profile;

						if (comment_title.matches("")
								|| comment_text.matches("")
								|| attr_id.matches("")) {
							title = "Error Message";
							alertboxmsg = "Required field(s) is missing.";
							popupMessage(title, alertboxmsg);
						} else {

							try {
								List<NameValuePair> params = new ArrayList<NameValuePair>();
								params.add(new BasicNameValuePair("attr_id",
										attr_id));
								params.add(new BasicNameValuePair(
										"comment_title", comment_title));
								params.add(new BasicNameValuePair(
										"comment_text", comment_text));
								params.add(new BasicNameValuePair("rating",
										rating));
								params.add(new BasicNameValuePair("username",
										username));

								StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
										.permitAll().build();
								StrictMode.setThreadPolicy(policy);

								JSONObject json = jsonParser.makeHttpRequest(
										ADDCOM_URL, "POST", params);
								if (json != null) {
									success = json.getInt(TAG_SUCCESS);
									if (success == 1) {
										title = "Message";
										alertboxmsg = "Successfully submit comment.";
										popupMessage(title, alertboxmsg);
										finish();
										startActivity(getIntent());
									} else if (success == 0) {
										title = "Message";
										alertboxmsg = json
												.getString(TAG_MESSAGE);
										popupMessage(title, alertboxmsg);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	class GetAttrDetails extends AsyncTask<String, String, String> {

		/* Before starting background thread Show Progress Dialog */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AttractionDetails.this);
			pDialog.setMessage("Loading attraction details. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/** Getting product details in background thread **/
		protected String doInBackground(String... params) {

			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {

					try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("attr_id", mAttr_id));

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
									String adultP = c.getString(TAG_PADULT); 
									String childP = c.getString(TAG_PCHILD);
									String img = c.getString(TAG_IMG);
									final String link = c.getString(TAG_LINK);

									byte[] image = Base64.decode(img,
											Base64.DEFAULT);
									Bitmap bitmap = BitmapFactory
											.decodeByteArray(image, 0,
													image.length);
									mImageView.setImageBitmap(bitmap);

									mTitle.setText(title);
									mDetail.setText(desc);
									mOpenHrs.setText(openHrs);
									mAdultPrice.setText("Price of Adult : " + adultP);
									mChildPrice.setText("Price of Children : " + childP);
									if (!(link.equalsIgnoreCase("null"))) {
										mLink.setText(link);
										mLink.setOnClickListener(new View.OnClickListener() {

											@Override
											public void onClick(View v) {
												Intent nextActivity = new Intent(
														AttractionDetails.this,
														WebViewActivity.class);
												Bundle extras = new Bundle();
												extras.putString("url", link);
												extras.putString("profile_username", name_profile);
												extras.putString("AID", mAttr_id);
												nextActivity.putExtras(extras);
												startActivity(nextActivity);
											}

										});
									}

									setMap(mlat, mlong, title, addr);
								}
							} else if (success == 0) {
								title = "Message";
								alertboxmsg = json.getString("Error!");
								popupMessage(title, alertboxmsg);
							}
						}

						TableRow row = (TableRow) findViewById(R.id.tableRow6);

						JSONObject json_comm = jsonParser.makeHttpRequest(
								READCOM_URL, "POST", params);
						if (json_comm != null) {
							success = json_comm.getInt(TAG_SUCCESS);
							if (success == 1) {
								row.setVisibility(View.VISIBLE);
								mReview.setVisibility(View.VISIBLE);

								mComments = json_comm.getJSONArray(TAG_COMMENT);
								commentItem = new ArrayList<CommentItem>();
								ListView listview = (ListView) findViewById(R.id.list);

								if (mComments.length() < 2) {
									listview.getLayoutParams().height = 500 / 2;
								} else {
									listview.getLayoutParams().height = 500 * mComments
											.length() / 2;
								}

								// looping through All Attractions
								for (int i = 0; i < mComments.length(); i++) {
									JSONObject c = mComments.getJSONObject(i);

									CommentItem comItem = new CommentItem();

									comItem.setUsr_img(c.getString(TAG_USR_IMG));
									comItem.setTitle(c.getString(TAG_CT));
									comItem.setRate(c.getString(TAG_RATE));
									comItem.setUsername(c
											.getString(TAG_USR_NAME));
									comItem.setDescription(c
											.getString(TAG_TEXT));
									comItem.setDate(c.getString(TAG_DATE));

									commentItem.add(comItem);
								}
								commentListAdapter = new CommentListAdapter(
										getApplicationContext(),
										R.layout.list_item_comment, commentItem);

								listview.setAdapter(commentListAdapter);
							} else if (success == 0) {
							}
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			return null;
		}

		/** After completing background task Dismiss the progress dialog **/
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
				.target(new LatLng(latitude, longitude)).zoom(14).build();
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