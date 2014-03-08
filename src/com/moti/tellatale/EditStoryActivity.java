package com.moti.tellatale;

import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moti.tellatale.R;

/**
 * This activity is used to edit or continue an existing story.
 */
public class EditStoryActivity extends StoryActivity
{
	public final static int SEND = 1;
	public final static int REJECT = 2;
	public final static int REPLACE = 3;
	
	// The story object that will be edited or continued
	private Story ReceivedStory;
	
	// Indicates if the user adds a new segment to the story or edits the last segment
	private boolean NewSegment = true;
	
	// Views that will be used throughout the activity 
	private TextView StoryTextView;
	private TextView LastSegmentTextView;
	private EditText EditSegment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		String xmlStory = SharedPref.getString(getString(R.string.pref_key_temp_story), null);
		if (xmlStory != null)
		{
			ReceivedStory = parseXml(xmlStory);
			if (ReceivedStory != null)
			{
				loadStory();
				return;
			}
		}
		
		getStoryFromServer(SEND, null);
	}
	
	protected void onPause()
	{
		super.onPause();
		saveSegment();
	}
	
	/**
	 * Takes the Story object 'ReceivedStory', parses it, and displays it on the screen
	 */
	private void loadStory()
	{
		if (ReceivedStory == null)
		{
			message("You should take a nap");
			return;
		}
		
		StorySegment lastSegment = ReceivedStory.getCurrentSegment();
		if (lastSegment == null)
		{
			message("Have a nice day :-(");
			return;
		}
		
		String lastSegmentText = lastSegment.getText();
		String storyText = ReceivedStory.getText();
		
		setContentView(R.layout.activity_story);
		EditSegment = (EditText)findViewById(R.id.edittext_add_segment);
		EditSegment.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_SEGMENT_LENGTH)});
		StoryTextView = (TextView) findViewById(R.id.textview_story);
		StoryTextView.setText(storyText);
		LastSegmentTextView = (TextView) findViewById(R.id.textview_last_segment);
		LastSegmentTextView.setTextColor(Color.RED);
		
		if (SharedPref.contains(getString(R.string.pref_key_saved_segment)))
		{
			EditSegment.setText(SharedPref.getString(getString(R.string.pref_key_saved_segment), ""));
			EditSegment.setSelection(EditSegment.length());

			if (SharedPref.getBoolean(getString(R.string.pref_key_is_parallel_segment), false))
			{
				setParallelLastSegment();
			}
			
			int index = SharedPref.getInt(getString(R.string.pref_key_segment_index), -1);
			if (index != -1)
			{
				ReceivedStory.setCurrentLastSeqNumberLocation(index);
				lastSegmentText = ReceivedStory.getStorySegment(index).getText();
			}
		}
		
		LastSegmentTextView.setText(lastSegmentText);
	}
	
	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_XML_OK:
			ReceivedStory = parseXml(response);
			if (ReceivedStory != null)
			{
				saveString(response, getString(R.string.pref_key_temp_story));
				loadStory();
			}
			else
			{
				message("Illegal story received");
			}
			break;
		case HttpConnectionTask.STATUS_RESPONSE_OK:
			message(":-) Your story has been sent!");
			clearActivity();
			break;
		case HttpConnectionTask.STATUS_ILEGAL_SEGMENT:
			message(":-( Ilegal story sent");
			clearActivity();
			break;
		case HttpConnectionTask.STATUS_NO_STORY_AVAILABLE:
			message(":-( There is no story available at the momment");
			break;
		case HttpConnectionTask.STATUS_ERROR_CREDENTIALS:
			message(":-( You have a problem with your credentials");
			break;
		case HttpConnectionTask.STATUS_SERVER_ERROR:
			message(":-( Server Error");
			break;
		case HttpConnectionTask.STATUS_APP_ERROR:
			message(":-( App Error: " + response);
			break;
		case HttpConnectionTask.STATUS_ERROR_TIMEOUT:
			message(":-( Timeout Error");
			break;
		default:
			message(":-( What the fuck??? " + response);
		}
	}
	
	/**
	 * Clears all the files that were created by the application
	 */
	public void clearActivity()
	{
		Editor editor = SharedPref.edit();
		editor.remove(getString(R.string.pref_key_temp_story));
		editor.remove(getString(R.string.pref_key_saved_segment));
		editor.remove(getString(R.string.pref_key_is_parallel_segment));
		editor.remove(getString(R.string.pref_key_segment_index));
		editor.commit();
		
		// make sure EditSegment's text will not be saved when activity ends
		EditSegment.setText("");
		NewSegment = true;
	}
	
	/**
	 * Parse an xml String representing a story to a 'Story' object
	 * @param xmlStory
	 * @return Story object on success, null on failure
	 */
	private static Story parseXml(String xmlStory)
	{
		if (xmlStory == null)
		{
			return null;
		}
		
		XmlParser xmlParser = new XmlParser();
		try
		{
			Story story = xmlParser.parseStory(xmlStory);
			return story;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_story, menu);
		return true;
	}
	
	/**
	 * Initiates a request to the server to send a story
	 * @param action The action the server should do
	 * SEND: just send an available story REPLACE: replace the current story
 	 */
	private void getStoryFromServer(int action, String storyName)
	{
		message("Loading story...");
		String url = SERVER_URL + SERVER_SEND_URL;
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
		String credentials = "username=" + userName + "&password=" + password;
		url += "?" + credentials + "&action=" + action;
		if (storyName != null)
		{
			url += "&storyName=" + storyName;
		}
		sendHttp(url, null);
	}

	public void onClickSendButton(View sendButton)
	{
		StorySegment segment = null;
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
		String text = EditSegment.getText().toString();
		if (NewSegment)
		{	
			segment = ReceivedStory.createNewSegment(text, userName, password);
		}
		else
		{
			segment = ReceivedStory.createparallelSegment(text, userName, password);
		}
		Story story = new Story(segment, ReceivedStory.getName());
		message("Sending...");
		sendStory(story);
	}
	
	public void onClickRejectButton(View button)
	{
		clearActivity();
		message("Loading a new story...");
		getStoryFromServer(REJECT, ReceivedStory.getName());
	}
	
	public void onClickReplaceButton(View button)
	{
		clearActivity();
		message("Loading a new story...");
		getStoryFromServer(REPLACE, ReceivedStory.getName());
	}
	
	/**
	 * Saves to a file the new story segment that is currently displayed on the 'EditSegment' EditText
	 */
	private void saveSegment()
	{
		if (EditSegment == null || EditSegment.getText().toString() == "")
		{
			return;
		}
		
		String storyString = EditSegment.getText().toString();
		saveString(storyString, getString(R.string.pref_key_saved_segment));
		if (!NewSegment)
		{
			saveBoolean(true, getString(R.string.pref_key_is_parallel_segment));
		}
		else
		{
			saveBoolean(false, getString(R.string.pref_key_is_parallel_segment));
		}
		saveInt(ReceivedStory.getCurrentLastSeqNumberLocation(), getString(R.string.pref_key_segment_index));
	}
	
	private void setParallelLastSegment()
	{
		LastSegmentTextView.setTextColor(Color.GRAY);
		NewSegment = false;
	}
	
	public void onClickNextSeg(View Xbutton)
	{
		StorySegment lastSegment = ReceivedStory.getNextSegment();
		if (lastSegment != null)
		{
			LastSegmentTextView.setText(lastSegment.getText());
			EditSegment.setText("");
		}
		else if (NewSegment)
		{
			setParallelLastSegment();
			EditSegment.setText(LastSegmentTextView.getText().toString());
			EditSegment.setSelection(EditSegment.length());
		}
	}
	
	public void onClickPrevSeg(View Vbutton)
	{
		if (!NewSegment)
		{
			StorySegment lastSegment = ReceivedStory.getCurrentSegment();
			LastSegmentTextView.setText(lastSegment.getText());
			EditSegment.setText("");
			LastSegmentTextView.setTextColor(Color.RED);
			NewSegment = true;
		}
		else
		{
			StorySegment lastSegment = ReceivedStory.getPrevSegment();
			if (lastSegment != null)
			{
				LastSegmentTextView.setText(lastSegment.getText());
				EditSegment.setText("");
			}
		}
	}
}
