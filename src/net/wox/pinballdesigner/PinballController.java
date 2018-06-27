package net.wox.pinballdesigner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import net.wox.seagull.core.JSONParsingException;
import net.wox.seagull.core.blackboard.BlackboardName;
import net.wox.seagull.core.blackboard.IBlackboard;
import net.wox.seagull.core.entity.ui.Button;
import net.wox.seagull.core.entity.ui.HUDDefinition;
import net.wox.seagull.core.entity.ui.Slider;
import net.wox.seagull.core.entity.ui.Text;
import net.wox.seagull.core.entity.ui.UIController;
import net.wox.seagull.core.entity.ui.UIElement;
import net.wox.seagull.core.entity.ui.UISubcribe;
import net.wox.seagull.core.graphics.rendering.meshes.Mesh;
import net.wox.seagull.core.graphics.rendering.meshes.Meshes;
import net.wox.seagull.core.time.Time;
import net.wox.seagull.core.util.JSONUtils;

@HUDDefinition("Data/ui/Pinball.ui")
public class PinballController extends UIController {
	@BlackboardName("Game")
	public IBlackboard gameBlackboard;

	@BlackboardName("Window")
	public IBlackboard window;

	public static final int FRAMES_PER_SECOND = 30;
	public static final int FRAMES = 980;
	private boolean playing = false;

	private List<UIElement> leds = new ArrayList<>();
	private List<HashMap<String, Float>> playlist = new ArrayList<>();
	public int currentFrame = 0;

	@Override
	public void init() {
		super.init();

		for( int i = 0; i < FRAMES; i++ )
        {
            playlist.add( new HashMap<String, Float>() );
        }
		
		try {
			JSONArray object = JSONUtils.getArrayFromFile("Data/LEDS2.json");

			for (Object o : object) {
				JSONObject jsonObject = (JSONObject) o;
				String name = JSONUtils.getString(jsonObject, "name");
				int x = JSONUtils.getInt(jsonObject, "x");
				int y = JSONUtils.getInt(jsonObject, "y");
				String color = JSONUtils.getString(jsonObject, "color");
				
				switch( color )
				{
					case "WHITE" : color = "#FFFFFF"; break;
					case "GREEN" : color = "#00FF00"; break;
					case "RED"   : color = "#FF0000"; break;
					case "ORANGE": color = "#FFA500"; break;
					case "YELLOW": color = "#FFFF00"; break;
					default: color = "#000000"; break;
				}
				
				x *= 0.9f;
				y *= 0.9f;
				x += 450;

				Mesh[] texture = Meshes.ninePatchAtlasMesh(16, 16, "Seagull_UI", "button");
				UIElement button = addElement(new Button("LED_" + name + "_" + color, "", "Cataclysmo", 24, "#FFFFFF", x,
						(int) window.get("height") - y, 16, 16, texture, null, null));
				button.color(color);
				button.setParent(this);
				leds.add(button);
			}
		} catch (IOException | URISyntaxException | JSONParsingException | ParseException e) {
			e.printStackTrace();
		}

		Slider slider = (Slider) (getElement("slider_container").getElement("frame_slider"));
		slider.maxValue(FRAMES - 1);
		
//        LightShow.doLine( 0  , 180, new Vector2f(715, 510), new Vector2f(0, 1).normalize(), new Vector2f(0f,  0f), 4.0f, 30f, playlist, leds );
		
		Vector2f rotLineStart = new Vector2f(715, 1000);
		Vector2f rotLineEnd = new Vector2f(217.2054f, 142.27672f);
		float rotSpd = 60f / 35f;
		rotLineEnd = LightShow.doSearchLight(true, 0  , 35, rotLineStart, rotLineEnd,  rotSpd, 0f, 100f, 1000, playlist, leds);
		rotLineEnd = LightShow.doSearchLight(true, 35 , 35, rotLineStart, rotLineEnd, -rotSpd, 0f, 100f, 1000, playlist, leds);
		rotLineEnd = LightShow.doSearchLight(true, 70 , 35, rotLineStart, rotLineEnd,  rotSpd, 0f, 100f, 1000, playlist, leds);
		rotLineEnd = LightShow.doSearchLight(true, 105, 35, rotLineStart, rotLineEnd, -rotSpd, 0f, 100f, 1000, playlist, leds);
		
		Vector2f start = new Vector2f(710, 180);
		
//		Mesh[] texture = Meshes.ninePatchAtlasMesh(16, 16, "Seagull_UI", "button");
//		UIElement button = addElement(new Button("hallo", "", "Cataclysmo", 24, "#FFFFFF", start.x, start.y, 32, 32, texture, null, null));
//		button.color("#FF0000");
//		button.setParent(this);
		
		int offset = 140;
		for( int i = 0; i < 16; i++ )
		{
			LightShow.doCircle(true, offset + ( i * 15 ), 30, start, 0, 30, 50f, playlist, leds);
		}

		start = new Vector2f(710, 650);
//		Mesh[] texture = Meshes.ninePatchAtlasMesh(16, 16, "Seagull_UI", "button");
//		UIElement button = addElement(new Button("hallo", "", "Cataclysmo", 24, "#FFFFFF", start.x, start.y, 32, 32, texture, null, null));
//		button.color("#FF0000");
//		button.setParent(this);
		
//		LightShow.doSearchLight(true, 405, 120, start, start.mul( 1.2f, new Vector2f()),  6, new Vector2f(0f, -4f), 20f, 130f, 800, playlist, leds);
		rotLineEnd = LightShow.doSearchLight(true, true, 380, 120, start, start.mul(-1.2f, new Vector2f()),  -6, new Vector2f(0f, -4f), 20f, 130f, 800, playlist, leds);
		rotLineEnd = LightShow.doSearchLight(true, true, 500, 120, start, rotLineEnd,  -6, new Vector2f(0f, 3f), 20f, 130f, 800, playlist, leds);
		rotLineEnd = LightShow.doSearchLight(true, true, 620, 120, start, rotLineEnd,  -6, new Vector2f(0f, -3f), 20f, 130f, 800, playlist, leds);
		
		int curFrame = 740;
		int flashes  = 3;
		int flashOn  = 2;
		int flashOff = 3;
		for( int i = 0; i < 4; i++ )
		{
			LightShow.flashColor( curFrame, flashes, flashOn, flashOff, "#00FF00", playlist, leds); curFrame += flashes * ( flashOff + flashOn );
			LightShow.flashColor( curFrame, flashes, flashOn, flashOff, "#FFFF00", playlist, leds); curFrame += flashes * ( flashOff + flashOn );
			LightShow.flashColor( curFrame, flashes, flashOn, flashOff, "#FFFFFF", playlist, leds); curFrame += flashes * ( flashOff + flashOn );
			LightShow.flashColor( curFrame, flashes, flashOn, flashOff, "#FF0000", playlist, leds); curFrame += flashes * ( flashOff + flashOn );
		}
//		LightShow.doLine(405, 120, start, new Vector2f(0f, 1f), new Vector2f(0f, -4f), 6, 20f, playlist, leds);
		
		//		System.out.println(rotLineEnd.x + ", " + rotLineEnd + ">>");
//		rotLineEnd = LightShow.doSearchLight(true, 30, 60, rotLineStart, rotLineEnd,  1.0f, 0f, 100f, 1000, playlist, leds);
//		rotLineEnd = LightShow.doSearchLight(true, 90, 30, rotLineStart, rotLineEnd, -1.0f, 0f, 100f, 1000, playlist, leds);
//        System.out.println(" !!!! " +  rotLineEnd);
//		LightShow.doBand(100, 0, 1024, playlist, leds);
	}

