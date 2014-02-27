package com.moti.tellatale;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * This class parses XML stories.
 * Given an InputStream representation of a story, it returns a List of story segments,
 * where each list element represents a single segment in the XML story.
 */
public class XmlParser
{
    private static final String ns = null;
    
    private XmlPullParser initParser(String in) throws XmlPullParserException, IOException
    {
    	XmlPullParser parser = Xml.newPullParser();
    	parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    	parser.setInput(new StringReader(in));
    	parser.nextTag();
    	return parser;
    }

    public Story parseStory(String in) throws XmlPullParserException, IOException
    {
    	XmlPullParser parser = initParser(in);
    	return readStory(parser);
    }
    
    public List<Story> parseStories(String in) throws XmlPullParserException, IOException
    {
    	XmlPullParser parser = initParser(in);
    	return readStories(parser);
    }
    
    private List<Story> readStories(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "stories");
        List<Story> stories = new ArrayList<Story>();
        while (parser.next() != XmlPullParser.END_TAG)
        {
            stories.add(readStory(parser));
        }
        return stories;
    }

    private Story readStory(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "story");
        String storyName = parser.getAttributeValue(ns, "name");
        Story story = new Story(storyName);
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("story_segment"))
            {
            	story.addStorySegment(readStorySegment(parser));
            }
            else
            {
                skip(parser);
            }
        }
        return story;
    }


    // Parses the contents of a story segment. If it encounters a text or seq_number tag, hands them
    // off to their respective 'read' methods for processing. Otherwise, skips the tag.
    private StorySegment readStorySegment(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, "story_segment");
        String text = null;
        String seqNumber = null;
        String userName = null;
        String version = null;
        boolean dropped = false;
        while ((parser.next()) != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals("text"))
            {
                text = readSegmentText(parser);
            }
            else if (name.equals("seq_number"))
            {
                seqNumber = readSeqNumber(parser);
            }
            else if (name.equals("user_name"))
            {
            	userName = readUserName(parser);
            }
            else if (name.equals("version"))
            {
            	version = readVersion(parser);
            }
            else if (name.equals("dropped"))
            {
            	dropped = true;
            	parser.next();
            }
            else
            {
                skip(parser);
            }
        }
        StorySegment segment = new StorySegment(text, seqNumber, userName);
        segment.setVersion(version);
        if (dropped)
        {
        	segment.setDropped();
        }
        return segment;
    }

    // Processes text tags in the story segment.
    private String readSegmentText(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "text");
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "text");
        return text;
    }

    // Processes seq_number tags in the story segment.
    private String readSeqNumber(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "seq_number");
        String seqNumber = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "seq_number");
        return seqNumber;
    }
    
 // Processes version tags in the story segment.
    private String readVersion(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "version");
        String version = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "version");
        return version;
    }
    
 // Processes user_name tags in the story segment.
    private String readUserName(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, ns, "user_name");
        String userName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "user_name");
        return userName;
    }

    // For the tags that have text values, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT)
        {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        if (parser.getEventType() != XmlPullParser.START_TAG)
        {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next())
            {
            case XmlPullParser.END_TAG:
                    depth--;
                    break;
            case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}