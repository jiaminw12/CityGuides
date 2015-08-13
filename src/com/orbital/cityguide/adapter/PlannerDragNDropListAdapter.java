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
		public final String id;
		public final String text;

		public Item(String id, String text) {
			this.id = id;
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
			final TextView mID = (TextView) view.findViewById(R.id.attrID);
			mID.setText(item.id);
			final TextView mTitle = (TextView) view.findViewById(R.id.name);
			mTitle.setText(item.text);
			
			Spinner childSpinner = (Spinner) rl.findViewById(R.id.child_spinner);
			num_Child = Integer.parseInt(childSpinner.getSelectedItem().toString());
			Spinner adultSpinner = (Spinner) rl.findViewById(R.id.adult_spinner);
			num_Adult = Integer.parseInt(adultSpinner.getSelectedItem().toString());
			
			mSinglePrice = (TextView) view.findViewById(R.id.singlePrice);
			try {
				dbAdaptor.open();
				cursor = dbAdaptor.getAllPlanner();
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					do {
						String mAttrTitle = cursor.getString(2);
						if (mAttrTitle.equalsIgnoreCase(mTitle.getText().toString())){
							mPriceAdult = Float.valueOf(cursor.getString(3));
							mPriceChild = Float.valueOf(cursor.getString(4));
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
			
			DecimalFormat df = new DecimalFormat("#0.00");
			df.setMaximumFractionDigits(2);
			totalSinglePrice = (mPriceAdult * num_Adult)+(mPriceChild * num_Child);
			mSinglePrice.setText("$" + df.format(totalSinglePrice));
			
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
