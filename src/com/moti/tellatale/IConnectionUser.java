package com.moti.tellatale;

public interface IConnectionUser
{
	/**
	 * A callback function that is called by the async task 'HttpConnectionTask' when
	 * it has finished to send/receive data to the server
	 * @param requestStatus the status the task returned with
	 * @param response the response, if any, from the server
	 */
	public void connectionFinished(int requestStatus, String response);
}
