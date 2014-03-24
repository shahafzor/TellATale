package com.moti.tellatale;

import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewStoryFragment extends StoryFragment implements View.OnClickListener
{
	private static final String KeyNewStory = "new story";
	
	private EditText EdittextNewStory;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("sha", "NewStoryFragment.onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d("sha", "NewStoryFragment.onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_new_story, container, false);

		if (savedInstanceState == null)
		{
			EdittextNewStory = (EditText)rootView.findViewById(R.id.edittext_new_story);
			EdittextNewStory.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_SEGMENT_LENGTH)});
			Button button = (Button)rootView.findViewById(R.id.button_send_story);
			button.setOnClickListener(this);
		}

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		Log.d("sha", "NewStoryFragment.onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState != null)
		{
			return;
		}
		
		String savedStory = SharedPref.getString(KeyNewStory, "");
		if (!savedStory.equals(""))
		{
			EdittextNewStory.setText(savedStory);
			EdittextNewStory.setSelection(EdittextNewStory.length());
		}
	}
	
	public void onPause()
	{
		Log.d("sha", "NewStoryFragment.onPause");
		super.onPause();
		
		if (EdittextNewStory == null)
		{
			return;
		}

		String storyString = EdittextNewStory.getText().toString();
		saveString(storyString, KeyNewStory);
	}
	
	private void onClickSendButton(View sendButton)
	{
		String userName = SharedPref.getString(getString(R.string.pref_key_user_name), "");
		String password = SharedPref.getString(getString(R.string.pref_key_user_password), "");
		String storyString = EdittextNewStory.getText().toString();
		
		StorySegment storySegment = new StorySegment(storyString, userName);
		storySegment.setPassword(password);
		
		Story story = new Story(storySegment);
		
		getView().findViewById(R.id.layout_sending).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.layout_main).setVisibility(View.GONE);
		((TextView)getView().findViewById(R.id.textview_message)).setText(getString(R.string.message_sending_story));
		
		sendStory(story);
	}

	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_RESPONSE_OK:
			EdittextNewStory.setText("");
			message("Your story has been sent!");
			break;
		case HttpConnectionTask.STATUS_SERVER_ERROR:
			message("Server Error");
			break;
		case HttpConnectionTask.STATUS_APP_ERROR:
			message("App Error " + response);
			break;
		case HttpConnectionTask.STATUS_NO_PERMISSION:
			message("Action not authorized " + response);
			break;
		case HttpConnectionTask.STATUS_ERROR_TIMEOUT:
			message("Timeout Error");
			break;
		default:
			message("Something... " + response);
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.button_send_story:
			onClickSendButton(v);
			break;
		}
	}
}
