package com.orbital.cityguide.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.orbital.cityguide.ConnectToWebServices;
import com.orbital.cityguide.JSONParser;
import com.orbital.cityguide.R;
import com.orbital.cityguide.TripPlannerFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PlannerDragNDropListAdapter extends BaseAdapter {

	JSONParser jParser = new JSONParser();
	
	static ConnectToWebServices mConnect = new ConnectToWebServices();
	static String ipadress = mConnect.GetIPadress();
	
	private static final String RETRIEVEID_URL = "http://" + ipadress + "/City_Guide/getAttractionIDByTitle.php";
	private static final String READATTR_URL = "http://" + ipadress +"/City_Guide/getAttraction.php";
	
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_PADULT = "price_adult";
	private static final String TAG_PCHILD = "price_child";
	private static final String TAG_ATTRACTION = "attractions";
	private JSONArray mAttrID = null;
	int success;

	DBAdapter dbAdaptor;
	Cursor cursor = null;
	Item item;
	ViewGroup mParent;
	String mID;
	float mPriceAdult, mPriceChild, totalSinglePrice, totalPrice=0;
	int num_Adult, num_Child;
	final int INVALID_ID = -1;
	RelativeLayout rl;
	TextView mSinglePrice;
	
	DecimalFormat df;

	public static abstract class Row {
	}

	public static final class Section extends Row {
		public final String text;

		public Section(String text) {
			this.text = text;
		}
	}

	public static final class Item extends Row {
		public final String text;

		public Item(String text) {
			this.text = text;
		}
	}

	private List<Row> rows;

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	@Override
	public int getCount() {
		return rows.size();
	}

	@Override
	public Row getItem(int position) {
		return rows.get(position);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (getItem(position) instanceof Section) {
			return 1;
		} else {
			return 0;
		}
	}

	public String retrieveIdByTitle(String attr_title) {
		String id = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("attr_title", attr_title));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jParser.makeHttpRequest(RETRIEVEID_URL, "POST",
					params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mAttrID = json.getJSONArray(TAG_ATTRACTION);
					JSONObject c = mAttrID.getJSONObject(0);
					id = c.getString(TAG_AID);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return id;
	}

	public float retrievePrice(String attr_id) {
		String id = null;
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("attr_id", attr_id));

			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			JSONObject json = jParser.makeHttpRequest(READATTR_URL, "POST",
					params);
			if (json != null) {
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					mAttrID = json.getJSONArray(TAG_ATTRACTION);
					JSONObject c = mAttrID.getJSONObject(0);
					mPriceAdult = Float.valueOf(c.getString(TAG_PADULT)); 
					mPriceChild =Float.valueOf(c.getString(TAG_PCHILD));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		totalSinglePrice = (mPriceAdult * num_Adult)+(mPriceChild * num_Child);

		return totalSinglePrice;
	}
		
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		
		dbAdaptor = new DBAdapter(parent.getContext());
		View view = convertView;
		rl = (RelativeLayout) parent.getParent();
		df = new DecimalFormat("#0.00");
		df.setMaximumFractionDigits(2);
		
		if (getItemViewType(position) == 0) { // Item
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = (RelativeLayout) inflater.inflate(
						R.layout.list_item_planner, parent, false);
			}

			item = (Item) getItem(position);
			final TextView mTitle = (TextView) view.findViewById(R.id.name);
			mTitle.setText(item.text);
			
			Spinner childSpinner = (Spinner) rl.findViewById(R.id.child_spinner);
			num_Child = Integer.parseInt(childSpinner.getSelectedItem().toString());
			Spinner adultSpinner = (Spinner) rl.findViewById(R.id.adult_spinner);
			num_Adult = Integer.parseInt(adultSpinner.getSelectedItem().toString());
			
			mSinglePrice = (TextView) view.findViewById(R.id.singlePrice);
			mSinglePrice.setText("$" + df.format(retrievePrice(retrieveIdByTitle(item.text))));
			
		} else { // Section
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = (LinearLayout) inflater.inflate(
						R.layout.row_section_search, parent, false);
			}

			Section section = (Section) getItem(position);
			TextView textView = (TextView) view.findViewById(R.id.textView1);
			textView.setText(section.text);
			view.setClickable(false);
			view.setEnabled(false);
			view.setOnClickListener(null);
		}
		
		return view;

	}

	
}
