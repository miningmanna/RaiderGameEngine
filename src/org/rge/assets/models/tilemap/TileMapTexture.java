package org.rge.assets.models.tilemap;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.assets.AssetManager;
import org.rge.assets.models.Texture;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class TileMapTexture implements EngineObject {
	
	private EngineReference ref;
	
	public int width, height;
	public HashMap<Integer, TileType> types;
	
	Texture atlas;
	
	public TileMapTexture(AssetManager am, RawTileMapTexture raw) {
		initLuaTable();
		
		types = new HashMap<>();
		
		width = raw.width;
		height = raw.height;
		
		BufferedImage[][] rawImgs = new BufferedImage[width][height];
		for(int x = 0; x <  width; x++)
			for(int y = 0; y < height; y++) {
				System.out.println("TILEMAP TEX PATH: " + raw.texPaths[x][y]);
				rawImgs[x][y] = am.getTextureRaw(raw.texPaths[x][y]);
				System.out.println("TILEMAP TEX: " + rawImgs[x][y]);
			}
		
		int iw = 0, ih = 0;
		for(int x = 0; x <  width; x++) {
			for(int y = 0; y < height; y++) {
				if(rawImgs[x][y] != null) {
					BufferedImage img = rawImgs[x][y];
					if(img.getWidth() > iw)
						iw = img.getWidth();
					if(img.getHeight() > ih)
						ih = img.getHeight();
				}
			}
		}
		
		BufferedImage img = new BufferedImage(iw*width, ih*height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) img.getGraphics();
		for(int x = 0; x <  width; x++)
			for(int y = 0; y < height; y++)
				if(rawImgs[x][y] != null)
					g2d.drawImage(rawImgs[x][y], x*iw, y*ih, iw, ih, null);
		System.out.println("ATLAS IMG: " + img);
		atlas = am.loadTexture(img);
		
		for(int i = 0; i < raw.types.size(); i++)
			types.put(raw.types.get(i).id, raw.types.get(i));
		
	}
	
	private void initLuaTable() {
		ref = new EngineReference(this);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
	public static class RawTileMapTexture implements EngineObject {
		
		private EngineReference ref;
		
		public int width, height;
		public ArrayList<TileType> types;
		public String[][] texPaths;
		
		public RawTileMapTexture(int w, int h) {
			width = w;
			height = h;
			texPaths = new String[w][h];
			types = new ArrayList<>();
			initLuaTable();
		}
		
		int findType(int id) {
			for(int i = 0; i < types.size(); i++)
				if(types.get(i).id == id)
					return i;
			return -1;
		}
		
		private void initLuaTable() {
			ref = new EngineReference(this);
			
			ref.set("width", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return valueOf(width);
				}
			});
			ref.set("height", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return valueOf(height);
				}
			});
			
			ref.set("texture", new ThreeArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
					if(!(arg0 instanceof LuaInteger))
						return NIL;
					if(!(arg1 instanceof LuaInteger))
						return NIL;
					int x = arg0.toint();
					int y = arg1.toint();
					if(x < 0 || x >= width || y < 0 || y >= height)
						return NIL;
					if(!(arg2 instanceof LuaString))
						return LuaValue.valueOf(texPaths[x][y]);
					
					texPaths[x][y] = arg2.tojstring();
					
					return LuaValue.valueOf(texPaths[x][y]);
				}
			});
			ref.set("addType", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0) {
					if(arg0 instanceof EngineReference) {
						EngineReference ref = (EngineReference) arg0;
						if(!(ref.parent instanceof TileType))
							return FALSE;
						
						types.add((TileType) ref.parent);
						
						return TRUE;
					}
					return FALSE;
				}
			});
			ref.set("hasType", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0) {
					if(arg0 instanceof LuaInteger) {
						int id = arg0.checkint();
						if(findType(id) > 0)
							return FALSE;
						return TRUE;
					}
					return FALSE;
				}
			});
			ref.set("removeType", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0) {
					if(arg0 instanceof LuaInteger) {
						int id = arg0.checkint();
						int i = findType(id);
						if(i < 0)
							return FALSE;
						types.remove(i);
						return TRUE;
					}
					return FALSE;
				}
			});
		}
		
		@Override
		public EngineReference getEngineReference() {
			return ref;
		}
		
	}
	
	public void advance(float dt) {
		// TODO: Advance atlas animation if present
	}
	
}
