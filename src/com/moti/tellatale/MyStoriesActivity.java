package com.moti.tellatale;

import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.moti.telatale.R;

/** TODO:
 * - decide what to do with many versions on last sequence number
 */
public class MyStoriesActivity extends StoryActivity
{
	private static final String StoriesXmlKey = "StoriesXmlKey";
	private static final String CurrentIndexKey = "CurrentIndexKey";
	private String StoriesXml;
	private List<Story> Stories;
	private int CurrentIndex = 0;
	TextView StoryTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null)
		{
			message("Getting your stories...");
			getMyStoriesFromServer();
		}
	}
	
	@Override
	protected void onSaveInstanceState (Bundle savedInstanceState)
	{
		savedInstanceState.putString(StoriesXmlKey, StoriesXml);
		savedInstanceState.putInt(CurrentIndexKey, CurrentIndex);
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		StoriesXml = savedInstanceState.getString(StoriesXmlKey);
		CurrentIndex = savedInstanceState.getInt(CurrentIndexKey);
		loadStories();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_stories, menu);
		return true;
	}

	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		if (requestStatus == HttpConnectionTask.STATUS_XML_OK)
		{
			StoriesXml = response;
			loadStories();
		}
		else if (requestStatus == HttpConnectionTask.STATUS_NO_STORY_AVAILABLE)
		{
			message("You have no story yet, my friend");
		}
		else
		{
			message("What's the matter, dear?");
		}
	}
	
	private void loadStories()
	{
		Stories = parseXml(StoriesXml);
		if (Stories != null && Stories.size() > 0)
		{
			setContentView(R.layout.activity_my_stories);
			StoryTextView = (TextView) findViewById(R.id.textview_story);
			try
			{
				Story story = Stories.get(CurrentIndex);
				StoryTextView.setText(story.toString());
			}
			catch (IndexOutOfBoundsException e)
			{
				message("Happy cow!");
			}
			
		}
		else
		{
			message("Where is the cat?");
		}
	}
	
	/**
	 * Initiates a request to the server to send all the stories that this user was involved in
 	 */
	private void getMyStoriesFromServer()
	{
		String url = getString(R.string.server_url) + getString(R.string.server_send_my_stories_url_suffix);
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
		String credentials = "username=" + userName + "&password=" + password;
		url += "?" + credentials;
		sendHttp(url, null);
	}
	
	public void onClickNextStory(View sendButton)
	{
		if (Stories != null && CurrentIndex < Stories.size() - 1)
		{
			CurrentIndex++;
			StoryTextView.setText(Stories.get(CurrentIndex).toString());
		}
	}
	
	public void onClickPrevStory(View sendButton)
	{
		if (Stories != null && CurrentIndex > 0)
		{
			CurrentIndex--;
			StoryTextView.setText(Stories.get(CurrentIndex).toString());
		}
	}
	
	/**
	 * Parse an xml String representing stories to a list of 'Story' objects
	 * @param xmlStories
	 * @return list of 'Story' objects on success, null on failure
	 */
	private static List<Story> parseXml(String xmlStories)
	{
		if (xmlStories == null)
		{
			return null;
		}
		
		XmlParser xmlParser = new XmlParser();
		try
		{
			List<Story> stories = xmlParser.parseStories(xmlStories);
			return stories;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
