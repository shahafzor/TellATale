package com.moti.tellatale;

import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends StoryActivity
{
	private enum Action {login, newUser}

	private String Username;
	private String Password;
	private TextView ErrorTextView;
	private TextView UsernameErrorTextView;
	private TextView PasswordErrorTextView;
	private Action CurrentAction = Action.login;
	private String Url = LOGIN_URL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ErrorTextView = (TextView) findViewById(R.id.textview_error_msg);
		ErrorTextView.setTextColor(Color.RED);
		UsernameErrorTextView = (TextView) findViewById(R.id.textview_username_error_msg);
		UsernameErrorTextView.setTextColor(Color.RED);
		PasswordErrorTextView = (TextView) findViewById(R.id.textview_password_error_msg);
		PasswordErrorTextView.setTextColor(Color.RED);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void onClickChangeAction(View v)
	{
		TextView link = (TextView)v;
		clearErrorMessages();
		if (CurrentAction == Action.login)
		{
			CurrentAction = Action.newUser;
			Url = NEW_USER_URL;
			link.setText(getString(R.string.title_activity_login));
			setTitle(getString(R.string.title_new_user));
		}
		else
		{
			CurrentAction = Action.login;
			Url = LOGIN_URL;
			link.setText(getString(R.string.title_new_user));
			setTitle(getString(R.string.title_activity_login));
		}
		link.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
	}
	
	public void onClickLogin(View v)
	{
		clearErrorMessages();
		boolean inputIsValid = true;
		
		Username = ((EditText)findViewById(R.id.edittext_username)).getText().toString();
		if (!InputValidation.validateUserName(Username))
		{
			inputIsValid = false;
			UsernameErrorTextView.setText(getString(R.string.error_msg_username));
		}
		
		Password = ((EditText)findViewById(R.id.edittext_password)).getText().toString();
		if (!InputValidation.validatePassword(Password))
		{
			inputIsValid = false;
			PasswordErrorTextView.setText(getString(R.string.error_msg_password));
		}
		
		if (inputIsValid)
		{
			String url = SERVER_URL + Url;
			String credentials = "username=" + Username + "&password=" + Password;
			url += "?" + credentials;
			sendHttp(url, null);
		}
	}

	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_LOGIN_OK:
			try
			{
				int permission = 0;
				if (CurrentAction == Action.login)
					permission = Integer.parseInt(response);
				Editor editor = SharedPref.edit();
				editor.putString(getString(R.string.pref_key_user_name), Username);
				editor.putString(getString(R.string.pref_key_user_password), Password);
				editor.putInt(getString(R.string.pref_key_user_permission), permission);
				editor.commit();   
				setResult(RESULT_OK, null);
				finish();
				break;
			}
			catch (NumberFormatException e){}
		case HttpConnectionTask.STATUS_ERROR_CREDENTIALS:
			ErrorTextView.setText("wrong login details");
			break;
		case HttpConnectionTask.STATUS_ILLEGAL_INPUT:
			ErrorTextView.setText("illegal input");
			break;
		default:
			ErrorTextView.setText("some error");
			break;
		}
	}
	
	private void clearErrorMessages()
	{
		ErrorTextView.setText("");
		UsernameErrorTextView.setText("");
		PasswordErrorTextView.setText("");
	}
}
