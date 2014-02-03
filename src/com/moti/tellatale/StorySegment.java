package com.moti.tellatale;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

public class StorySegment
{
	private int SeqNumber = 1;
	private int Version = 0;
	private String Text;
	private int Dislike = 0;
	private String UserName;
	private String Password;
	private boolean IsParallel = false;
	private boolean Dropped = false;
	private boolean NeedDrop = false;
	
	public StorySegment(String text, String username)
	{
		Text = text;
		UserName = username;
	}
	
	public StorySegment(String text, int seqNumber, String username)
	{
		this(text, username);
		setSeqNumber(seqNumber);
	}
	
	public StorySegment(String text, String seqNumber, String username)
	{
		this(text, username);
		setSeqNumber(seqNumber);
	}

	public boolean setSeqNumber(String number)
	{
		try
		{
			SeqNumber = Integer.parseInt(number);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	public void setSeqNumber(int number)
	{
		SeqNumber = number;
	}
	
	public void setPassword(String password)
	{
		Password = password;
	}
	
	public String getPassword()
	{
		return Password;
	}
	
	public int getVersion()
	{
		return Version;
	}
	
	public int getDislike()
	{
		return Dislike;
	}
	
	public void setVersion(int version)
	{
		Version = version;
	}
	
	public boolean setVersion(String version)
	{
		try
		{
			Version = Integer.parseInt(version);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	public void setDropped()
	{
		Dropped = true;
	}
	
	public boolean isDropped()
	{
		return Dropped;
	}
	
	public void setUserName(String name)
	{
		UserName = name;
	}
	
	public void setText(String text)
	{
		Text = text;
	}
	
	public int getSeqNumber()
	{
		return SeqNumber;
	}
	
	public String getText()
	{
		return Text;
	}
	
	public void setIsParallel()
	{
		IsParallel = true;
	}
	
	public boolean isParallel()
	{
		return IsParallel;
	}
	
	public String getUserName()
	{
		return UserName ;
	}
	
	public int getNextVersion()
	{
		return ++Version;
	}
	
	public int getNextSeqNumber()
	{
		return ++SeqNumber;
	}
	
	public void setNeedDrop()
	{
		NeedDrop = true;
	}
	
	public void toXml(XmlSerializer serializer) throws IOException
	{
		serializer.startTag("", "story_segment");
    	serializer.startTag("", "seq_number");
    	serializer.text(Integer.toString(getSeqNumber()));
    	serializer.endTag("", "seq_number");
    	serializer.startTag("", "version");
    	serializer.text(Integer.toString(getVersion()));
    	serializer.endTag("", "version");
    	serializer.startTag("", "text");
    	serializer.text(getText());
    	serializer.endTag("", "text");
    	serializer.startTag("", "user_name");
    	serializer.text(getUserName());
    	serializer.endTag("", "user_name");
    	if (getPassword() != null)
    	{
    		serializer.startTag("", "password");
    		serializer.text(getPassword());
    		serializer.endTag("", "password");
    	}
    	if (isParallel())
    	{
    		serializer.startTag("", "parallel");
    		serializer.endTag("", "parallel");
    	}
    	if (isDropped())
    	{
	    	serializer.startTag("", "dropped");
	    	serializer.endTag("", "dropped");
    	}
    	if (NeedDrop)
    	{
	    	serializer.startTag("", "need_drop");
	    	serializer.endTag("", "need_drop");
    	}
    	serializer.endTag("", "story_segment");
	}
}