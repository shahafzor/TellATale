package com.moti.tellatale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private SharedPreferences SharedPref;
	
	public SharedPreferences getSharedPref()
	{
		return SharedPref;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d("sha", "MainActivity/onCreate");
		SharedPref = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		if (userName == "")
		{
			startLoginActivity();
			finish();
		}
		else
		{
			//getActionBar().hide();

			// Set the activity layout
			setContentView(R.layout.activity_main);

			// Add the menu fragment
			MenuFragment fragment = new MenuFragment();
			getFragmentManager().beginTransaction().add(R.id.frame_menu, fragment).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    // Handle item selection
	    switch (item.getItemId())
	    {
	    case R.id.action_logout:
	    	logout();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void startLoginActivity()
	{
		if (checkConnection(this))
		{
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		}
		else
		{
			TextView textView = new TextView(this);
    		textView.setTextSize(20);
            textView.setText("No network connection available");
            setContentView(textView);
		}
	}
	
	public void onClickMenuButton(View button)
	{
		StoryFragment fragment = null;
		
		switch (button.getId())
		{
		case R.id.button_new_story:
			fragment = new NewStoryFragment();
			break;
		case R.id.button_my_stories:
			fragment = new MyStoriesFragment();
			break;
		case R.id.button_get_story:
			fragment = new EditStoryFragment();
			break;
		}

		getFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();
	}
	
	public void logout()
	{
		clearFiles();
		startLoginActivity();
		finish();
	}
	
	private void clearFiles()
	{
		Editor editor = SharedPref.edit();
		editor.clear();
		editor.commit();
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
}
