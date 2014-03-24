package com.moti.tellatale;

//import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MenuFragment extends Fragment
{
	private static final String ButtonMyStoriesKey = "my stories";
	private static final String ButtonNewStoryKey = "new story";
	private static final String ButtonGetStoryKey = "get story";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
		if (savedInstanceState != null)
		{
			Button b = (Button)rootView.findViewById(R.id.button_my_stories);
			b.setEnabled(savedInstanceState.getBoolean(ButtonMyStoriesKey));
			b = (Button)rootView.findViewById(R.id.button_new_story);
			b.setEnabled(savedInstanceState.getBoolean(ButtonNewStoryKey));
			b = (Button)rootView.findViewById(R.id.button_get_story);
			b.setEnabled(savedInstanceState.getBoolean(ButtonGetStoryKey));
		}
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		SharedPreferences SharedPref = ((MainActivity)getActivity()).getSharedPref();

		int permission = SharedPref.getInt(getString(R.string.pref_key_user_permission), 0);
		if (permission < 1)
		{
			getView().findViewById(R.id.button_new_story).setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		Button b = (Button) getView().findViewById(R.id.button_my_stories);
		savedInstanceState.putBoolean(ButtonMyStoriesKey, b.isEnabled());
		b = (Button) getView().findViewById(R.id.button_new_story);
		savedInstanceState.putBoolean(ButtonNewStoryKey, b.isEnabled());
		b = (Button) getView().findViewById(R.id.button_get_story);
		savedInstanceState.putBoolean(ButtonGetStoryKey, b.isEnabled());
	
		super.onSaveInstanceState(savedInstanceState);
	}
}
