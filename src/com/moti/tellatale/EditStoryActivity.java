package com.moti.tellatale;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moti.telatale.R;
/**TODO:
 * need_drop
 * tests:
 * 1 - receive illegal xml file
 */

/**
 * This activity is used to edit or continue an existing story.
 */
public class EditStoryActivity extends StoryActivity
{
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
		message("Loading story...");
		
		boolean fileExists = SharedPref.contains(getString(R.string.pref_key_temp_story_file_exists));
		if (fileExists)
		{
			String fileName = getString(R.string.temp_story_file_name);
			ReceivedStory = parseXml(getXmlFromFile(fileName));
			if (ReceivedStory != null)
			{
				loadStory();
			}
			else
			{
				getStoryFromServer();
			}
		}
		else
		{
			getStoryFromServer();
		}
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
		String storyText = ReceivedStory.toString();
		
		setContentView(R.layout.activity_story);
		LastSegmentTextView = (TextView) findViewById(R.id.textview_last_segment);
		EditSegment = (EditText)findViewById(R.id.edittext_add_segment);
		EditSegment.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_SEGMENT_LENGTH)});
		StoryTextView = (TextView) findViewById(R.id.textview_story);
		LastSegmentTextView.setTextColor(Color.RED);
		StoryTextView.setText(storyText);

		if (SharedPref.contains(getString(R.string.pref_key_saved_segment)))
		{
			EditSegment.setText(SharedPref.getString(getString(R.string.pref_key_saved_segment), ""));
			EditSegment.setSelection(EditSegment.length());

			if (SharedPref.getBoolean(getString(R.string.pref_key_is_duplicate_segment), false))
			{
				setX();
			}
			//else
			//{
				int index = SharedPref.getInt(getString(R.string.pref_key_segment_index), -1);
				if (index != -1)
				{
					ReceivedStory.setCurrentLastSeqNumberLocation(index);
					lastSegmentText = ReceivedStory.getStorySegment(index).getText();
				}
			//}
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
				saveStory(response);
				loadStory();
			}
			break;
		case HttpConnectionTask.STATUS_RESPONSE_OK:
			message(":-) Your story has been sent!");
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
			message(":-( App Error");
			break;
		case HttpConnectionTask.STATUS_ERROR_TIMEOUT:
			message(":-( Timeout Error");
			break;
		case 1:
			message(":-( Error #1");
			break;
		case 2:
			message(":-( Error #2");
			break;
		case 3:
			message(":-( Error #3: " + response);
			break;
		case 4:
			message(":-( Error #4: " + response);
			break;
		case 5:
			message(":-( Error #5");
			break;
		case 6:
			message(":-( Error #6");
			break;
		case 7:
			message(":-( Error #7");
			break;
		case 8:
			message(":-( Error #8");
			break;
		case 9:
			message(":-( Error #9");
			break;
		case 10:
			message(":-( Error #10:" + response);
			break;
		default:
			message(":-( What the fuck???");
		}
	}
	
	/**
	 * Clears all the files that were created by the application
	 */
	public void clearActivity()
	{
		Editor editor = SharedPref.edit();
		editor.remove(getString(R.string.pref_key_temp_story_file_exists));
		editor.remove(getString(R.string.pref_key_saved_segment));
		editor.remove(getString(R.string.pref_key_is_duplicate_segment));
		editor.remove(getString(R.string.pref_key_segment_index));
		editor.commit();
		deleteFile(getString(R.string.temp_story_file_name));
		EditSegment = null;
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
			Story story = xmlParser.parse(xmlStory);
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
	
	
	private void getStoryFromServer()
	{
		String url = getString(R.string.server_url) + getString(R.string.server_send_url_suffix);
		if (MainActivity.checkConnection(this))
		{
			String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
			String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
			String credentials = "username=" + userName + "&password=" + password;
			HttpConnectionTask conn = new HttpConnectionTask(this);
			conn.execute(url, credentials, "html");
		}
		else
		{
			message("You have no internet connection");
		}
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
	
	/**
	 * Saves to a file the new story segment that is currently displayed on the 'EditSegment' EditText
	 */
	private void saveSegment()
	{
		if (EditSegment == null)
		{
			return;
		}
		
		String storyString = EditSegment.getText().toString();
		saveString(storyString, getString(R.string.pref_key_saved_segment));
		if (!NewSegment)
		{
			saveBoolean(true, getString(R.string.pref_key_is_duplicate_segment));
		}
		else
		{
			saveBoolean(false, getString(R.string.pref_key_is_duplicate_segment));
		}
		saveInt(ReceivedStory.getCurrentLastSeqNumberLocation(), getString(R.string.pref_key_segment_index));
	}
	
	
	private void setX()
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
			setX();
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

	/**
	 * Saves the story object 'ReceivedStory' to a xml file
	 */
	private void saveStory(String story)
	{
		String fileName = getString(R.string.temp_story_file_name);
		if (saveXmlToFile(story, fileName))
		{
			saveBoolean(true, getString(R.string.pref_key_temp_story_file_exists));
		}
	}
	
	/**
	 * Saves a xml format String to a xml file 
	 * @param xmlString
	 * @param fileName
	 * @return if file was saved: true, else: false
	 */
	private boolean saveXmlToFile(String xmlString, String fileName)
	{
		try
		{
			FileOutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);
			outputStream.write(xmlString.getBytes());
			outputStream.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Opens the xml file 'filename', and returns its content as a String
	 * @param fileName the name of the xml file to read
	 * @return String content of the file, or null if an error occurred
	 */
	private String getXmlFromFile(String fileName)
	{
		byte[] buffer = new byte[StoryActivity.BUFFER_SIZE];
		String xml = new String();
		try
		{
			FileInputStream inputStream = openFileInput(fileName);
			int len = 0;
			while (len != -1)
			{
				len = inputStream.read(buffer);
				if (len != -1)
				{
					xml += new String(buffer, 0, len);
				}
			}
		}
		catch (Exception e)
		{
			return null;
		}
		
		return xml;
	}
}