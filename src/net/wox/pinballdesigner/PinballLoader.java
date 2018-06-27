package net.wox.pinballdesigner;
import net.wox.seagull.core.SeagullEngine;

public class PinballLoader 
{
	public static void main( String... args )
	{
		new SeagullEngine(new PinballController()).run(1280, 1024, "Pinball Designer");
	}
}
