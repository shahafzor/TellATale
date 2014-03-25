package com.moti.tellatale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements IConnectionUser
{
	private enum Action {login, newUser}

	private SharedPreferences SharedPref;
	private String Username;
	private String Password;
	private TextView UsernameErrorTextView;
	private TextView PasswordErrorTextView;
	private Action CurrentAction = Action.login;
	private String Url = ServerUrls.LOGIN_URL;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPref = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
		setContentView(R.layout.activity_login);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			//getActionBar().hide();
		}
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
		Button button = (Button)findViewById(R.id.button_registration);
		clearErrorMessages();
		if (CurrentAction == Action.login)
		{
			CurrentAction = Action.newUser;
			Url = ServerUrls.NEW_USER_URL;
			link.setText(getString(R.string.title_activity_login));
			setTitle(getString(R.string.title_new_user));
			button.setText(getString(R.string.button_add_account));
		}
		else
		{
			CurrentAction = Action.login;
			Url = ServerUrls.LOGIN_URL;
			link.setText(getString(R.string.title_new_user));
			setTitle(getString(R.string.title_activity_login));
			button.setText(getString(R.string.button_sign_in));
		}
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
			findViewById(R.id.layout_sending).setVisibility(View.VISIBLE);
			findViewById(R.id.layout_main).setVisibility(View.GONE);
			sendHttpLoginRequest();
			
		}
	}

	public void onClickFacebookSignUp(View v)
	{}

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
			message(":-(  There is no internet connection");
		}
	}

	private void login(String permissionStr)
	{
		int permission = 0;
		if (CurrentAction == Action.login)
		{
			try
			{
				permission = Integer.parseInt(permissionStr);
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
	}

	@Override
	public void connectionFinished(int requestStatus, String response)
	{
		Log.d("sha", "async task end");
		findViewById(R.id.layout_sending).setVisibility(View.GONE);
		findViewById(R.id.layout_main).setVisibility(View.VISIBLE);
		switch (requestStatus)
		{
		case HttpConnectionTask.STATUS_LOGIN_OK:
			login(response);
			break;
		case HttpConnectionTask.STATUS_ERROR_CREDENTIALS:
			message("wrong login details");
			break;
		case HttpConnectionTask.STATUS_ILLEGAL_INPUT:
			message("illegal input");
			break;
		case HttpConnectionTask.STATUS_DUPLICATE_USER:
			message("username exists");
			break;
		default:
			message("some error");
			break;
		}
	}

	private void clearErrorMessages()
	{
		UsernameErrorTextView.setText("");
		PasswordErrorTextView.setText("");
	}

	protected void message(String msg)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(msg);
		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
			}});
		dialog.show();
	}
}
