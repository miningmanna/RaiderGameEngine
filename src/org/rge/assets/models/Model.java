package org.rge.assets.models;

import java.io.IOException;

import org.joml.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.rge.assets.AssetManager;
import org.rge.assets.models.Model.RawData.RawSurface;
import org.rge.graphics.Shader;

public class Model {
	
	AssetManager am;
	
	public RawData raw;
	public Shader shader;
	public Surface[] surfs;
	public int[] usedVBOs;
	public int vao;
	public int indCount;
	
	public Model(AssetManager am, RawData data, boolean keepData) throws IOException {
		
		this.am = am;
		raw = data;
		
		am.loadModel(this);
		shader = am.getShader(data.shaderName);
		
		surfs = new Surface[data.surfaces.length];
		for(int i = 0; i < surfs.length; i++) {
			RawSurface rawSurf = data.surfaces[i];
			Surface surf = new Surface();
			surfs[i] = surf;
			
			surf.par = this;
			surf.indOffset = rawSurf.indOffset;
			surf.indLength = rawSurf.indLength;
			surf.isTranslucent = rawSurf.isTranslucent;
			surf.additiveColor = rawSurf.additiveColor;
			surf.midPoint = rawSurf.midPoint;
			surf.tex = am.loadTexture(am.getTextureRaw(rawSurf.tex));
		}
		
		if(!keepData)
			this.raw = null;
		
	}
	
	public void setShader(String shaderPath) {
		try {
			shader = am.getShader(shaderPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class Surface {
		
		public Model par;
		public int indOffset;
		public int indLength;
		public Texture tex;
		public boolean isTranslucent;
		public boolean additiveColor;
		public boolean doubleSided;
		public Vector3f midPoint;
		
	}
	
	public static class RawData {
		
		public String shaderName;
		public RawSurface[] surfaces;
		public Verts[] verts;
		public int[] rawInds;
		
		public static class RawSurface {
			
			public int indOffset;
			public int indLength;
			public String tex;
			public boolean isTranslucent;
			public boolean additiveColor;
			public boolean doubleSided;
			public Vector3f midPoint;
			
		}
		
		public static class Verts {
			
			public int pos;
			public int dimension;
			public float[] rawVerts;
			
			public void flip(int component) {
				scale(component, -1);
			}
			
			public void scale(int component, float s) {
				if(component < 0 || component >= dimension)
					return;
				for(int i = component; i < rawVerts.length; i += dimension)
					rawVerts[i] *= s;
			}
			
			public void scale(float... scales) {
				for(int i = 0; i < scales.length && i < dimension; i++)
					for(int j = i; j < rawVerts.length; j += dimension)
						rawVerts[j] *= scales[i];
			}
			
			public void scaleAll(float scale) {
				for(int i = 0; i < rawVerts.length; i++)
					rawVerts[i] *= scale;
			}
			
			public void add(int component, float val) {
				if(component < 0 || component >= dimension)
					return;
				for(int i = component; i < rawVerts.length; i += dimension)
					rawVerts[i] += val;
			}
			
			public void add(float... vals) {
				for(int i = 0; i < vals.length && i < dimension; i++)
					for(int j = i; j < rawVerts.length; j += dimension)
						rawVerts[j] += vals[i];
			}
			
			public void addAll(float val) {
				for(int i = 0; i < rawVerts.length; i++)
					rawVerts[i] += val;
			}
			
			public void switchComponents(int comp1, int comp2) {
				if(comp1 < 0 || comp1 >= dimension || comp2 < 0 || comp2 >= dimension)
					return;
				for(int i = 0; i < Math.floorDiv(rawVerts.length, dimension); i++) {
					float temp = rawVerts[i*dimension+comp1];
					rawVerts[i*dimension+comp1] = rawVerts[i*dimension+comp2];
					rawVerts[i*dimension+comp2] = temp;
				}
			}
			
		}
		
	}
}
