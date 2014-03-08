package com.moti.tellatale;

public class InputValidation
{
	public static boolean validateUserName(String username)
	{
		return username.matches("[a-zA-Z0-9]{3,12}");
	}
	
	public static boolean validatePassword(String password)
	{
		return password.matches("[a-zA-Z0-9]{6,12}");
	}
}
