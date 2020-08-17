package org.rge.assets.audio;

import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class SoundClip implements EngineObject {
	
	private EngineReference ref;
	
	public SoundClip() {
		ref = new EngineReference(this);
	}
	
	public int buffer;
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
	public static class RawSoundClip {
		
		public int sampleRate;
		public byte[] data;
		
	}
	
}
