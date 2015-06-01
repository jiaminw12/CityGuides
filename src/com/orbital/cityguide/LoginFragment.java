package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginFragment extends Fragment {

	Button register;
	Button clickHere;
	Button login;

	EditText userName;
	EditText pw;

	String title, alertboxmsg;
	int success;

	private static final String LOGIN_URL = "http://192.168.1.7/City_Guide/login.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	public LoginFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_login, container,
				false);
		register = (Button) rootView.findViewById(R.id.register);
		login = (Button) rootView.findViewById(R.id.login);
		userName = (EditText) rootView.findViewById(R.id.username);
		pw = (EditText) rootView.findViewById(R.id.password);
		setRetainInstance(true);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	public void init() {

		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent nextActivity = new Intent(getActivity(), Register.class);
				startActivity(nextActivity);
			}
		});

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String username = userName.getText().toString();
				String password = pw.getText().toString();

				if (username.matches("") || password.matches("")) {
					title = "Error Message";
					alertboxmsg = "Required field(s) is missing.";
					popupMessage(title, alertboxmsg);
				} else {
					try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("username", username));
						params.add(new BasicNameValuePair("password", password));

						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.permitAll().build();
						StrictMode.setThreadPolicy(policy);

						JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL,
								"POST", params);
						if (json != null) {
							success = json.getInt(TAG_SUCCESS);
							if (success == 1) {
								title = "Message";
								alertboxmsg = "Login successfully!";
								popupMessage(title, alertboxmsg);

								// go to eventList.java
								Intent nextActivity = new Intent(getActivity(),
										AfterLoginNavigationList.class);
								Bundle extras = new Bundle();
								extras.putString("profile_username", username);
								nextActivity.putExtras(extras);
								startActivity(nextActivity);
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
		});
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
