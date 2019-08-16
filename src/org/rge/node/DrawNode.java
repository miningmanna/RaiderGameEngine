package org.rge.node;

import java.util.ArrayList;

import org.joml.Matrix4f;
import org.rge.assets.models.Model;
import org.rge.node.collision.Collider;
import org.rge.sound.Source;

public class DrawNode {
	
	private static final Matrix4f identityMatrix = new Matrix4f().identity();
	
	public ArrayList<DrawNode> subNodes;
	public Model model;
	public Move move;
	public Collider collider;
	public Source source;
	
	public DrawNode() {
		subNodes = new ArrayList<>();
	}
	
	public Matrix4f getTransform() {
		if(move != null)
			return move.getTransform();
		else
			return identityMatrix;
	}
	
}
