package com.moti.tellatale;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This activity is used to edit or continue an existing story.
 */
public class EditStoryFragment extends StoryFragment implements View.OnClickListener
{
	public final static int SEND = 1;
	public final static int REJECT = 2;
	public final static int REPLACE = 3;
	
	private static final String KeyTmpStory = "tmp story";
	private static final String KeySavedSegment = "saved segment";
	private static final String KeySegmentIndex = "segment index";
	private static final String KeyIsParallelSegment = "is it parallel";
	
	// The story object that will be edited or continued
	private Story ReceivedStory;
	
	// Indicates if the user adds a new segment to the story or edits the last segment
	private boolean NewSegment = true;
	
	private boolean StorySent = false;
	
	// Views that will be used throughout the activity 
	private TextView StoryTextView;
	private TextView LastSegmentTextView;
	private EditText EditSegment;
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_edit_story, container, false);

		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		View rootView = getView();
		EditSegment = (EditText)rootView.findViewById(R.id.edittext_add_segment);
		StoryTextView = (TextView)rootView.findViewById(R.id.textview_story);
		LastSegmentTextView = (TextView)rootView.findViewById(R.id.textview_last_segment);
		Button button = (Button)rootView.findViewById(R.id.button_reject_story);
		button.setOnClickListener(this);
		button = (Button)rootView.findViewById(R.id.button_replace_story);
		button.setOnClickListener(this);
		button = (Button)rootView.findViewById(R.id.button_send_story);
		button.setOnClickListener(this);
		button = (Button)rootView.findViewById(R.id.button_next_segment);
		button.setOnClickListener(this);
		button = (Button)rootView.findViewById(R.id.button_prev_segment);
		button.setOnClickListener(this);
		
		String xmlStory = SharedPref.getString(KeyTmpStory, null);
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
	
	public void onPause()
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
		
		getView().findViewById(R.id.layout_waiting).setVisibility(View.GONE);
		getView().findViewById(R.id.layout_main).setVisibility(View.VISIBLE);
		
		String lastSegmentText = lastSegment.getText();
		String storyText = ReceivedStory.getText();
		
		EditSegment.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_SEGMENT_LENGTH)});
		StoryTextView.setText(storyText);
		LastSegmentTextView.setTextColor(Color.RED);
		
		if (SharedPref.contains(KeySavedSegment))
		{
			EditSegment.setText(SharedPref.getString(KeySavedSegment, ""));
			EditSegment.setSelection(EditSegment.length());

			if (SharedPref.getBoolean(KeyIsParallelSegment, false))
			{
				setParallelLastSegment();
			}
			
			int index = SharedPref.getInt(KeySegmentIndex, -1);
			if (index != -1)
			{
				lastSegment = ReceivedStory.getStorySegment(index);
				if (lastSegment != null)
				{
					ReceivedStory.setCurrentLastSeqNumberLocation(index);
					lastSegmentText = lastSegment.getText();
				}
			}
		}
		
		LastSegmentTextView.setText(lastSegmentText);
	}
	
	private void handleReceivedStory(String story)
	{
		ReceivedStory = parseXml(story);
		if (ReceivedStory != null)
		{
			saveString(story, KeyTmpStory);
			if (isVisible())
			{
				loadStory();
			}
			else
			{
				Log.d("sha", "connectionFinished not visible");
			}
			
		}
		else
		{
			message("Illegal story received");
		}
	}
	
	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_XML_OK:
			handleReceivedStory(response);
			break;
		case HttpConnectionTask.STATUS_RESPONSE_OK:
			message("Your story has been sent!");
			StorySent = true;
			clearFragment();
			break;
		case HttpConnectionTask.STATUS_ILEGAL_SEGMENT:
			message("Ilegal story sent");
			clearFragment();
			break;
		case HttpConnectionTask.STATUS_NO_STORY_AVAILABLE:
			message("There is no story available at the momment");
			break;
		case HttpConnectionTask.STATUS_ERROR_CREDENTIALS:
			message("You have a problem with your credentials");
			break;
		case HttpConnectionTask.STATUS_SERVER_ERROR:
			message("Server Error");
			break;
		case HttpConnectionTask.STATUS_APP_ERROR:
			message("App Error: " + response);
			break;
		case HttpConnectionTask.STATUS_ERROR_TIMEOUT:
			message("Timeout Error");
			break;
		default:
			message("What the fuck??? " + response);
		}
	}
	
	/**
	 * Clears all the files that were created by the application
	 */
	public void clearFragment()
	{
		Editor editor = SharedPref.edit();
		editor.remove(KeyTmpStory);
		editor.remove(KeySavedSegment);
		editor.remove(KeyIsParallelSegment);
		editor.remove(KeySegmentIndex);
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
	
	/**
	 * Initiates a request to the server to send a story
	 * @param action The action the server should do
	 * SEND: just send an available story REPLACE: replace the current story
 	 */
	private void getStoryFromServer(int action, String storyName)
	{
		Log.d("sha", "EditStoryFragment.getStoryFromServer");
		String url = ServerUrls.SERVER_URL + ServerUrls.SERVER_SEND_URL;
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
		if (StorySent)
		{
			return;
		}
		
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
		
		getView().findViewById(R.id.layout_waiting).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.layout_main).setVisibility(View.GONE);
		((TextView)getView().findViewById(R.id.textview_message)).setText(getString(R.string.message_sending_story));
		
		sendStory(story);
	}
	
	public void onClickRejectButton(View button)
	{
		clearFragment();
		getView().findViewById(R.id.layout_waiting).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.layout_main).setVisibility(View.GONE);
		getStoryFromServer(REJECT, ReceivedStory.getName());
	}
	
	public void onClickReplaceButton(View button)
	{
		clearFragment();
		getView().findViewById(R.id.layout_waiting).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.layout_main).setVisibility(View.GONE);
		getStoryFromServer(REPLACE, ReceivedStory.getName());
	}
	
	/**
	 * Saves to a file the new story segment that is currently displayed on the 'EditSegment' EditText
	 */
	private void saveSegment()
	{
		if (StorySent || EditSegment == null || ReceivedStory == null)
		{
			return;
		}
		
		String storyString = EditSegment.getText().toString();
		saveString(storyString, KeySavedSegment);
		if (!NewSegment)
		{
			saveBoolean(true, KeyIsParallelSegment);
		}
		else
		{
			saveBoolean(false, KeyIsParallelSegment);
		}
		saveInt(ReceivedStory.getCurrentLastSeqNumberLocation(), KeySegmentIndex);
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

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.button_reject_story:
			onClickRejectButton(v);
			break;
		case R.id.button_replace_story:
			onClickReplaceButton(v);
			break;
		case R.id.button_send_story:
			onClickSendButton(v);
			break;
		case R.id.button_next_segment:
			onClickNextSeg(v);
			break;
		case R.id.button_prev_segment:
			onClickPrevSeg(v);
			break;
		}
	}
}
