package com.moti.tellatale;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownServiceException;

import android.os.AsyncTask;

//TODO: status codes ?

class HttpConnectionTask extends AsyncTask<String, Void, String>
{
	public static final int STATUS_XML_OK = 300;
	public static final int STATUS_NO_STORY_AVAILABLE = 301;
	public static final int STATUS_RESPONSE_OK = 302;
	public static final int STATUS_SERVER_ERROR = 303;
	public static final int STATUS_APP_ERROR = 304;
	public static final int STATUS_ERROR_XML_PARSE = 305;
	public static final int STATUS_ERROR_STRING_CONVERT = 306;
	public static final int STATUS_ERROR_CREDENTIALS = 307;
	public static final int STATUS_ERROR_TIMEOUT = 308;
	
	private StoryActivity ParentActivity;
	private int RequestStatus = STATUS_APP_ERROR;

	public HttpConnectionTask(StoryActivity activity)
	{
		this.ParentActivity = activity;
	}

	@Override
	protected String  doInBackground(String... params)
	{
		// params comes from the execute() call: params[0] is the url params[1] is the output.
		try
		{
			return sendRequest(params[0], params[1], params[2]);
		}
		catch (SocketTimeoutException e)
		{
			RequestStatus = STATUS_ERROR_TIMEOUT;
			return null;
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	protected void onPostExecute(String response)
	{
        ParentActivity.connectionFinished(RequestStatus, response);
    }

	private String sendRequest(String serverUrl, String output, String type) throws IOException, SocketTimeoutException, MalformedURLException
	{
		InputStream inputStream = null;
		HttpURLConnection conn = null;
		
		try
		{
			try{
				URL url = new URL(serverUrl);
				conn = (HttpURLConnection) url.openConnection();
			} catch(MalformedURLException e){
				RequestStatus = 1;
				return null;
			}
			catch(IOException e){
				RequestStatus = 2;
				return null;
			}
			conn.setReadTimeout(35000 /* milliseconds */);
	        conn.setConnectTimeout(10000 /* milliseconds */);
			conn.setDoInput(true);
			
			if (output != null)
			{
				doOutput(conn, output, type);
				if (RequestStatus != STATUS_APP_ERROR)
					return null;
			}

			try{
				inputStream = conn.getInputStream();
			} catch(SocketTimeoutException e){
				throw e;
			} catch(UnknownServiceException e){
				RequestStatus = 3;
				return e.getMessage();
			}catch(IOException e){
				RequestStatus = 4;
				return e.getMessage();
			}
			catch(Exception e){
				RequestStatus = 10;
				return e.getMessage();
			}
			
			RequestStatus = conn.getResponseCode();
			String response = null;
			if (RequestStatus == STATUS_XML_OK)
			{
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
	
	private void doOutput(HttpURLConnection conn, String output, String type) throws IOException, SocketTimeoutException
	{
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Length", "" + Integer.toString(output.getBytes().length));
		conn.setRequestProperty("Content-Language", "en-US");
		conn.setUseCaches(false);
		if (type == "xml")
		{
			conn.setRequestProperty("Content-Type", "application/xml");
		}

		OutputStreamWriter outWriter = null;
		OutputStream outStream = null;

		try{
			outStream = conn.getOutputStream();
		} catch(SocketTimeoutException e){
			throw e;
		} catch(Exception e){
			RequestStatus = 5;
		}

		try{
			outWriter = new OutputStreamWriter(outStream, "UTF-8");
		} catch(Exception e){
			RequestStatus = 6;
		}

		try{
			outWriter.write(output);
		} catch(Exception e){
			RequestStatus = 7;
		}
		
		try{
			outWriter.flush();
		} catch(Exception e){
			RequestStatus = 8;
		}

		try{
			outWriter.close();
		}  catch(Exception e){
			RequestStatus = 9;
		}
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