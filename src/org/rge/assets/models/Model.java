package org.rge.assets.models;

import java.io.IOException;

import org.joml.Vector3f;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.rge.assets.AssetManager;
import org.rge.assets.models.Model.RawData.RawSurface;
import org.rge.assets.models.Texture.TextureRawInfo;
import org.rge.graphics.Renderable;
import org.rge.graphics.Shader;
import org.rge.graphics.Surface;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Model implements EngineObject, Renderable {
	
	EngineReference engReference;
	
	AssetManager am;
	
	public RawData raw;
	public Shader shader;
	public Surface[] surfs;
	public int[] usedVBOs;
	public int vao;
	public int indCount;
	
	public Model(AssetManager am, RawData data, boolean keepData) throws IOException {
		
		initLuaTable();
		
		this.am = am;
		raw = data;
		
		am.loadModel(this);
		
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
			surf.doubleSided = rawSurf.doubleSided;
			surf.tex = am.loadTexture(rawSurf.tex);
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
	
	public void advance(float dt) {
		for(int i = 0; i < surfs.length; i++)
			if(surfs[i].tex != null)
				if(surfs[i].tex.animated)
					surfs[i].tex.advance(dt);
	}
	
	public static class RawData {
		
		public RawSurface[] surfaces;
		public Verts[] verts;
		public int[] rawInds;
		
		public static class RawSurface {
			
			public int indOffset;
			public int indLength;
			public TextureRawInfo tex;
			public boolean isTranslucent;
			public boolean additiveColor;
			public boolean doubleSided;
			public Vector3f midPoint;
			
		}
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("shader", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(arg0 == NIL) {
					shader = null;
					return NIL;
				}
				
				if(!(arg0 instanceof EngineReference))
					if(shader == null)
						return NIL;
					else
						return shader.getEngineReference();
				
				EngineReference _ref = (EngineReference) arg0;
				if(!(_ref.parent instanceof Shader))
					if(shader == null)
						return NIL;
					else
						return shader.getEngineReference();
				
				shader = (Shader) _ref.parent;
				
				return shader.getEngineReference();
			}
		});
		
	}

	@Override
	public int getVAO() {
		return vao;
	}

	@Override
	public Shader getShader() {
		return shader;
	}

	@Override
	public int[] getUsedVBOs() {
		return usedVBOs;
	}

	@Override
	public Surface[] getSurfs() {
		return surfs;
	}
	
}
