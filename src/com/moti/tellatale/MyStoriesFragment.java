package com.moti.tellatale;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MyStoriesFragment extends StoryFragment implements View.OnClickListener
{
	private static final String StoriesXmlKey = "StoriesXmlKey";
	private static final String CurrentIndexKey = "CurrentIndexKey";
	private String StoriesXml = null;
	private List<Story> Stories;
	private int CurrentIndex = 0;
	private TextView StoryTextView;
	private Button ButtonNext;
	private Button ButtonPrev;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_my_stories, container, false);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		View rootView = getView();
		StoryTextView = (TextView)rootView.findViewById(R.id.textview_story);
		ButtonNext = (Button)rootView.findViewById(R.id.button_next);
		ButtonPrev = (Button)rootView.findViewById(R.id.button_prev);
		ButtonPrev.setOnClickListener(this);
		ButtonNext.setOnClickListener(this);
		
		if (savedInstanceState == null || savedInstanceState.getString(StoriesXmlKey) == null)
		{
			getMyStoriesFromServer();
		}
		else
		{
			StoriesXml = savedInstanceState.getString(StoriesXmlKey);
			CurrentIndex = savedInstanceState.getInt(CurrentIndexKey);
			loadStories();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		if (StoriesXml != null)
		{
			savedInstanceState.putString(StoriesXmlKey, StoriesXml);
			savedInstanceState.putInt(CurrentIndexKey, CurrentIndex);
		}

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.button_next:
			nextStory();
			break;
		case R.id.button_prev:
			prevStory();
			break;
		}
	}

	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		if (!isVisible())
		{
			Log.d("sha", "MyStroiesFragment.connectionFinished not added");
			return;
		}
		
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
		getView().findViewById(R.id.layout_loading).setVisibility(View.GONE);
		getView().findViewById(R.id.layout_main).setVisibility(View.VISIBLE);
		Stories = parseXml(StoriesXml);
		if (Stories != null && Stories.size() > 0)
		{
			try
			{
				Story story = Stories.get(CurrentIndex);
				StoryTextView.setText(story.getText());
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
		String url = ServerUrls.SERVER_URL + ServerUrls.SERVER_SEND_MY_STORIES_URL;
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
		String credentials = "username=" + userName + "&password=" + password;
		
		String facebookId = SharedPref.getString(getString(R.string.pref_key_user_facebook_id), "");
		if (facebookId != "")
		{
			credentials += "&facebookId=" + facebookId;
		}
		
		url += "?" + credentials;
		sendHttp(url, null);
	}
	
	private void nextStory()
	{
		if (Stories != null && CurrentIndex < Stories.size() - 1)
		{
			CurrentIndex++;
			StoryTextView.setText(Stories.get(CurrentIndex).getText());
		}
	}
	
	private void prevStory()
	{
		if (Stories != null && CurrentIndex > 0)
		{
			CurrentIndex--;
			StoryTextView.setText(Stories.get(CurrentIndex).getText());
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
