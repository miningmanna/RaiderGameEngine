package org.rge.assets.audio;

import javax.sound.sampled.AudioInputStream;

import org.rge.assets.ALLoader;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class SoundStream implements EngineObject {
	
	private int curBuff;
	private int[] buffs;
	private byte[] lBuff;
	private boolean finished;
	
	private ALLoader loader;
	private AudioInputStream in;
	
	private EngineReference ref;
	
	public SoundStream(ALLoader loader, AudioInputStream in) {
		this.loader = loader;
		this.in = in;
		
		lBuff = new byte[4096*4*8];
		buffs = new int[3];
		curBuff = 0;
		finished = false;
		
		for(int i = 0; i < buffs.length; i++)
			buffs[i] = loader.genBuffer();
		
		ref = new EngineReference(this);
	}
	
	public int[] fillAll() {
		
		int[] ret = new int[buffs.length];
		for(int i = 0; i < buffs.length; i++) {
			if(fillBuffer(buffs[i]))
				ret[i] = buffs[i];
			else
				ret[i] = -1;
		}
		
		return ret;
	}
	
	public boolean fillBuffer(int buff) {
		if(finished)
			return false;
		
		int res = buffs[curBuff];
		System.out.println("FILL BUFFER: " + curBuff + " -> " + res);
		curBuff = (curBuff+1)%buffs.length;
		
		try {
			int l = 0;
			int read = 0;
			while(read != lBuff.length) {
				l = in.read(lBuff, read, lBuff.length-read);
				if(l < 0)
					break;
				read += l;
			}
			if(l < 0) {
				finished = true;
				in.close();
				return false;
			}
			
			System.out.println("LOADING BUFFER");
			
			loader.bufferData(res, lBuff, (int) in.getFormat().getSampleRate());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Filled!");
		return true;
	}
	
	public void close() {
		try {
			if(in != null) {
				finished = true;
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getBufferCount() {
		return buffs.length;
	}

	public boolean finished() {
		return finished;
	}

	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
}
