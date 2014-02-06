package com.moti.tellatale;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.moti.telatale.R;

public class NewStoryActivity extends StoryActivity
{
	private EditText EdittextNewStory;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_story);
		
		EdittextNewStory = (EditText) findViewById(R.id.edittext_new_story);
		EdittextNewStory.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_SEGMENT_LENGTH)});
		
		if (SharedPref.contains(getString(R.string.pref_key_saved_new_story_segment)))
		{
			EdittextNewStory.setText(SharedPref.getString(getString(R.string.pref_key_saved_new_story_segment), ""));
			EdittextNewStory.setSelection(EdittextNewStory.length());
		}
	}
	
	protected void onPause()
	{
		super.onPause();
		if (EdittextNewStory != null)
		{
			String storyString = EdittextNewStory.getText().toString();
			saveString(storyString, getString(R.string.pref_key_saved_new_story_segment));
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_story, menu);
		return true;
	}
	
	public void onClickSendButton(View sendButton)
	{
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
		String storyString = EdittextNewStory.getText().toString();
		
		StorySegment storySegment = new StorySegment(storyString, userName);
		storySegment.setPassword(password);
		
		Story story = new Story(storySegment);
		message("Sending...");
		sendStory(story);
	}

	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_RESPONSE_OK:
			removeString(getString(R.string.pref_key_saved_new_story_segment));
			EdittextNewStory = null;
			message(":-) Your story has been sent!");
			break;
		case HttpConnectionTask.STATUS_SERVER_ERROR:
			message(":-( Server Error");
			break;
		case HttpConnectionTask.STATUS_APP_ERROR:
			message(":-( App Error" + response);
			break;
		case HttpConnectionTask.STATUS_ERROR_TIMEOUT:
			message(":-( Timeout Error");
			break;
		default:
			message("Something..." + response);
		}
	}
}
