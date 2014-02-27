package com.moti.tellatale;

//TODO
// - check user input
// - manage 'strings' res

import com.moti.telatale.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity
{
	public static final int LOGIN_REQUEST = 0;
	private SharedPreferences SharedPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Set the activity layout
		setContentView(R.layout.activity_main);
	
		SharedPref = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		int permission = SharedPref.getInt(getString(R.string.pref_key_user_permission), 0);
		if (userName == "")
		{
			startLoginActivity();
		}
		else
		{
			setMenu(permission, userName, true);
		}
		
	}
	
	private void startLoginActivity()
	{
		if (checkConnection(this))
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, LOGIN_REQUEST);
		}
		else
		{
			TextView textView = new TextView(this);
    		textView.setTextSize(20);
            textView.setText("No network connection available");
            setContentView(textView);
		}
	}
	
	public void onClickLogin(View view)
	{
		startLoginActivity();
	}
	
	public void onClickNewStory(View view)
	{
		Intent intent = new Intent(this, NewStoryActivity.class);
		startActivity(intent);
	}
	
	public void onClickGetStory(View view)
	{
		Intent intent = new Intent(this, EditStoryActivity.class);
		startActivity(intent);
	}
	
	public void onClickMyStories(View view)
	{
		Intent intent = new Intent(this, MyStoriesActivity.class);
		startActivity(intent);
	}
	
	public void onClickLogout(View view)
	{
		clearFiles();
		setMenu(1, null, false);
	}
	
	//TODO: create clear() function for each activity
	private void clearFiles()
	{
		Editor editor = SharedPref.edit();
		editor.remove(getString(R.string.pref_key_user_name));
		editor.remove(getString(R.string.pref_key_user_password));
		editor.remove(getString(R.string.pref_key_user_permission));
		editor.remove(getString(R.string.pref_key_saved_new_story_segment));
		editor.remove(getString(R.string.pref_key_temp_story_file_exists));
		editor.remove(getString(R.string.pref_key_saved_segment));
		editor.remove(getString(R.string.pref_key_is_duplicate_segment));
		editor.remove(getString(R.string.pref_key_segment_index));
		editor.commit();
		deleteFile(getString(R.string.temp_story_file_name));
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == LOGIN_REQUEST)
		{
			if (resultCode == RESULT_OK)
			{
				int permission = SharedPref.getInt(getString(R.string.pref_key_user_permission), 0);
				String username = SharedPref.getString(getString(R.string.pref_key_user_name), "");
				setMenu(permission,username, true);
			}
		}
	}
	
	public static boolean checkConnection(Activity activity)
	{
		ConnectivityManager connMgr = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connMgr == null)
		{
			return false;
		}
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * set the menu setup according to user login status and permission
	 */
	private void setMenu(int permission, String username, boolean logedIn)
	{
		TextView textview = (TextView) findViewById(R.id.textview_user_name);
		if (username != null)
		{
			textview.setVisibility(View.VISIBLE);
			textview.setText("Hi " + username);
		}
		else
		{
			textview.setVisibility(View.GONE);
		}
		
		int visibility1 = logedIn ? View.GONE : View.VISIBLE;
		int visibility2 = logedIn ? View.VISIBLE : View.GONE;
		
		Button button = (Button) findViewById(R.id.button_login);
		button.setVisibility(visibility1);
		
		button = (Button) findViewById(R.id.button_logout);
    	button.setVisibility(visibility2);
    	
	    if (permission > 0)
	    {
	    	button = (Button) findViewById(R.id.button_new_story);
	    	button.setVisibility(visibility2);
	    }
	    
	    button = (Button) findViewById(R.id.button_get_story);
    	button.setVisibility(visibility2);
    	
    	button = (Button) findViewById(R.id.button_my_stories);
    	button.setVisibility(visibility2);
	}
}