package com.orbital.cityguide;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment_AfterLogin extends Fragment {

	SectionsPagerAdapter mSectionsPagerAdapter;

	public static final String TAG = HomeFragment_AfterLogin.class
			.getSimpleName();

	ViewPager mViewPager;
	static String name_profile;

	public static HomeFragment_AfterLogin newInstance(String nameProfile) {
		HomeFragment_AfterLogin myFragment = new HomeFragment_AfterLogin();
		Bundle args = new Bundle();
		args.putString("profile_username", nameProfile);
		myFragment.setArguments(args);
		name_profile = nameProfile;
		return myFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_item_one, container, false);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getChildFragmentManager());

		mViewPager = (ViewPager) v.findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		return v;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new HistoryFragment();
				Bundle args = new Bundle();
				args.putString("profile_username", name_profile);
				fragment.setArguments(args);
				break;
			case 1:
				fragment = new PackageFragment();
				break;
			case 2:
				fragment = new WeatherFragment();
				break;
			}
			return fragment;

		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section3).toUpperCase(l);
			case 2:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

}
