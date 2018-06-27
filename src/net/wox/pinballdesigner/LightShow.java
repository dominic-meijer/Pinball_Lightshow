package net.wox.pinballdesigner;

import java.awt.geom.Line2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.joml.Vector2f;

import net.wox.seagull.core.entity.ui.UIElement;

public class LightShow 
{
	public static Vector2f doSearchLight( boolean hard, int startFrame, int duration, Vector2f lineStart, Vector2f lineEnd, float rotPerFrameDegrees, float bandWidthStart, float bandWidthEnd, float maxDist, List<HashMap<String, Float>> playlist, List<UIElement> leds )
	{
		return doSearchLight(hard, false, startFrame, duration, lineStart, lineEnd, rotPerFrameDegrees, new Vector2f(), bandWidthStart, bandWidthEnd, maxDist, playlist, leds);
	}
	
	public static Vector2f doSearchLight( boolean hard, boolean keep, int startFrame, int duration, Vector2f lineStart, Vector2f lineEnd, float rotPerFrameDegrees, Vector2f movement, float bandWidthStart, float bandWidthEnd, float maxDist, List<HashMap<String, Float>> playlist, List<UIElement> leds )
    {
		if( !keep )
		{
			lineStart = new Vector2f(lineStart);
			lineEnd   = new Vector2f(lineEnd  );
		}
		Vector2f rotLineEnd = new Vector2f(lineEnd);
    	for( int i = startFrame; i < startFrame + duration; i++ )
    	{
    		if( i < 0 || i >= PinballController.FRAMES ) { continue; }
    		
    		rotLineEnd = pivot( rotLineEnd, lineStart, rotPerFrameDegrees );
	    	for(  UIElement led : leds )
	    	{
	    		float distance = distanceToLine(lineStart, rotLineEnd, new Vector2f( led.position().x, led.position().y ) );
	    		
	    		Vector2f closestPoint = getClosestPointOnSegment(lineStart, rotLineEnd, new Vector2f( led.position().x, led.position().y ) );
	    		float distanceToLineStart = closestPoint.distance(lineStart);
	    		if( distanceToLineStart < 1f )
	    		{
	    			closestPoint = getClosestPointOnSegment(lineStart, rotLineEnd.mul(-1, new Vector2f()), new Vector2f( led.position().x, led.position().y ) );
	    			distanceToLineStart = closestPoint.distance(lineStart);
	    		}
	    		
	    		float bandWidthRange      = bandWidthEnd - bandWidthStart;
	    		float finalBandWidth      = bandWidthStart + ( (distanceToLineStart / maxDist) * bandWidthRange );
	    		
	    		float brightness = Math.min( 1.0f - ( finalBandWidth ), 1f );
	    		
	    		if( hard )
	    		{
		    		if( distance < finalBandWidth ) 
		    		{
		    			brightness = 1f;
		    		}
		    		else
		    		{
		    			brightness = 0f;
		    		}
	    		}
	    		
				if( brightness > 0.1f && brightness <= 1f )
	    		{
					float prevBrightness = playlist.get(i).getOrDefault(led.name(), 0f);
	    			playlist.get(i).put(led.name(), Math.max( Math.max( brightness, 0 ), prevBrightness) );
	    		}
	    		else if ( brightness <= 0.1f )
	    		{
	    			float prevBrightness = playlist.get(i).getOrDefault(led.name(), 0f);
	    			playlist.get(i).put(led.name(), prevBrightness);
	    		}
	    	}
	    	
	    	lineStart.add(movement);
	    	lineEnd  .add(movement);
    	}
    	
    	return rotLineEnd;
    }
	
