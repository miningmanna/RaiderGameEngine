package org.rge.assets;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.rge.assets.audio.SoundClip;
import org.rge.assets.audio.SoundClip.RawSoundClip;
import org.rge.assets.audio.SoundStream;

public class ALLoader {
	
	private ArrayList<Integer> buffers;
	
	public ALLoader() {
		buffers = new ArrayList<>();
	}
	
	public SoundClip loadRawSoundClip(RawSoundClip raw) {
		
		
		int buffer = genBuffer();
		bufferData(buffer, raw.data, raw.sampleRate);
		
		//int realSize = alGetBufferi(buffer, AL_SIZE);
		SoundClip c = new SoundClip();
		c.buffer = buffer;
		
		SoundClip sc = new SoundClip();
		sc.buffer = buffer;
		return sc;
	}
	
	public void bufferData(int buffer, byte[] data, int sampleRate) {
		ByteBuffer buff = BufferUtils.createByteBuffer(data.length).put(data);
		buff.flip();
		AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, buff, (int) sampleRate);
	}
	
	public SoundStream loadSoundStream(AudioInputStream in) {
		return new SoundStream(this, in);
	}
	
	public int genBuffer() {
		int id = AL10.alGenBuffers();
		buffers.add(id);
		return id;
	}
	
	public void destroy() {
		for(int id : buffers)
			destroyBuffer(id);
	}
	
	private void destroyBuffer(int id) {
		AL10.alDeleteBuffers(id);
	}
	
}