	private void timerLoop()
    {
    	if( ++currentFrame >= FRAMES )
    	{
    		currentFrame = 0;
    	}
    	
    	Slider slider = (Slider) (getElement("slider_container").getElement("frame_slider"));
		slider.value(currentFrame);
    	
    	if( playing )
    	{
    		Time.plan( (long)(1000f / FRAMES_PER_SECOND), this::timerLoop);
    	}
    }
	
	private void loadFrame(int frame) {
		for( UIElement led : leds )
		{
			led.color("#000000");
		}
		
		for (Entry<String, Float> entry : playlist.get(frame).entrySet()) {
			UIElement element = getElement(entry.getKey());
			if (element != null) {
				element.color(new Vector4f(entry.getValue(), entry.getValue(), entry.getValue(), 1f));
			}
		}
	}

	@UISubcribe(element = "LED_*", event = "onClick")
	public void test(Button button) {
		Text text = (Text) (getElement("control_panel").getElement("led_name"));
		text.setText(button.name());
	}

	@UISubcribe(element = "control_panel$play_button", event = "onClick")
	public void play(Button button) {
		if (button.label().equals("PLAY")) {
			playing = true;
			button.label("PAUSE");
			timerLoop();
		} else {
			playing = false;
			button.label("PLAY");
		}
	}

	@UISubcribe(element = "control_panel$export_button", event = "onClick")
	public void export(Button button) {
		LightShow.writeLightshow(playlist);
	}

	@UISubcribe(element = "slider_container$frame_minus", event = "onClick")
	public void frameMinus(Button button) {
		Slider slider = (Slider) (getElement("slider_container").getElement("frame_slider"));
		slider.value(slider.value() - 1);
	}

	@UISubcribe(element = "slider_container$frame_plus", event = "onClick")
	public void framePlus(Button button) {
		Slider slider = (Slider) (getElement("slider_container").getElement("frame_slider"));
		slider.value(slider.value() + 1);
	}

	@UISubcribe(element = "slider_container$frame_slider", event = "onValueChange")
	public void frameChange(Slider slider, Integer value) {
		currentFrame = value;
		loadFrame(value);
	}

	@UISubcribe(element = "fps", event = "onTick")
	public void updateFPSCounter(Text fps) {
		if (Time.getTime() % Time.msToTicks(100) == 0) {
			fps.setText("FPS:\t" + gameBlackboard.get("FPS"));
		}
	}
	
    
}