	public static void flashColor( int frameStart, int pulses, int pulseFramesOn, int pulseFramesOff, String color, List<HashMap<String, Float>> playlist, List<UIElement> leds )
	{
		for(UIElement led : leds)
		{
			String ledColor = led.name().substring( led.name().lastIndexOf("_") + 1 );
			
			int frameStep = pulseFramesOn  + pulseFramesOff;
			
			if( ledColor.equalsIgnoreCase(color) )
			{	
				for( int j = 0; j < pulses * frameStep; j += frameStep )
				{
					for( int i = j + frameStart; i < j + frameStart + pulseFramesOn ; i++ )
					{
						if( i < 0 || i >= PinballController.FRAMES ) { continue; }
						playlist.get(i).put(led.name(), 1f);
					}
					
					for( int i = j + frameStart + pulseFramesOn; i < j + frameStart + pulseFramesOn + pulseFramesOff; i++ )
					{
						if( i < 0 || i >= PinballController.FRAMES ) { continue; }
						playlist.get(i).put(led.name(), 0f);
					}
				}
			}
		}
	}
	
	private static Vector2f pivot( Vector2f position, Vector2f origin, float angle )
	{
		Vector2f newPos = new Vector2f(position);
		
		double x1 = newPos.x - origin.x;
		double y1 = newPos.y - origin.y;

		//APPLY ROTATION
		x1 = x1 * Math.cos(Math.toRadians(angle)) - y1 * Math.sin(Math.toRadians(angle));
		y1 = x1 * Math.sin(Math.toRadians(angle)) + y1 * Math.cos(Math.toRadians(angle));

		//TRANSLATE BACK
		newPos.x = (float) (x1 + origin.x);
		newPos.y = (float) (y1 + origin.y);
		
		return newPos;
	}
	
	public static void doLine( int startFrame, int duration, Vector2f position, Vector2f startDir, Vector2f move, float rotPerFrameDegrees, float bandWidth, List<HashMap<String, Float>> playlist, List<UIElement> leds )
    {
    	for( int i = startFrame; i < startFrame + duration; i++ )
    	{
    		if( i < 0 || i >= PinballController.FRAMES ) { continue; }

	    	Vector2f lineStart = position.add(startDir.mul(-1f, new Vector2f()), new Vector2f());
	    	Vector2f lineEnd   = position.add(startDir.mul( 1f, new Vector2f()), new Vector2f());
	    	
	    	for(  UIElement led : leds )
	    	{
	    		float distance = distanceToLine(lineStart, lineEnd, new Vector2f( led.position().x, led.position().y ) );
	    		
	    		float brightness = 1.0f - ( distance / bandWidth );
	    		
				if( brightness > 0.1f && brightness <= 1f )
	    		{
	    			playlist.get(i).put(led.name(), Math.max( brightness, 0 ));
	    		}
	    		else if ( brightness <= 0.1f )
	    		{
	    			playlist.get(i).put(led.name(), 0f);
	    		}
	    	}
	    	
	    	Vector2f rotated = rotateVector( startDir, -rotPerFrameDegrees );
	    	startDir.x = rotated.x;
	    	startDir.y = rotated.y;
	    	position.x += move.x;
	    	position.y += move.y;
    	}
    }
	
	public static void doCircle( boolean hard, int startFrame, int duration, Vector2f position, float radius, float radiusDelta, float bandWidth, List<HashMap<String, Float>> playlist, List<UIElement> leds )
    {
    	for( int i = startFrame; i < startFrame + duration; i++ )
    	{
    		if( i < 0 || i >= PinballController.FRAMES ) { continue; }

	    	for(  UIElement led : leds )
	    	{
	    		float distanceX  = Math.abs( (position.x - led.position().x) );
	    		float distanceY  = Math.abs( (position.y - ( led.position().y * 0.6f ) ) );
	    		float distance   = (float) (radius - Math.sqrt(((distanceX * distanceX) + (distanceY * distanceY))));
	    		float brightness = 1.0f - ( Math.abs(distance) / bandWidth );
	    		
	    		if( hard )
	    		{
		    		if( brightness > 0.1f ) { brightness = 1f; }
		    		else                    { brightness = 0f; }
	    		}
	    		
				if( brightness > 0.1f && brightness <= 1f )
	    		{
	    			playlist.get(i).put(led.name(), Math.max( brightness, 0 ));
	    		}
	    		else if ( brightness <= 0.1f )
	    		{
	    			playlist.get(i).put(led.name(), 0f);
	    		}
	    	}
	    	
	    	radius += radiusDelta;
    	}
    }
    
