package org.rge.sound;

import static org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_STOPPED;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceQueueBuffers;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourceUnqueueBuffers;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;
import org.rge.assets.audio.SoundClip;
import org.rge.assets.audio.SoundStream;

public class Source implements EngineObject {
	
	private EngineReference ref;
	
	private SoundSystem soundSystem;
	private int sourceId;
	private SoundStream stream;
	
	public Source(SoundSystem soundSystem) {
		this.soundSystem = soundSystem;
		
		sourceId = soundSystem.genSource();
		
		initLuaTable();
	}
	
	public void play(SoundClip clip) {
		if(clip == null) {
			alSourceStop(sourceId);
			return;
		}
		alSourceStop(sourceId);
		alSourceUnqueueBuffers(sourceId);
		alSourceQueueBuffers(sourceId, clip.buffer);
		alSourcePlay(sourceId);
		
	}
	
	public void play(SoundStream stream) {
		this.stream = stream;
		int[] buffs = stream.fillAll();
		for(int i = 0; i < buffs.length; i++)
			if(buffs[i] != -1)
				alSourceQueueBuffers(sourceId, buffs[i]);
		soundSystem.registerSourceStreamUpdate(this);
		alSourcePlay(sourceId);
	}
	
	public void stop() {
		alSourceStop(sourceId);
	}
	
	public void update() {
		if(!stream.finished()) {
			if(alGetSourcei(sourceId, AL_SOURCE_STATE) != AL_STOPPED) {
				while(alGetSourcei(sourceId, AL_BUFFERS_PROCESSED) != 0) {
					System.out.println("UPDATE");
					int b = alSourceUnqueueBuffers(sourceId);
					if(stream.fillBuffer(b))
						alSourceQueueBuffers(sourceId, b);
					else
						break;
				}
			} else {
				int[] buffs = new int[alGetSourcei(sourceId, AL_BUFFERS_PROCESSED)];
				alSourceUnqueueBuffers(sourceId, buffs);
				for(int i = 0; i < buffs.length; i++) {
					if(stream.fillBuffer(buffs[i]))
						alSourceQueueBuffers(sourceId, buffs[i]);
					else
						break;
				}
				alSourcePlay(sourceId);
			}
		}
	}
	
	private void initLuaTable() {
		
		ref = new EngineReference(this);
		
		ref.set("play", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(!(arg0 instanceof EngineReference))
					return ref;
				
				EngineReference argRef = (EngineReference) arg0;
				if(argRef.parent instanceof SoundClip) {
					System.out.println("Playing!");
					play((SoundClip) argRef.parent);
				}
				if(argRef.parent instanceof SoundStream) {
					System.out.println("Playing!");
					play((SoundStream) argRef.parent);
				}
				
				
				return ref;
			}
		});
		
		ref.set("stop", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				stop();
				return ref;
			}
		});
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
}
