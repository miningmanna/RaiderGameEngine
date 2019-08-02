package org.rge.assets.models;

import java.io.IOException;

import org.rge.assets.AssetManager;
import org.rge.graphics.Shader;

public class Model {
	
	AssetManager am;
	
	public RawData raw;
	public Shader shader;
	public int[] usedVBOs;
	public int vao;
	
	public Model(AssetManager am, RawData data, boolean keepData) {
		
		this.am = am;
		if(keepData)
			this.raw = data;
		
		am.loadModel(this);
		
	}
	
	public void setShader(String shaderPath) {
		try {
			shader = am.getShader(shaderPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class RawData {
		
		public String shaderName;
		public String[] texs;
		public Verts[] verts;
		public int[] rawInds;
		
		public static class Verts {
			
			public int dimension;
			public float[] rawVerts;
			
		}
		
	}
}
