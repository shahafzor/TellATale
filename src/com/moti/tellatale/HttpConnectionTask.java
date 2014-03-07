package com.moti.tellatale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.os.AsyncTask;

class HttpConnectionTask extends AsyncTask<String, Void, String>
{
	// server error codes
	public static final int STATUS_XML_OK = 1;
	public static final int STATUS_NO_STORY_AVAILABLE = 2;
	public static final int STATUS_RESPONSE_OK = 3;
	public static final int STATUS_SERVER_ERROR = 4;
	public static final int STATUS_ERROR_CREDENTIALS = 5;
	public static final int STATUS_ILEGAL_SEGMENT = 6;
	public static final int STATUS_LOGIN_OK = 7;
	public static final int STATUS_ILLEGAL_INPUT = 8;
	public static final int STATUS_DUPLICATE_USER = 9;
	
	// app error codes
	public static final int STATUS_APP_ERROR = 100;
	public static final int STATUS_ERROR_XML_PARSE = 101;
	public static final int STATUS_ERROR_STRING_CONVERT = 102;
	public static final int STATUS_ERROR_TIMEOUT = 103;
	
	private StoryActivity ParentActivity;
	private int RequestStatus = STATUS_APP_ERROR;

	public HttpConnectionTask(StoryActivity activity)
	{
		this.ParentActivity = activity;
	}

	@Override
	protected String doInBackground(String... params)
	{
		// params comes from the execute() call: params[0] is the url params[1] is the output.
		try
		{
			return sendRequest(params[0], params[1]);
		}
		catch (SocketTimeoutException e)
		{
			RequestStatus = STATUS_ERROR_TIMEOUT;
			return e.toString();
		}
		catch (IOException e)
		{
			return e.toString();
		}
	}
	
	protected void onPostExecute(String response)
	{
        ParentActivity.connectionFinished(RequestStatus, response);
    }

	private String sendRequest(String serverUrl, String output) throws IOException, SocketTimeoutException
	{
		InputStream inputStream = null;
		HttpURLConnection conn = null;
		
		try
		{
			URL url = new URL(serverUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(30000 /* milliseconds */);
			conn.setConnectTimeout(10000 /* milliseconds */);

			if (output != null)
			{
				doOutput(conn, output);
			}
			
			String response = Integer.toString(conn.getResponseCode());
			RequestStatus = conn.getHeaderFieldInt("status_code", 0);
			if (RequestStatus == STATUS_XML_OK || RequestStatus == STATUS_LOGIN_OK)
			{
				inputStream = conn.getInputStream();
				response = readIt(inputStream);
			}
			return response;
		}
		// Make sure that resources are closed after the application has finished using them
		finally
		{
			closeResources(inputStream, conn);
		}
	}
	
	private void closeResources(InputStream inputStream, HttpURLConnection conn) throws IOException
	{
		if (inputStream != null)
		{
			inputStream.close();
		}
		if (conn != null)
		{
			conn.disconnect();
		}
	}
	
	private void doOutput(HttpURLConnection conn, String output) throws IOException, SocketTimeoutException
	{
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Length", "" + Integer.toString(output.getBytes().length));
		conn.setRequestProperty("Content-Type", "application/xml");

		OutputStreamWriter outWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		outWriter.write(output);
		outWriter.flush();
		outWriter.close();
	}

	// Reads an InputStream and converts it to a String.
	private String readIt(InputStream stream)
	{
		try
		{
			char[] buffer = new char[StoryActivity.BUFFER_SIZE];
			int length = 0;
			String response = new String();
			Reader reader = new InputStreamReader(stream, "UTF-8");
			while (length != -1)
			{
				length = reader.read(buffer);
				if (length != -1)
				{
					response += new String(buffer, 0, length);
				}
			}
			return response;
		}
		catch (IOException e)
		{
			RequestStatus = STATUS_ERROR_STRING_CONVERT;
			return null;
		}
	}
}
