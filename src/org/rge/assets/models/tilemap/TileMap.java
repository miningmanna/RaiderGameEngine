package org.rge.assets.models.tilemap;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
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
	public int[] vbos;
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
		
		int points = width*height + (width+1)*(height+1);
		
		heights = new float[points];
		System.arraycopy(raw.heights, 0, heights, 0, points);
		
		tiles = new int[width][height];
		for(int i = 0; i < width; i++)
			System.arraycopy(raw.tiles[i], 0, tiles[i], 0, height);
		
		if(raw.rawTexs != null)
			texs = new TileMapTexture(am, raw.rawTexs);
		
		// CREATE MESH
		int vcount = width*height*12;
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
		
		verts = new Verts[] { vpos, vtex, vnorm };
		
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
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
		int pointsoff = (y*width+x)*12;
		int posoff = pointsoff*3;
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				int off = posoff + (i*3+j)*3;
				
				int hindex = 0;
				int posoffi = 0;
				switch(i*3+j) {
				case 0:
				case 10:
					posoffi = 0;
					hindex = (y*(2*width+1))+x;
					break;
				case 1:
				case 3:
					posoffi = 1;
					hindex = ((y+1)*(2*width+1))+x;
					break;
				case 4:
				case 6:
					posoffi = 2;
					hindex = ((y+1)*(2*width+1))+x+1;
					break;
				case 9:
				case 7:
					posoffi = 3;
					hindex = (y*(2*width+1))+x+1;
					break;
				case 2:
				case 5:
				case 8:
				case 11:
					posoffi = 4;
					hindex = (y*(2*width+1))+x+width+1;
					break;
				}
				vpos.rawVerts[off+0] = x + POS_OFFSETS[posoffi*2];
				vpos.rawVerts[off+1] = heights[hindex];
				vpos.rawVerts[off+2] = y + POS_OFFSETS[posoffi*2+1];
			}
		}
		
	}
	
	private void updateHeight(int x, int y) {
		int pointsoff = (y*width+x)*12;
		int posoff = pointsoff*3;
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				int off = posoff + (i*3+j)*3;
				
				int hindex = 0;
				switch(i*3+j) {
				case 0:
				case 10:
					hindex = (y*(2*width+1))+x;
					break;
				case 1:
				case 3:
					hindex = ((y+1)*(2*width+1))+x;
					break;
				case 4:
				case 6:
					hindex = ((y+1)*(2*width+1))+x+1;
					break;
				case 9:
				case 7:
					hindex = (y*(2*width+1))+x+1;
					break;
				case 2:
				case 5:
				case 8:
				case 11:
					hindex = (y*(2*width+1))+x+width+1;
					break;
				}
				vpos.rawVerts[off+1] = heights[hindex];
			}
		}
		
	}
	
	private void updateNormals(int x, int y) {
		int pointsoff = (y*width+x)*12;
		
		for(int j = 0; j < 4; j++) {
			
			int p0 = pointsoff + j*3; // Current point -> starting point of triangle j
			int p1 = pointsoff + j*3 + 1; // Always the middlepoint -> index 4 in current tile
			int p2 = pointsoff + j*3 + 2; // Point of the next triagnel (i+1)%4
			
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
		
		int pointsoff = (y*width+x)*12;
		int texoff = pointsoff*2;
		
		if(texs == null) {
			for(int i = 0; i < 5; i++)
			{
				if(i == 4) {
					for(int j = 0; j < 4; j++)
					{
						int off = texoff + ((j*3 + 2)*2);
						vtex.rawVerts[off    ] = POS_OFFSETS[i*2];
						vtex.rawVerts[off + 1] = POS_OFFSETS[i*2+1];
					}
				} else {
					for(int j = 0; j < 2; j++)
					{
						int off = texoff + ((i*3 + j*10)%12)*2;
						vtex.rawVerts[off    ] = POS_OFFSETS[i*2];
						vtex.rawVerts[off + 1] = POS_OFFSETS[i*2+1];
					}
				}
			}
			return;
		}
		
		int tile = tiles[x][y];
		TileType tex = texs.types.get(tile);
		if(tex == null) {
			for(int i = 0; i < 5; i++)
			{
				if(i == 4) {
					for(int j = 0; j < 4; j++)
					{
						int off = texoff + ((j*3 + 2)*2);
						vtex.rawVerts[off    ] = POS_OFFSETS[i*2];
						vtex.rawVerts[off + 1] = POS_OFFSETS[i*2+1];
					}
				} else {
					for(int j = 0; j < 2; j++)
					{
						int off = texoff + ((i*3 + j*10)%12)*2;
						vtex.rawVerts[off    ] = POS_OFFSETS[i*2];
						vtex.rawVerts[off + 1] = POS_OFFSETS[i*2+1];
					}
				}
			}
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
			
			for(int i = 0; i < 5; i++) {
				float xp = POS_OFFSETS[i*2]   - 0.5f;
				float yp = POS_OFFSETS[i*2+1] - 0.5f;
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
				
				if(i == 4) {
					for(int j = 0; j < 4; j++)
					{
						int off = texoff + ((j*3 + 2)*2);
						vtex.rawVerts[off    ] = ((xp*cos - yp*sin) + 0.5f + tdata.x) / (float) texs.width;
						vtex.rawVerts[off + 1] = ((xp*sin + yp*cos) + 0.5f + tdata.y) / (float) texs.height;
					}
				} else {
					for(int j = 0; j < 2; j++)
					{
						int off = texoff + ((i*3 + j*10)%12)*2;
						vtex.rawVerts[off    ] = ((xp*cos - yp*sin) + 0.5f + tdata.x) / (float) texs.width;
						vtex.rawVerts[off + 1] = ((xp*sin + yp*cos) + 0.5f + tdata.y) / (float) texs.height;
					}
				}
			}
		} else {
			
			// Determine neighbouring conditions
			int field = 0;
			int offset = 0;
			for(int ox = -1; ox < 2; ox++) {
				for(int oy = -1; oy < 2; oy++) {
					if(ox == 0 && oy == 0)
						continue; // Skip tile at parameter coordinates
					
					int nx = ox + x;
					int ny = oy + y;
					// Skip if out of bounds (equals a non-connecting tile)

					int neighboudId;
					if(nx < 0 || nx >= width)
						neighboudId = tiles[x][y];
					else if(ny < 0 || ny >= height)
						neighboudId = tiles[x][y];
					else
						neighboudId = tiles[nx][ny];
					
					if(tex.findConnect(neighboudId) >= 0)
						field |= (1 << offset); // Set bit
					offset++;
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
			
			for(int i = 0; i < 5; i++) {
				float xp = POS_OFFSETS[i*2]   - 0.5f;
				float yp = POS_OFFSETS[i*2+1] - 0.5f;
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
				
				if(i == 4) {
					for(int j = 0; j < 4; j++)
					{
						int off = texoff + ((j*3 + 2)*2);
						vtex.rawVerts[off    ] = ((xp*cos - yp*sin) + 0.5f + tdata.x) / (float) texs.width;
						vtex.rawVerts[off + 1] = ((xp*sin + yp*cos) + 0.5f + tdata.y) / (float) texs.height;
					}
				} else {
					for(int j = 0; j < 2; j++)
					{
						int off = texoff + ((i*3 + j*10)%12)*2;
						vtex.rawVerts[off    ] = ((xp*cos - yp*sin) + 0.5f + tdata.x) / (float) texs.width;
						vtex.rawVerts[off + 1] = ((xp*sin + yp*cos) + 0.5f + tdata.y) / (float) texs.height;
					}
				}
			}
		}
	}
	
	// TODO: implements as flush for changes
	/*public void updateModel() {
		
		
		
	}*/
	
	private void initLuaTable() {
		
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
		
		ref.set("shader", new OneArgFunction() {
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
		ref.set("updateTile", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaInteger && arg1 instanceof LuaInteger))
					return NIL;
				
				int x = arg0.checkint();
				int y = arg1.checkint();
				
				updateTexCoords(x, y);
				genMesh(x, y);
				updateNormals(x, y);
				
				int num = y*width + x;
				glLoader.updateTilesTiles(TileMap.this, num);
				
				return NIL;
			}
		});
		ref.set("tile", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				if(arg0 instanceof LuaInteger && arg1 instanceof LuaInteger) {
					int x = arg0.checkint();
					int y = arg1.checkint();
					if(!(arg2 instanceof LuaInteger))
						return LuaValue.valueOf(tiles[x][y]);
					
					int originalTile = tiles[x][y];
					int newTile = arg2.checkint();
					if(originalTile == newTile)
						return LuaValue.valueOf(originalTile);
					
					tiles[x][y] = newTile;
					
					for(int ox = -1; ox < 2; ox++) {
						for(int oy = -1; oy < 2; oy++) {
							
							int ax = x + ox;
							int ay = y + oy;
							if(ax < 0 || ax >= width || ay < 0 || ay >= height)
								continue;
							
							
							updateTexCoords(ax, ay);
							
							int num = ay*width + ax;
							glLoader.updateTilesTiles(TileMap.this, num);
							
						}
					}
					
					return LuaValue.valueOf(newTile);
				}
				
				return NIL;
			}
		});
		ref.set("pointHeight", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(!(arg0 instanceof LuaInteger))
					return NIL;
				
				int offset = arg0.checkint();
				if(offset < 0 || offset >= heights.length)
					return NIL;
				
				if(!(arg1 instanceof LuaDouble) && !(arg1 instanceof LuaInteger))
					return LuaValue.valueOf(heights[offset]);
				
				heights[offset] = arg1.tofloat();
				
				int drow = 2*width+1;
				int x = offset%drow;
				int y = offset/drow; 
				if(x > width) {
					// Middlepoint
					x -= width+1;
					
					updateHeight(x, y);
					updateNormals(x, y);
					int num = y*width + x;
					glLoader.updateTilesTiles(TileMap.this, num);
					
				} else {
					// Corners
					
					for(int ox = -1; ox < 1; ox++) {
						for(int oy = -1; oy < 1; oy++) {
							int ax = x + ox;
							int ay = y + oy;
							if(ax < 0 || ax >= width || ay < 0 || ay >= height)
								continue;
							updateHeight(ax, ay);
							updateNormals(ax, ay);
							
							int num = ay*width + ax;
							glLoader.updateTilesTiles(TileMap.this, num);
						}
					}
					
				}
				
				return LuaValue.valueOf(heights[offset]);
			}
		});
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
			ref.set("pointHeight", new TwoArgFunction() {
				@Override
				public LuaValue call(LuaValue arg0, LuaValue arg1) {
					if(!(arg0 instanceof LuaInteger))
						return NIL;
					
					int offset = arg0.checkint();
					if(offset < 0 || offset >= heights.length)
						return NIL;
					
					if(!(arg1 instanceof LuaDouble) && !(arg1 instanceof LuaInteger))
						return LuaValue.valueOf(heights[offset]);
					
					heights[offset] = arg1.tofloat();
					
					return LuaValue.valueOf(heights[offset]);
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
