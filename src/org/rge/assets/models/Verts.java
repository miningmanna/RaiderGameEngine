package org.rge.assets.models;

public class Verts {
	
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