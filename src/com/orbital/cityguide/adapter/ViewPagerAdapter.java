package com.orbital.cityguide.adapter;

import java.util.ArrayList;
import java.util.List;

import com.orbital.cityguide.PackageFragment;
import com.orbital.cityguide.WeatherFragment;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
 
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
 
 
    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
 
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
 
    }
 
    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
 
        if(position == 0) // if the position is 0 we are returning the First tab
        {
            Fragment tab1 = new WeatherFragment();
            return tab1;
        }
        else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
        	Fragment tab2 = new PackageFragment();
            return tab2;
        }
 
 
    }
 
    // This method return the titles for the Tabs in the Tab Strip
 
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }
 
    // This method return the Number of tabs for the tabs Strip
 
    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}
