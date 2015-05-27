package com.orbital.cityguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {

	Button register;
	Button clickHere;
	Button login;

	EditText Username;
	EditText Password;

	String title, alertboxmsg;

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
		Username = (EditText) findViewById(R.id.username);
		Password = (EditText) findViewById(R.id.password);

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				title = "Message";
				alertboxmsg = "Login successfully!";
				popupMessage(title, alertboxmsg);

				// go to eventList.java
				Intent nextActivity = new Intent(Login.this,
						AfterLoginNavigationList.class);
				// Bundle extras = new Bundle();
				// extras.putString("profile_username", username);
				// nextActivity.putExtras(extras);
				startActivity(nextActivity);
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
