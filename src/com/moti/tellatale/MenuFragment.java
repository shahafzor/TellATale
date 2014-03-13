package com.moti.tellatale;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MenuFragment extends Fragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
		SharedPreferences SharedPref = ((MainActivity)getActivity()).getSharedPref();
		
		int permission = SharedPref.getInt(getString(R.string.pref_key_user_permission), 0);
		if (permission < 1)
		{
			rootView.findViewById(R.id.button_new_story).setVisibility(View.GONE);
		}
		
		return rootView;
	}
}
