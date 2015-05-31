package com.orbital.cityguide;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		addListenerOnButton();
	}

	public void addListenerOnButton() {

		register = (Button) findViewById(R.id.register);
		register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent nextActivity = new Intent(Login.this,
						Register.class);
				startActivity(nextActivity);
			}
		});

		login = (Button) findViewById(R.id.login);
		userName = (EditText) findViewById(R.id.username);
		pw = (EditText) findViewById(R.id.password);

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String username = userName.getText().toString();
				String password = pw.getText().toString();
				
				if (username.matches("") || password.matches("")) {
					title = "Error Message";
					alertboxmsg = "Please fill in all information needed.";
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
								Intent nextActivity = new Intent(Login.this,
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
		AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
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

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

}
