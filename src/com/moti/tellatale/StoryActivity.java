package com.moti.tellatale;

import com.moti.telatale.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.TextView;

public abstract class StoryActivity extends Activity
{
	public static final int BUFFER_SIZE = 1024;
	public static final int MAX_SEGMENT_LENGTH = 150;
	protected SharedPreferences SharedPref;

	/**
	 * A callback function that is called by the async task 'HttpConnectionTask' when
	 * it has finished to send/receive data to the server
	 * @param requestStatus the status the task returned with
	 * @param response the response, if any, from the server
	 */
	public abstract void connectionFinished(int requestStatus, String response);
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPref = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
	}
	
	protected void sendStory(Story story)
	{
		String xmlFile = story.toXml();
		String url = getString(R.string.server_url) + getString(R.string.server_recv_url_suffix);
		
		if (MainActivity.checkConnection(this))
		{
			HttpConnectionTask conn = new HttpConnectionTask(this);
			conn.execute(url, xmlFile);
		}
		else
		{
			message(":-(  There is no internet connection");
		}
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
		TextView textview = new TextView(this);
		textview.setText(msg);
		setContentView(textview);
	}
}
