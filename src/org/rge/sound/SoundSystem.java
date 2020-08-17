package org.rge.sound;

import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER;

import java.util.ArrayList;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class SoundSystem implements EngineObject {
	
	private EngineReference ref;
	
	private long context;
	private long device;
	
	private ArrayList<Integer> sourceIds;
	
	private ArrayList<Source> streams;
	
	public SoundSystem() {
		sourceIds = new ArrayList<>();
		streams = new ArrayList<>();
		
		initOpenAL();
		
		initLuaTable();
	}
	
	private void initOpenAL() {
		String soundDefDevice = alcGetString(0, ALC_DEFAULT_ALL_DEVICES_SPECIFIER);
		device = alcOpenDevice(soundDefDevice);
		
		int[] attribs = {0};
		context = alcCreateContext(device, attribs);
		alcMakeContextCurrent(context);
		
		ALCCapabilities	alcCap	= ALC.createCapabilities(device);
		ALCapabilities	alCap	= AL.createCapabilities(alcCap);
		
		if(!alCap.OpenAL10) {
			System.err.println("No OpenAL 1.0 capabilities!");
			System.exit(-1);
		}
	}
	
	public void update() {
		
		for(Source s : streams)
			s.update();
		
	}
	
	public void registerSourceStreamUpdate(Source stream) {
		streams.add(stream);
	}
	
	public void removeSourceStreamUpdate(Source stream) {
		streams.remove(stream);
	}
	
	private void initLuaTable() {
		ref = new EngineReference(this);
		
		
	}
	
	public int genSource() {
		int id = AL10.alGenSources();
		sourceIds.add(id);
		return id;
	}
	
	public void destroySource(int id) {
		AL10.alDeleteSources(id);
	}
	
	public void destroy() {
		
		for(int id : sourceIds)
			destroySource(id);
		
		ALC10.alcDestroyContext(context);
		ALC10.alcCloseDevice(device);
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
}
