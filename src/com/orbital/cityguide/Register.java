package com.orbital.cityguide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class Register extends FragmentActivity implements OnDateSetListener {

	static int LOAD_IMAGE = 1;
	static int LOAD_IMAGE_KITKAT = 2;
	static int TAKE_PICTURE = 3;

	Uri outputFileUri;

	static final String appDirectoryName = "City Guides Images";
	static final File imageRoot = new File(
			Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
			appDirectoryName);

	private static final String REGISTER_URL = "http://192.168.1.7/City_Guide/registration.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	// declare variables
	EditText userName;
	EditText pw;
	EditText confirmPassword;
	EditText emailAddr;
	TextView birthDate;
	ImageButton pickdate;
	Spinner Gender = null;
	ImageView imageView;

	Bitmap bmp;

	Button cancel;
	Button submit;
	Button takePic = null;

	int yy, mm, dd, success;
	String title, alertboxmsg;
	protected ArrayAdapter<CharSequence> genderAdapter;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		addListenerOnButton();

		birthDate = (TextView) findViewById(R.id.age);
		final Calendar c = Calendar.getInstance();
		yy = c.get(Calendar.YEAR);
		mm = c.get(Calendar.MONTH);
		dd = c.get(Calendar.DAY_OF_MONTH);

		updateDisplay();

		pickdate = (ImageButton) findViewById(R.id.pickDate);
		/** Listener for click event of the button */
		pickdate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DatePickerFragment newFragment = new DatePickerFragment();
				String choseDate = birthDate.getText().toString();
				Bundle args = new Bundle();
				args.putString("dateText", choseDate);
				newFragment.setArguments(args);
				newFragment.show(getSupportFragmentManager(), "datePicker");
			}
		});

		Gender = (Spinner) findViewById(R.id.gender);
		this.genderAdapter = ArrayAdapter.createFromResource(this,
				R.array.genderArray, android.R.layout.simple_spinner_item);
		Gender.setAdapter(this.genderAdapter);

		submitButton();
	}

	public void submitButton() {
		userName = (EditText) findViewById(R.id.username);
		pw = (EditText) findViewById(R.id.password);
		confirmPassword = (EditText) findViewById(R.id.confirmpw);
		emailAddr = (EditText) findViewById(R.id.email);
		birthDate = (TextView) findViewById(R.id.age);
		Gender = (Spinner) findViewById(R.id.gender);
		imageView = (ImageView) findViewById(R.id.imgView);

		submit = (Button) findViewById(R.id.submit);

		submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String username = userName.getText().toString();
				String password = pw.getText().toString();
				String confirmPwd = confirmPassword.getText().toString();
				String date = birthDate.getText().toString();
				String gender = Gender.getSelectedItem().toString();
				String emailAddress = emailAddr.getText().toString();
				String image = "";

				if (username.matches("") || password.matches("")
						|| date.matches("") || gender.matches("")
						|| emailAddress.matches("")) {
					title = "Error Message";
					alertboxmsg = "Required field(s) is missing.";
					popupMessage(title, alertboxmsg);
				} else if (password.matches(confirmPwd)) {
					boolean hasDrawable = (imageView.getDrawable() != null);
					if (hasDrawable) {
						Bitmap bitmap = ((BitmapDrawable) imageView
								.getDrawable()).getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] imageFile = stream.toByteArray();
						image = Base64
								.encodeToString(imageFile, Base64.DEFAULT);
					}

					try {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("username", username));
						params.add(new BasicNameValuePair("emailAddress",
								emailAddress));
						params.add(new BasicNameValuePair("password", password));
						params.add(new BasicNameValuePair("date", date));
						params.add(new BasicNameValuePair("image", image));
						params.add(new BasicNameValuePair("gender", gender));

						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.permitAll().build();
						StrictMode.setThreadPolicy(policy);

						JSONObject json = jsonParser.makeHttpRequest(REGISTER_URL,
								"POST", params);
						if (json != null) {
							success = json.getInt(TAG_SUCCESS);
							if (success == 1) {
								title = "Message";
								alertboxmsg = "User created!";
								popupMessage(title, alertboxmsg);
								Intent LoginFragment = new Intent();
								startActivity(LoginFragment);
							} else if (success == 0) {
								title = "Message";
								alertboxmsg = json.getString(TAG_MESSAGE);
								popupMessage(title, alertboxmsg);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					title = "Error Message";
					alertboxmsg = "Password incorrect.";
					popupMessage(title, alertboxmsg);

				}
			}
		});
	}

	public void addListenerOnButton() {

		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		Button Browse = (Button) findViewById(R.id.browse);
		Browse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
				photoPickerIntent.setType("image/*");
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
					startActivityForResult(photoPickerIntent, LOAD_IMAGE_KITKAT);
				} else {
					startActivityForResult(photoPickerIntent, LOAD_IMAGE);
				}
			}
		});

		takePic = (Button) findViewById(R.id.takePicture);
		takePic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				File file = new File(Environment.getExternalStorageDirectory(),
						"MyPhoto.jpg");
				outputFileUri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

				startActivityForResult(intent, TAKE_PICTURE);
			}
		});
	}

	/*---------------------- IMAGE ------------------------------*/
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			// LOAD_IMAGE
			if (requestCode == 1 && null != data) {

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				Log.e("path", picturePath); // use selectedImagePath

				imageView = (ImageView) findViewById(R.id.imgView);
				bmp = BitmapFactory.decodeFile(picturePath);
				bmp = Shrink(picturePath, 100, 200);
				imageView.setImageBitmap(bmp);

			} else if (requestCode == 2) {
				// LOAD_IMAGE_KITKAT
				Uri selectedImage = data.getData();
				String id = selectedImage.getLastPathSegment().split(":")[1];
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				final String imageOrderBy = null;

				Uri uri = getUri();
				String selectedImagePath = "path";

				Cursor imageCursor = getContentResolver().query(uri,
						filePathColumn, MediaStore.Images.Media._ID + "=" + id,
						null, imageOrderBy);

				if (imageCursor.moveToFirst()) {
					selectedImagePath = imageCursor.getString(imageCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
				}
				Log.e("path", selectedImagePath); // use selectedImagePath

				imageView = (ImageView) findViewById(R.id.imgView);
				bmp = BitmapFactory.decodeFile(selectedImagePath);
				bmp = Shrink(selectedImagePath, 100, 200);
				imageView.setImageBitmap(bmp);

			} else if (requestCode == 3) {
				// TAKE_PICTURE
				imageView = (ImageView) findViewById(R.id.imgView);
				BitmapDrawable bmpd = new BitmapDrawable(getResources(),
						outputFileUri.getPath());
				imageView.setImageDrawable(bmpd);
			}
		}
	}

	// By using this method get the Uri of Internal/External Storage for Media
	private Uri getUri() {
		String state = Environment.getExternalStorageState();
		if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
			return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

		return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	}

	public static Bitmap Shrink(String file, int width, int height) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, options);
		options.inSampleSize = calcSize(options, width, height);
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(file, options);
		return bmp;
	}

	public static int calcSize(BitmapFactory.Options options, int width,
			int height) {
		final int uHeight = options.outHeight;
		final int uWidth = options.outWidth;
		int inSampleSize = 1;
		if (uHeight > height || uWidth > width) {
			if (uWidth > uHeight) {
				inSampleSize = Math.round((float) uHeight / (float) height);
			} else {
				inSampleSize = Math.round((float) uWidth / (float) width);
			}
		}
		return inSampleSize;
	}

	/*---------------------- DATEPICKER ------------------------------*/
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		yy = year;
		mm = month;
		dd = day;
		updateDisplay();
	}

	// Updates the date in the TextView
	private void updateDisplay() {
		birthDate.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(yy).append("-")
				.append(mm < 9 ? ("0" + (mm + 1)) : mm + 1).append("-")
				.append(dd < 10 ? "0" + dd : dd));
	}

	public void popupMessage(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
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
