package com.orbital.cityguide;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devsmart.android.ui.HorizontalListView;
import com.orbital.cityguide.adapter.WeatherForecastListAdapter;
import com.orbital.cityguide.model.WeatherForecastItem;

public class HomeFragment extends Fragment {

	String title, alertboxmsg;
	String name_profile;

	Typeface weatherFont;

	TextView cityField;
	TextView updatedField;
	TextView detailsField;
	TextView currentTemperatureField;
	TextView weatherIcon;

	HorizontalListView mlistview;
	WeatherForecastListAdapter weatherForecastListAdapter;
	ArrayList<WeatherForecastItem> weatherForecastItem = null;

	Handler handler;

	public HomeFragment() {
		handler = new Handler();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_home, container,
				false);
		cityField = (TextView) rootView.findViewById(R.id.city_field);
		updatedField = (TextView) rootView.findViewById(R.id.updated_field);
		detailsField = (TextView) rootView.findViewById(R.id.details_field);
		currentTemperatureField = (TextView) rootView
				.findViewById(R.id.current_temperature_field);
		weatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);
		mlistview = (HorizontalListView) rootView.findViewById(R.id.listView);
		weatherIcon.setTypeface(weatherFont);
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		weatherFont = Typeface.createFromAsset(getActivity().getAssets(),
				"weather.ttf");
		updateWeatherData(new CityPreference(getActivity()).getCity());
	}

	@Override
	public void onResume() {
		super.onResume();
		updateWeatherData(new CityPreference(getActivity()).getCity());
	}

	private void updateWeatherData(final String city) {
		new Thread() {
			public void run() {
				final JSONObject json = RemoteFetch
						.getJSON(getActivity(), city);
				final JSONObject jsonDaily = RemoteFetch.getDailyJSON(
						getActivity(), city);
				if (json == null && jsonDaily == null) {
					handler.post(new Runnable() {
						public void run() {
							Toast.makeText(
									getActivity(),
									getActivity().getString(
											R.string.place_not_found),
									Toast.LENGTH_LONG).show();
						}
					});
				} else {
					handler.post(new Runnable() {
						public void run() {
							renderWeather(json);
							renderWeatherDaily(jsonDaily);
						}
					});
				}
			}
		}.start();
	}

	private void renderWeather(JSONObject json) {
		try {
			cityField.setText(json.getString("name").toUpperCase(Locale.US)
					+ ", " + json.getJSONObject("sys").getString("country"));

			JSONObject details = json.getJSONArray("weather").getJSONObject(0);
			JSONObject main = json.getJSONObject("main");
			detailsField
					.setText(details.getString("description").toUpperCase(
							Locale.US)
							+ "\n"
							+ "Humidity: "
							+ main.getString("humidity")
							+ "%"
							+ "\n"
							+ "Pressure: "
							+ main.getString("pressure") + " hPa");

			currentTemperatureField.setText(String.format("%.1f",
					main.getDouble("temp"))
					+ "¡æ");

			DateFormat df = DateFormat.getDateTimeInstance();
			String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
			updatedField.setText("Last update: " + updatedOn);

			setWeatherIcon(details.getInt("id"), json.getJSONObject("sys")
					.getLong("sunrise") * 1000, json.getJSONObject("sys")
					.getLong("sunset") * 1000);

		} catch (Exception e) {
			Log.e("Weather", "One or more fields not found in the JSON data");
		}
	}

	private void renderWeatherDaily(JSONObject json) {
		try {

			mlistview.getLayoutParams().width = 1049;

			weatherForecastItem = new ArrayList<WeatherForecastItem>();

			// looping through All Attractions
			for (int i = 0; i < 7; i++) {
				JSONObject list = json.getJSONArray("list").getJSONObject(i);

				JSONObject mWeather = list.getJSONArray("weather")
						.getJSONObject(0);
				JSONObject mTemp = list.getJSONObject("temp");

				WeatherForecastItem weatherItem = new WeatherForecastItem();

				DateFormatSymbols symbols = new DateFormatSymbols(new Locale(
						"en"));
				String[] dayNames = symbols.getInstance().getShortWeekdays();
				weatherItem.setDate(dayNames[i + 1]);

				weatherItem
						.setIconString(setWeatherIcon(mWeather.getInt("id")));
				weatherItem.setTemperature(String.format("%.1f",
						mTemp.getDouble("day")));

				weatherForecastItem.add(weatherItem);
			}

			weatherForecastListAdapter = new WeatherForecastListAdapter(
					getActivity().getApplicationContext(),
					R.layout.list_item_weather, weatherForecastItem);

			mlistview.setAdapter(weatherForecastListAdapter);

		} catch (Exception e) {
			Log.e("Weather 2", "One or more fields not found in the JSON data");
		}
	}

	private void setWeatherIcon(int actualId, long sunrise, long sunset) {
		int id = actualId / 100;
		String icon = "";
		if (actualId == 800) {
			long currentTime = new Date().getTime();
			if (currentTime >= sunrise && currentTime < sunset) {
				icon = getActivity().getString(R.string.weather_sunny);
			} else {
				icon = getActivity().getString(R.string.weather_clear_night);
			}
		} else {
			switch (id) {
			case 2:
				icon = getActivity().getString(R.string.weather_thunder);
				break;
			case 3:
				icon = getActivity().getString(R.string.weather_drizzle);
				break;
			case 7:
				icon = getActivity().getString(R.string.weather_foggy);
				break;
			case 8:
				icon = getActivity().getString(R.string.weather_cloudy);
				break;
			case 6:
				icon = getActivity().getString(R.string.weather_snowy);
				break;
			case 5:
				icon = getActivity().getString(R.string.weather_rainy);
				break;
			}
		}
		weatherIcon.setText(icon);
	}

	private String setWeatherIcon(int actualId) {
		int id = actualId / 100;
		String icon = "";
		if (actualId == 800) {
			Calendar now = Calendar.getInstance();
			int a = now.get(Calendar.AM_PM);
			if (a == Calendar.AM) {
				icon = getActivity().getString(R.string.weather_sunny);
			} else {
				icon = getActivity().getString(R.string.weather_clear_night);
			}
		} else {
			switch (id) {
			case 2:
				icon = getActivity().getString(R.string.weather_thunder);
				break;
			case 3:
				icon = getActivity().getString(R.string.weather_drizzle);
				break;
			case 7:
				icon = getActivity().getString(R.string.weather_foggy);
				break;
			case 8:
				icon = getActivity().getString(R.string.weather_cloudy);
				break;
			case 6:
				icon = getActivity().getString(R.string.weather_snowy);
				break;
			case 5:
				icon = getActivity().getString(R.string.weather_rainy);
				break;
			}
		}
		return icon;
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
