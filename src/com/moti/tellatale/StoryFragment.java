package com.moti.tellatale;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;
//import android.app.Fragment;

public abstract class StoryFragment extends Fragment implements IConnectionUser
{
	

	public static final int MAX_SEGMENT_LENGTH = 150;
	protected SharedPreferences SharedPref;
	protected MainActivity ParentActivity;
	
	@Override
	public void onAttach(Activity activity)
	{
		Log.d("sha", "StroyFragment.onAttach");
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try
		{
			ParentActivity = (MainActivity)activity;
		} 
		catch (ClassCastException e)
		{
			throw new ClassCastException(activity.toString() + " must be MainActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("sha", "StroyFragment.onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		Log.d("sha", "StroyFragment.onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		SharedPref = ParentActivity.getSharedPref();
	}
	
	public void sendHttp(String url, String output)
	{
		if (ConnectionMgr.checkConnection(ParentActivity))
		{
			HttpConnectionTask conn = new HttpConnectionTask(this);
			conn.execute(url, output);
		}
		else
		{
			// TODO
			//message(":-(  There is no internet connection");
		}
	}
	
	protected void sendStory(Story story)
	{
		String xmlFile = story.toXml();
		String url = ServerUrls.SERVER_URL + ServerUrls.SERVER_RECEIVE_URL;
		sendHttp(url, xmlFile);
	}
	
	protected void saveString(String value, String key)
	{
		Editor edit = SharedPref.edit();
		edit.putString(key, value);
		edit.commit();
	}
	
	protected void saveBoolean(boolean value, String key)
	{
		Editor edit = SharedPref.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
	
	protected void saveInt(int value, String key)
	{
		Editor edit = SharedPref.edit();
		edit.putInt(key, value);
		edit.commit();
	}
	
	protected void removeString(String key)
	{
		Editor edit = SharedPref.edit();
		edit.remove(key);
		edit.commit();
	}
	
	protected void message(String msg)
	{
		Dialog dialog = new Dialog(ParentActivity);
		TextView text = new TextView(ParentActivity);
		text.setText(msg);
		dialog.setContentView(text);
		dialog.show();
	}
}
