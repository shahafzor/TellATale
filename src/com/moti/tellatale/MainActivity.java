package com.moti.tellatale;

//import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends FragmentActivity
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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				getActionBar().hide();
			}

			// Set the activity layout
			setContentView(R.layout.activity_main);

			if (savedInstanceState == null)
			{
				// Add the menu fragment
				MenuFragment fragment = new MenuFragment();
				getSupportFragmentManager().beginTransaction().add(R.id.frame_menu, fragment).commit();
				
				EmptyFragment fragment2 = new EmptyFragment();
				getSupportFragmentManager().beginTransaction().add(R.id.frame_main, fragment2).commit();
				//getFragmentManager().beginTransaction().add(R.id.frame_menu, fragment).commit();
			}
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
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	
	private void enableButtons()
	{
		Button b = (Button)findViewById(R.id.button_get_story);
		b.setEnabled(true);
		b = (Button)findViewById(R.id.button_my_stories);
		b.setEnabled(true);
		b = (Button)findViewById(R.id.button_new_story);
		b.setEnabled(true);
	}
	
	public void home()
	{
		enableButtons();
		Fragment fragment = new EmptyFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();
	}
	
	public void onClickMenuButton(View button)
	{
		StoryFragment fragment = null;
		enableButtons();
		
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

		if (fragment != null)
		{
			getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();
			button.setEnabled(false);
		}
	}
	
	public void logout()
	{
		clearFiles();
		startLoginActivity();
		finish();
	}
	
	private void clearFiles()
	{
		SharedPref.edit().clear().commit();
	}
}
