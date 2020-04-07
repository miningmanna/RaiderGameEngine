package org.rge.assets.models.tilemap;

import java.io.IOException;

import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.assets.AssetManager;
import org.rge.assets.GLLoader;
import org.rge.assets.models.Verts;
import org.rge.assets.models.tilemap.TileMapTexture.RawTileMapTexture;
import org.rge.graphics.Renderable;
import org.rge.graphics.Shader;
import org.rge.graphics.Surface;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class TileMap implements EngineObject, Renderable {
	
	private EngineReference ref;
	
	// GL stuff
	private GLLoader glLoader;
	
	public int vao;
	public Shader shader;
	public Surface surf;
	public Surface[] _surfs;
	public int[] usedVBOs;
	public Verts vpos, vnorm, vtex; // Travel in the y direction, then x
	public Verts[] verts;
	
	public TileMapTexture texs;
	
	// tile stuff
	public int width, height;
	public float[] heights;
	public int[][] tiles;
	
	public TileMap(AssetManager am, RawTileMap raw) {
		initLuaTable();
		
		glLoader = am.getGLLoader();
		width = raw.width;
		height = raw.height;
		
		try {
			shader = am.getShader("tilemap");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int points = width*height + (width+1)*(height+1);
		
		heights = new float[points];
		System.arraycopy(raw.heights, 0, heights, 0, points);
		
		tiles = new int[width][height];
		for(int i = 0; i < width; i++)
			System.arraycopy(raw.tiles[i], 0, tiles[i], 0, height);
		
		if(raw.rawTexs != null)
			texs = new TileMapTexture(am, raw.rawTexs);
		
		// CREATE MESH
		int vcount = width*height*5;
		vpos = new Verts();
		vpos.dimension = 3;
		vpos.pos = 0;
		
		vtex = new Verts();
		vtex.dimension = 2;
		vtex.pos = 1;
		
		vnorm = new Verts();
		vnorm.dimension = 3;
		vnorm.pos = 2;
		
		vpos.rawVerts = new float[vcount*3];
		vnorm.rawVerts = new float[vcount*3];
		vtex.rawVerts = new float[vcount*2];
		
		verts = new Verts[] { vpos, vnorm, vtex };
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < width; y++)
				genTile(x, y);
		
		surf = new Surface();
		surf.additiveColor = false;
		surf.doubleSided = false;
		surf.indOffset = 0;
		surf.indLength = width*height*4*3;
		surf.par = this;
		if(texs != null)
			surf.tex = texs.atlas;
		
		_surfs = new Surface[] { surf };
		
		glLoader.loadTileMap(this);
		
	}
	
	private void updateMeshHeight(int x, int y) {
		
		int pointsoff = (x*height+y)*5;
		int posoff = pointsoff*3;
		
		for(int i = 0; i < 5; i++) {
			int off = posoff+i*3;
			int hindex = 0;
			switch(i) {
			case 0: hindex = (y*(2*width-1))+x; break;
			case 1: hindex = ((y+1)*(2*width-1))+x; break;
			case 2: hindex = ((y+1)*(2*width-1))+x+1; break;
			case 3: hindex = (y*(2*width-1))+x+1; break;
			case 4: hindex = (y*(2*width-1))+x+width+1; break;
			}
			
			vpos.rawVerts[off+1] = heights[hindex];
			
		}
		
	}
	
	private void genTile(int x, int y) {
		
		genMesh(x, y);
		updateNormals(x, y);
		updateTexCoords(x, y);
		
	}
	
	private static final float[] POS_OFFSETS = {
			0, 0,
			0, 1,
			1, 1,
			1, 0,
			0.5f, 0.5f
	};
	private void genMesh(int x, int y) {
		int pointsoff = (x*height+y)*5;
		int posoff = pointsoff*3;
		
		
		for(int i = 0; i < 5; i++) {
			int off = posoff+i*3;
			int hindex = 0;
			switch(i) {
			case 0: hindex = (y*(2*width+1))+x; break;
			case 1: hindex = ((y+1)*(2*width+1))+x; break;
			case 2: hindex = ((y+1)*(2*width+1))+x+1; break;
			case 3: hindex = (y*(2*width+1))+x+1; break;
			case 4: hindex = (y*(2*width+1))+x+width+1; break;
			}
			
			vpos.rawVerts[off+0] = x + POS_OFFSETS[i*2];
			vpos.rawVerts[off+1] = heights[hindex];
			vpos.rawVerts[off+2] = y + POS_OFFSETS[i*2+1];
			
		}
		
	}
	
	private void updateNormals(int x, int y) {
		int pointsoff = (x*height+y)*5;
		
		for(int j = 0; j < 4; j++) {
			
			int p0 = pointsoff + j; // Current point -> starting point of triangle i
			int p1 = pointsoff + 4; // Always the middlepoint -> index 4 in current tile
			int p2 = pointsoff + (j+1)%4; // Point of the next triagnel (i+1)%4
			
			// a is a vector from p0 to p1
			float[] a = new float[3];
			for(int i = 0; i < 3; i++)
				a[i] = vpos.rawVerts[p1*3+i] - vpos.rawVerts[p0*3+i];
			
			// b is a vector from p0 to p2
			float[] b = new float[3];
			for(int i = 0; i < 3; i++)
				b[i] = vpos.rawVerts[p2*3+i] - vpos.rawVerts[p0*3+i];
			
			for(int i = 0; i < 3; i++)
				vnorm.rawVerts[p0*3+i] = vnorm.rawVerts[p1*3+i] = vnorm.rawVerts[p2*3+i]
						= ((a[(i+1)%3]*b[(i+2)%3]) - (b[(i+1)%3]*a[(i+2)%3]));
			
			
		}
	}
	
	private void updateTexCoords(int x, int y) {
		int pointsoff = (x*height+y)*5;
		int texoff = pointsoff*2;
		
		System.out.println("UPDATING TEX COORDS");
		
		if(texs == null) {
			for(int i = 0; i < 10; i++)
				vtex.rawVerts[texoff+i] = POS_OFFSETS[i];
			return;
		}
		
		int tile = tiles[x][y];
		TileType tex = texs.types.get(tile);
		if(tex == null) {
			for(int i = 0; i < 10; i++)
				vtex.rawVerts[texoff+i] = POS_OFFSETS[i];
			return;
		}

		if(tex.connects.size() == 0) {
			// Skip if it doesnt connect to anything
			TileTexPosRot tdata = tex.texs[0];
			
			// Xp Expression: (Px - 0.5f)
			// Yp Expression: (Py - 0.5f)
			// Used to rotate with X' = Xp*cos(r) - Yp*sin(r)
			// Used to rotate with Y' = Xp*sin(r) + Yp*cos(r)
			// Move back: with Xf = X'+0.5f
			// Move back: with Yf = Y'+0.5f
			
			for(int i = 0; i < 10; i += 2) {
				float xp = POS_OFFSETS[i]   - 0.5f;
				float yp = POS_OFFSETS[i+1] - 0.5f;
				int rot = tdata.rot % 4;
				if(rot < 0) // In case rotation is negative
					rot += 4;
				
				
				float cos = 0;
				float sin = 0;
				switch(rot)
				{
					case 0:
						cos = 1;
						sin = 0;
						break;
					case 1:
						cos = 0;
						sin = 1;
						break;
					case 2:
						cos = -1;
						sin = 0;
						break;
					case 3:
						cos = 0;
						sin = -1;
						break;
				}
				
				
				vtex.rawVerts[texoff + i    ] = ((xp*cos - yp*sin) + 0.5f + tdata.x) / (float) texs.width;
				vtex.rawVerts[texoff + i + 1] = ((xp*sin + yp*cos) + 0.5f + tdata.y) / (float) texs.height;
			}
		} else {
			
			// Determine neighbouring conditions
			int field = 0;
			for(int ox = -1; ox < 2; ox++) {
				for(int oy = -1; oy < 2; oy++) {
					if(ox == 0 && oy == 0)
						continue; // Skip tile at parameter coordinates
					field = field << 1;
					
					int nx = ox + x;
					int ny = oy + y;
					// Skip if out of bounds (equals a non-connecting tile)
					if(nx < 0 || nx >= width)
						continue;
					if(ny < 0 || ny >= height)
						continue;
					
					int neighboudId = tiles[nx][ny];
					if(tex.findConnect(neighboudId) >= 0)
						field |= 1; // Set bit
				}
			}
			
			// Neighbour bitfield used to select rotation and coordinate on atlas
			TileTexPosRot tdata = tex.texs[field];
			
			// Xp Expression: (Px - 0.5f)
			// Yp Expression: (Py - 0.5f)
			// Used to rotate with X' = Xp*cos(r) - Yp*sin(r)
			// Used to rotate with Y' = Xp*sin(r) + Yp*cos(r)
			// Move back: with Xf = X'+0.5f
			// Move back: with Yf = Y'+0.5f
			
			for(int i = 0; i < 10; i += 2) {
				float xp = POS_OFFSETS[i]   - 0.5f;
				float yp = POS_OFFSETS[i+1] - 0.5f;
				int rot = tdata.rot % 4;
				if(rot < 0) // In case rotation is negative
					rot += 4;
				
				
				float cos = 0;
				float sin = 0;
				switch(rot)
				{
					case 0:
						cos = 1;
						sin = 0;
						break;
					case 1:
						cos = 0;
						sin = 1;
						break;
					case 2:
						cos = -1;
						sin = 0;
						break;
					case 3:
						cos = 0;
						sin = -1;
						break;
				}
				
				
				vtex.rawVerts[texoff + i    ] = ((xp*cos - yp*sin) + 0.5f + tdata.x) / (float) texs.width;
				vtex.rawVerts[texoff + i + 1] = ((xp*sin + yp*cos) + 0.5f + tdata.y) / (float) texs.height;
			}
		}
	}
	
	public void updateModel() {
		
		
		
	}
	
	private void initLuaTable() {
		
		ref = new EngineReference(this);
		
	}
	
	@Override
	public EngineReference getEngineReference() {
		return ref;
	}
	
	public static class RawTileMap implements EngineObject {
		
		EngineReference ref;
		public RawTileMap(int w, int h) {
			width = w;
			height = h;
			width = w;
			height = h;
			heights = new float[w*h+(w+1)*(h+1)];
			tiles = new int[w][h];
			initLua();
		}
		
		public RawTileMapTexture rawTexs;
		
		public int width, height;
		public float[] heights;
		public int[][] tiles;
		
		private void initLua() {
			ref = new EngineReference(this);
			
			ref.set("width", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(width);
				}
			});
			ref.set("height", new ZeroArgFunction() {
				@Override
				public LuaValue call() {
					return LuaValue.valueOf(height);
				}
			});
			ref.set("tile", new ThreeArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
					if(arg0 instanceof LuaInteger && arg1 instanceof LuaInteger) {
						int x = arg0.checkint();
						int y = arg1.checkint();
						if(arg2 instanceof LuaInteger)
							tiles[x][y] = arg2.checkint();
						return LuaValue.valueOf(tiles[x][y]);
					}
					return NIL;
				}
			});
			ref.set("texture", new OneArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0) {
					if(arg0 instanceof EngineReference) {
						EngineReference ref = (EngineReference) arg0;
						if(ref.parent instanceof RawTileMapTexture)
							rawTexs = (RawTileMapTexture) ref.parent;
					}
					if(rawTexs != null)
						rawTexs.getEngineReference();
					return NIL;
				}
			});
		}
		
		@Override
		public EngineReference getEngineReference() {
			return ref;
		}
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
	public void advance(float dt) {
		if(texs != null)
			texs.advance(dt);
	}
	
	@Override
	public Surface[] getSurfs() {
		return _surfs;
	}
	
}
