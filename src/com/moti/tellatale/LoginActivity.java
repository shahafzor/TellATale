package com.moti.tellatale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements IConnectionUser
{
	private enum Action {login, newUser}
	
	private SharedPreferences SharedPref;
	private String Username;
	private String Password;
	private TextView ErrorTextView;
	private TextView UsernameErrorTextView;
	private TextView PasswordErrorTextView;
	private Action CurrentAction = Action.login;
	private String Url = ServerUrls.LOGIN_URL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPref = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
		setContentView(R.layout.activity_login);
		//getActionBar().hide();
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
			Url = ServerUrls.NEW_USER_URL;
			link.setText(getString(R.string.title_activity_login));
			setTitle(getString(R.string.title_new_user));
		}
		else
		{
			CurrentAction = Action.login;
			Url = ServerUrls.LOGIN_URL;
			link.setText(getString(R.string.title_new_user));
			setTitle(getString(R.string.title_activity_login));
		}
		link.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
	}
	
	public void onClickLogin(View button)
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
			sendHttpLoginRequest();
		}
	}
	
	public void sendHttpLoginRequest()
	{
		if (ConnectionMgr.checkConnection(this))
		{
			String url = ServerUrls.SERVER_URL + Url;
			String credentials = "username=" + Username + "&password=" + Password;
			url += "?" + credentials;
			HttpConnectionTask conn = new HttpConnectionTask(this);
			conn.execute(url, null);
		}
		else
		{
			// TODO
			//message(":-(  There is no internet connection");
		}
	}
	
	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_LOGIN_OK:
			int permission = 0;
			if (CurrentAction == Action.login)
			{
				try
				{
					permission = Integer.parseInt(response);
				}
				catch (NumberFormatException e){}
			}
			Editor editor = SharedPref.edit();
			editor.putString(getString(R.string.pref_key_user_name), Username);
			editor.putString(getString(R.string.pref_key_user_password), Password);
			editor.putInt(getString(R.string.pref_key_user_permission), permission);
			editor.commit();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
			break;
		case HttpConnectionTask.STATUS_ERROR_CREDENTIALS:
			ErrorTextView.setText("wrong login details");
			break;
		case HttpConnectionTask.STATUS_ILLEGAL_INPUT:
			ErrorTextView.setText("illegal input");
			break;
		case HttpConnectionTask.STATUS_DUPLICATE_USER:
			ErrorTextView.setText("username exists");
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
