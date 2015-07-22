package com.orbital.cityguide.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.orbital.cityguide.JSONParser;
import com.orbital.cityguide.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.StrictMode;
import android.renderscript.Sampler.Value;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlannerDragNDropListAdapter extends BaseAdapter {

	JSONParser jParser = new JSONParser();
	private static final String RETRIEVEID_URL = "http://192.168.1.5/City_Guide/getAttractionIDByTitle.php";
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_AID = "attr_id";
	private static final String TAG_ATTRACTION = "attractions";
	private JSONArray mAttrID = null;
	int success;

	DBAdapter dbAdaptor;
	Cursor cursor = null;
	Item item;
	ViewGroup mParent;
	String mID;
	final int INVALID_ID = -1;

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

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		SwipeMenuListView mListView = (SwipeMenuListView) parent
				.findViewById(android.R.id.list);
		
		dbAdaptor = new DBAdapter(parent.getContext());
		View view = convertView;
		mParent = parent;

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

			SwipeMenuCreator creator = new SwipeMenuCreator() {

				@Override
				public void create(SwipeMenu menu) {
					switch (menu.getViewType()) {
					case 0:
						// create menu of type 0
						// create "edit" item
						SwipeMenuItem openItem = new SwipeMenuItem(parent.getContext()
								.getApplicationContext());
						// set item background
						openItem.setBackground(new ColorDrawable(Color.rgb(0xC9,
								0xC9, 0xCE)));
						// set item width
						openItem.setWidth(dp2px(100));
						// set item title
						openItem.setTitle("Edit");
						// set item title fontsize
						openItem.setTitleSize(18);
						// set item title font color
						openItem.setTitleColor(Color.WHITE);
						// add to menu
						menu.addMenuItem(openItem);

						// create "delete" item
						SwipeMenuItem deleteItem = new SwipeMenuItem(parent.getContext()
								.getApplicationContext());
						// set item background
						deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
								0x3F, 0x25)));
						// set item width
						deleteItem.setWidth(dp2px(100));
						// set a icon
						deleteItem.setIcon(R.drawable.trash);
						// add to menu
						menu.addMenuItem(deleteItem);
						break;
					case 1:
						// section
						// create menu of type 1
						break;
					}
				}
			};
			
			// set creator
			mListView.setMenuCreator(creator);
			mListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(int position, SwipeMenu menu,
						int index) {
					switch (index) {
					case 0:
						// open
						Toast.makeText(parent.getContext(), "Open", Toast.LENGTH_SHORT)
								.show();
						break;
					case 1:
						// delete
						Toast.makeText(parent.getContext(), "Delete", Toast.LENGTH_SHORT)
								.show();
						// mAppList.remove(position);
						// mAdapter.notifyDataSetChanged();
						break;
					}
					// false : close the menu; true : not close
					// the menu
					return false;
				}
			});

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

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				mParent.getResources().getDisplayMetrics());
	}

}
