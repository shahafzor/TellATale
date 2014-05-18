package com.moti.tellatale;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

public class Story
{
	private String Name;
	private List<StorySegment> StorySegmentList;
	private int LastSeqNumber = 0;
	private int LastSeqNumberLocation = 0;
	private int CurrentLastSeqNumberLocation = 0;
	
	public Story()
	{
		StorySegmentList = new ArrayList<StorySegment>();
	}
	
	public Story(StorySegment segment)
	{
		this();
		if (segment != null)
		{
			StorySegmentList.add(segment);
		}
	}
	
	public Story(String name)
	{
		this();
		Name = name;
	}
	
	public Story(StorySegment segment, String name)
	{
		this(segment);
		Name = name;
	}
	
	public String getName()
	{
		return Name;
	}
	
	/**
	 * Returns the story segment on location 'index'
	 * @param index
	 * @return The requested StorySegment on success, null on error
	 */
	public StorySegment getStorySegment(int index)
	{
		try
		{
			return StorySegmentList.get(index);
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	public int getCurrentLastSeqNumberLocation()
	{
		return CurrentLastSeqNumberLocation;
	}
	
	public void setCurrentLastSeqNumberLocation(int index)
	{
		if (index > CurrentLastSeqNumberLocation && index < getStorySegmentCount())
		{
			CurrentLastSeqNumberLocation = index;
		}
	}
	
	public boolean addStorySegment(StorySegment segment)
	{
		if (segment != null)
		{
			try
			{
				StorySegmentList.add(segment);
				if (segment.getSeqNumber() > LastSeqNumber)
				{
					LastSeqNumber = segment.getSeqNumber();
					LastSeqNumberLocation = StorySegmentList.size() - 1;
					CurrentLastSeqNumberLocation = LastSeqNumberLocation;
				}
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}
		return false;
	}
	
	public int getStorySegmentCount()
	{
		return StorySegmentList.size();
	}
	
	/**
	 * Returns an iterator for the story segment list
	 * @return Iterator
	 */
	public Iterator<StorySegment> getIterator()
	{
		return StorySegmentList.iterator();
	}
	
	public String toXml()
	{
	    XmlSerializer serializer = Xml.newSerializer();
	    StringWriter writer = new StringWriter();
	    try
	    {
	        serializer.setOutput(writer);
	        serializer.startDocument("UTF-8", true);
	        serializer.startTag("", "story");
	        String name = getName();
	        if (name != null)
	        {
	        	serializer.attribute("", "name", name);
	        }

	        Iterator<StorySegment> it = getIterator();
	        while (it.hasNext())
	        {
	        	StorySegment segment = it.next();
	        	segment.toXml(serializer);
	        }
	        serializer.endTag("", "story");
	        serializer.endDocument();
	        
	        return writer.toString();
	    }
	    catch (Exception e)
	    {
	        return null;
	    } 
	}
	
	public StorySegment getLastSegment()
	{
		return getStorySegment(getStorySegmentCount() - 1);
	}
	
	StorySegment createNewSegment(String text, String username)
	{
		StorySegment newSegment = null;
		StorySegment segment = getCurrentSegment();
		if (segment != null)
		{
			int seqNumber = segment.getNextSeqNumber();
			newSegment = new StorySegment(text, seqNumber, username);
			newSegment.setVersion(segment.getVersion());
			
			// if the last segment had more than one version, the ones that were
			// not chosen will be dropped
			if (getStorySegmentCount() - LastSeqNumberLocation > 1)
			{
				newSegment.setNeedDrop();
			}
		}
		return newSegment;
	}
	
	StorySegment createparallelSegment(String text, String username)
	{
		StorySegment newSegment = null;
		StorySegment segment = getLastSegment();
		if (segment != null)
		{
			int seqNumber = segment.getSeqNumber();
			newSegment = new StorySegment(text, seqNumber, username);
			newSegment.setVersion(segment.getNextVersion());
			newSegment.setIsParallel();
		}
		return newSegment;
	}
	
	/**
	 * Returns the story segment that is currently on focus from all the story segments
	 * that share the same last sequence number
	 * @return The requested StorySegment on success, null on error
	 */
	public StorySegment getCurrentSegment()
	{
		return getStorySegment(CurrentLastSeqNumberLocation);
	}
	
	/**
	 * Move the focus to the next segment from all the story segments that share
	 * the same last sequence number
	 * @return the next segment, null on error
	 */
	public StorySegment getNextSegment()
	{
		if (CurrentLastSeqNumberLocation < getStorySegmentCount() - 1)
		{
			CurrentLastSeqNumberLocation++;
			return getCurrentSegment();
		}
		
		return null;
	}
	
	/**
	 * Move the focus to the previous segment from all the story segments that share
	 * the same last sequence number
	 * @return the previous segment, null on error
	 */
	public StorySegment getPrevSegment()
	{
		if (CurrentLastSeqNumberLocation > LastSeqNumberLocation)
		{
			CurrentLastSeqNumberLocation--;
			return getCurrentSegment();
		}
		
		return null;
	}
	
	
	/**
	 * Returns the "real" story, i.e the text of the story object
	 * @return The story as a String, empty String if there are no story segments
	 */
	public String getText()
	{
		String storyText = "";
		Iterator<StorySegment> it = getIterator();
		while (it.hasNext())
		{
			StorySegment segment = it.next();
			if (segment.getSeqNumber() == LastSeqNumber)
			{
				// don't add the last segment
				break;
			}
			if (!segment.isDropped()) //don't add dropped segment
			{
				storyText += segment.getText() + "\n";
			}
		}
		return storyText;
	}
	
	@Override
	public String toString()
	{
		String storyText = "";
		Iterator<StorySegment> it = getIterator();
		while (it.hasNext())
		{
			StorySegment segment = it.next();
			if (!segment.isDropped()) //don't add dropped segment
			{
				storyText += segment.getText() + "\n";
			}
		}
		return storyText;
	}
}
