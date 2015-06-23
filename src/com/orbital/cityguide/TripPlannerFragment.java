package com.orbital.cityguide;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TripPlannerFragment extends Fragment {
	
	String name_profile;
	
	public TripPlannerFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_planner, container, false);
        
        Bundle bundle = this.getArguments();
		name_profile = bundle.getString("profile_username", name_profile);
         
        return rootView;
    }
}
