package com.moti.tellatale;

import com.moti.tellatale.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.TextView;

public abstract class StoryActivity extends Activity
{
	protected static final String SERVER_URL = "http://www.motik.dx.am/";
	protected static final String SERVER_SEND_URL = "send_story.php";
	protected static final String SERVER_RECEIVE_URL = "receive_story.php";
	protected static final String SERVER_SEND_MY_STORIES_URL = "send_my_stories.php";
	protected static String LOGIN_URL = "index.php";
	protected static final String NEW_USER_URL = "add_user.php";
	
	
	//public static final String SERVER_URL = "http://10.0.2.2/TellATale/";
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
		String url = SERVER_URL + SERVER_RECEIVE_URL;
		sendHttp(url, xmlFile);
	}
	
	protected void sendHttp(String url, String output)
	{
		if (MainActivity.checkConnection(this))
		{
			HttpConnectionTask conn = new HttpConnectionTask(this);
			conn.execute(url, output);
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
