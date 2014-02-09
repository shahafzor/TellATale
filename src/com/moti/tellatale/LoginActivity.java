package com.moti.tellatale;

import com.moti.telatale.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActivity extends Activity
{
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		WebView webview = (WebView)findViewById(R.id.webview_login);
		webview.addJavascriptInterface(this, "Android");
		webview.setWebViewClient(new WebViewClient());
		WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		webview.loadUrl(getString(R.string.server_url));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	/**
	 * this function is called by the javascript of the login webpage after a successful login
	 */
	@JavascriptInterface
	public void login(String username, int permission, String password)
	{
		SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref_file_name), MODE_PRIVATE);
	    SharedPreferences.Editor editor = sharedPref.edit();
	    editor.putString(getString(R.string.pref_key_user_name), username);
	    editor.putString(getString(R.string.pref_key_user_password), password);
	    editor.putInt(getString(R.string.pref_key_user_permission), permission);
	    editor.commit();   
	    setResult(RESULT_OK, null);
	    finish();
	}
}