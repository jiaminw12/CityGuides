package com.orbital.cityguide.adapter;

import com.orbital.cityguide.R;
import com.orbital.cityguide.model.WeatherForecastItem;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherForecastListAdapter extends ArrayAdapter<WeatherForecastItem> {

	private Context context;
	private ArrayList<WeatherForecastItem> weatherForecastItem;
	int resource;
	int count;
	
	Typeface weatherFont;
	
	WeatherForecastItem wf = new WeatherForecastItem();

	public WeatherForecastListAdapter(Context context, int resource,
			ArrayList<WeatherForecastItem> weatherForecastItem) {
		super(context, resource, weatherForecastItem);
		this.context = context;
		this.resource = resource;
		this.weatherForecastItem = weatherForecastItem;
		this.count = weatherForecastItem.size();
		weatherFont = Typeface.createFromAsset(context.getAssets(),"weather.ttf");
	}
	
	public void updateResults(ArrayList<WeatherForecastItem> commentItem) {
		this.weatherForecastItem = weatherForecastItem;
        //Triggers the list update
        notifyDataSetChanged();
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.list_item_weather, null);
		}

		TextView mDate= (TextView) convertView.findViewById(R.id.date);
		TextView mIcon = (TextView) convertView.findViewById(R.id.weather_icon);
		TextView mTemp = (TextView) convertView.findViewById(R.id.temperature_field);

		mDate.setText(weatherForecastItem.get(position).getDate());
		mIcon.setTypeface(weatherFont);
		mIcon.setText(weatherForecastItem.get(position).getIconString());
		mTemp.setText(weatherForecastItem.get(position).getTemperature() + "¡æ");
		
		return convertView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

/*	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return commentItem.get(position);
	}*/

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

}
