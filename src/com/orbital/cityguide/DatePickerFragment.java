package com.orbital.cityguide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DatePickerFragment extends DialogFragment {

	int yy, mm, dd, year, month, day;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		
		Bundle args = this.getArguments();
		if (args != null){
			String finaldate = args.getString("dateText");
			if (finaldate != null){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date date = df.parse(finaldate);
					c.setTime(date);
					year = c.get(Calendar.YEAR);
					month = c.get(Calendar.MONTH);
					day = c.get(Calendar.DAY_OF_MONTH);				
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		DatePickerDialog dialog = new DatePickerDialog(getActivity(),
				(OnDateSetListener) getActivity(), year, month, day);

		return dialog;
	}
	

}