    public static void doBand( float bandWidth, float startY, float travelDistance, List<HashMap<String, Float>> playlist, List<UIElement> leds )
    {
    	float checkY = startY;
        int   step   = (int)(travelDistance / ( PinballController.FRAMES / 2f ) );
        for( int i = 0; i < PinballController.FRAMES; i++ )
        {
        	checkY += i < PinballController.FRAMES / 2f ? +step : -step;
        	
        	for( UIElement led : leds )
        	{
        		float brightness = 1.0f - ( Math.abs( checkY - led.position().y ) / bandWidth );
        		
        		if( brightness > 0.1f && brightness <= 1f )
        		{
        			playlist.get(i).put(led.name(), Math.max( brightness, 0 ));
        		}
        		else if ( brightness <= 0.1f )
        		{
        			playlist.get(i).put(led.name(), 0f);
        		}
        	}
        }
    }

    public static Vector2f getClosestPointOnSegment(Vector2f lineStart, Vector2f lineEnd, Vector2f posiiton)
    {
      return getClosestPointOnSegment(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y, posiiton.x, posiiton.y);
    }

    public static Vector2f getClosestPointOnSegment(float sx1, float sy1, float sx2, float sy2, float px, float py)
    {
      double xDelta = sx2 - sx1;
      double yDelta = sy2 - sy1;

      if ((xDelta == 0) && (yDelta == 0))
      {
        throw new IllegalArgumentException("Segment start equals segment end");
      }

      double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

      final Vector2f closestPoint;
      if (u < 0)
      {
        closestPoint = new Vector2f(sx1, sy1);
      }
      else if (u > 1)
      {
        closestPoint = new Vector2f(sx2, sy2);
      }
      else
      {
        closestPoint = new Vector2f((int) Math.round(sx1 + u * xDelta), (int) Math.round(sy1 + u * yDelta));
      }

      return closestPoint;
    }
    
    private static float distanceToLine( Vector2f lineStart, Vector2f lineEnd, Vector2f position )
    {
    	lineStart = new Vector2f(lineStart);
    	lineEnd   = new Vector2f(lineEnd  );
    	position  = new Vector2f(position );
    	
    	Line2D line = new Line2D.Double(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
    	return (float) line.ptLineDist(position.x, position.y);
    }
    
    private static Vector2f rotateVector( Vector2f direction, float degrees )
    {
    	float sin = (float) Math.sin(Math.toRadians(degrees));
    	float cos = (float) Math.cos(Math.toRadians(degrees));
    	
    	return new Vector2f( (cos * direction.x) - (sin * direction.y), (sin * direction.x) + (cos * direction.y) );
    }
    
	public static void writeLightshow( List<HashMap<String, Float>> playlist )
	{
		try( BufferedWriter writer = new BufferedWriter( new FileWriter( new File("lightshow.yaml") ) ) )
		{
			int emptyFrames = 0;
			HashMap<String, Integer> previousValues = new HashMap<>();
			for( int i = 0; i < playlist.size(); i++ )
			{				
				if( playlist.get(i).isEmpty() )
				{
					emptyFrames++;
				}
				else
				{
					String output = "- tocks: " + (emptyFrames + 1) + "\n";
					output += "  LEDs:\n";
					
					boolean hasData = false;
					for( Entry<String, Float> led : playlist.get(i).entrySet() )
					{
						int value =   (int) (255f * led.getValue() ) << 16
									| (int) (255f * led.getValue() ) << 8
									| (int) (255f * led.getValue() );
						
						String ledKey = led.getKey().substring(4, led.getKey().lastIndexOf("_") );
						if( previousValues.get(ledKey) == null || previousValues.get(ledKey) != value )
						{
							hasData = true;
							output += "    " + ledKey + ": " + String.format( "%06x", value ) + "\n";
						}
						
						previousValues.put( ledKey, value);
					}
					output += "\n";
					
					if ( hasData )
					{
						writer.write(output);
						emptyFrames = 0;
					}
					else
					{
						emptyFrames++;
					}
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
