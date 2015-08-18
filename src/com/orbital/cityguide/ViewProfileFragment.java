package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewProfileFragment extends Fragment {

	public static final String TAG_Name = ViewProfileFragment.class
			.getSimpleName();

	ImageView mProfilePic;
	TextView mUsername;
	TextView mEmail;
	TextView mDate;
	TextView mGender;
	Button mSubmit;

	CallbackManager callbackManager;

	String title, alertboxmsg;
	String name_profile;
	int success;

	static int LOAD_IMAGE = 1;
	static int LOAD_IMAGE_KITKAT = 2;
	static int TAKE_PICTURE = 3;
	
	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();

	private static final String GETUSR_URL = "http://" + ipadress + "/getUser.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_USER = "userprofile";
	private static final String TAG_USERNAME = "username";
	private static final String TAG_EMAIL = "emailAddress";
	private static final String TAG_DATE = "date";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_GENDER = "gender";

	// An array of all of our attractions
	private JSONArray mUsers = null;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	private static final String TAG = "BroadcastTest";
	private Intent intentBroadcast;

	public ViewProfileFragment() {
	}

	public static ViewProfileFragment newInstance(String name_profile) {
		ViewProfileFragment myFragment = new ViewProfileFragment();
		Bundle args = new Bundle();
		args.putString("profile_username", name_profile);
		myFragment.setArguments(args);
		return myFragment;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		View rootView = inflater.inflate(R.layout.fragment_profile, container,
				false);

		Bundle bundle = this.getArguments();
		name_profile = bundle.getString("profile_username");

		mProfilePic = (ImageView) rootView.findViewById(R.id.profilepic);
		mUsername = (TextView) rootView.findViewById(R.id.userName);
		mDate = (TextView) rootView.findViewById(R.id.date);
		mGender = (TextView) rootView.findViewById(R.id.gender);
		mSubmit = (Button) rootView.findViewById(R.id.submit);
		mSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Starting new intent
				Intent in = new Intent(getActivity(), EditProfile.class);
				in.putExtra("profile_username", name_profile);
				startActivity(in);
			}

		});

		setRetainInstance(true);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	@Override
	public void onResume() {
		super.onResume();
		init();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void init() {
		String username = name_profile;

		if (username.matches("")) {
			title = "Error Message";
			alertboxmsg = "Required field(s) is missing.";
			popupMessage(title, alertboxmsg);
		} else {
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", username));

				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);

				JSONObject json = jsonParser.makeHttpRequest(GETUSR_URL,
						"POST", params);
				if (json != null) {
					success = json.getInt(TAG_SUCCESS);
					if (success == 1) {

						mUsers = json.getJSONArray(TAG_USER);

						// looping through All Attractions
						for (int i = 0; i < mUsers.length(); i++) {
							JSONObject c = mUsers.getJSONObject(i);

							// Storing each json item in variable
							String name = c.getString(TAG_USERNAME);
							String email = c.getString(TAG_EMAIL);
							String birthDate = c.getString(TAG_DATE);
							String image_pic = c.getString(TAG_IMAGE);
							String gender = c.getString(TAG_GENDER);

							if (!image_pic.equalsIgnoreCase("null")) {
								byte[] image = Base64.decode(image_pic,
										Base64.DEFAULT);
								Bitmap bitmap = BitmapFactory.decodeByteArray(
										image, 0, image.length);
								mProfilePic.setImageBitmap(bitmap);
							} else {
								mProfilePic.setVisibility(View.GONE);
							}

							mUsername.setText(name);
							mDate.setText(birthDate);
							mGender.setText(gender);
						}
					} else if (success == 0) {
						title = "Message";
						alertboxmsg = json.getString("Error!");
						popupMessage(title, alertboxmsg);

					} else if (success == 0) {
						title = "Message";
						alertboxmsg = "Invalid Credentials!";
						popupMessage(title, alertboxmsg);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void popupMessage(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
