package org.rge.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.rge.lua.EngineObject;
import org.rge.lua.EngineReference;

public class Camera implements EngineObject {
	
	EngineReference engReference;
	
	public Vector3f position;
	public Matrix4f projection;
	public Matrix4f combined;
	public Matrix4f transform;
	public Matrix4f rotate;
	
	public Vector3f up, right, direction;
	
	
	public Camera() {
		
		initLuaTable();
		
		direction = new Vector3f(0, 0, -1);
		up = new Vector3f(0, 1, 0);
		right = new Vector3f(1, 0, 0);
		position = new Vector3f();
		projection = new Matrix4f();
		combined = new Matrix4f();
		transform = new Matrix4f();
		rotate = new Matrix4f();
		rotate.identity();
	}
	
	public void setFrustum(float fovy, float aspect, float zNear, float zFar) {
		projection.setPerspective((float) Math.toRadians(fovy), aspect, zNear, zFar);
	}
	
	public void setOrtho(float left, float right, float bottom, float top, float zNear, float zFar) {
		projection.setOrtho(left, right, bottom, top, zNear, zFar);
	}
	
	public void update() {
		transform.identity();
		transform.mul(rotate);
		transform.translate(-position.x, -position.y, -position.z);
		combined.set(projection);
		combined.mul(transform);
	}
	
	public void move(Vector3f v) {
		Vector3f temp = new Vector3f(direction);
		position.add(temp.mul(v.z));
		position.add(0, v.y, 0);
		temp.set(right);
		position.add(temp.mul(v.x));
	}
	
	public void rotateVertical(float angle) {
		rotate.rotate(angle, right);
		direction.rotateAxis(-angle, right.x, right.y, right.z);
	}
	
	public void rotateHorizontal(float d) {
		rotate.rotateY(d);
		right.rotateY(-d);
		direction.rotateY(-d);
	}
	
	@Override
	public EngineReference getEngineReference() {
		return engReference;
	}
	
	private Vector3f temp = new Vector3f();
	private void initLuaTable() {
		engReference = new EngineReference(this);
		
		engReference.set("update", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				update();
				return NIL;
			}
		});
		
		engReference.set("rotateHorizontal", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return NIL;
				rotateHorizontal((float) arg0.checkdouble());
				return NIL;
			}
		});
		
		engReference.set("rotateVertical", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return NIL;
				rotateVertical((float) arg0.checkdouble());
				return NIL;
			}
		});
		
		engReference.set("moveX", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return NIL;
				temp.x = (float) arg0.checkdouble();
				temp.y = 0;
				temp.z = 0;
				move(temp);
				return NIL;
			}
		});
		
		engReference.set("moveY", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return NIL;
				temp.x = 0;
				temp.y = (float) arg0.checkdouble();
				temp.z = 0;
				move(temp);
				return NIL;
			}
		});
		
		engReference.set("moveZ", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				if(!(arg0 instanceof LuaDouble || arg0 instanceof LuaInteger))
					return NIL;
				temp.x = 0;
				temp.y = 0;
				temp.z = (float) arg0.checkdouble();
				move(temp);
				return NIL;
			}
		});
		
		engReference.set("setFrustrum", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs vargs) {
				System.out.println("Setting frustrum");
				System.out.println(vargs.narg());
				if(vargs.narg() != 4)
					return NIL;
				for(int i = 0; i < 4; i++) {
					System.out.println(vargs.arg(i+1).getClass() + ": " + vargs.arg(i+1).checkdouble());
					if(!(vargs.arg(i+1) instanceof LuaDouble || vargs.arg(i+1) instanceof LuaInteger))
						return NIL;
				}
				
				System.out.println("Doing it!");
				
				setFrustum(
						(float) vargs.arg(1).checkdouble(),
						(float) vargs.arg(2).checkdouble(),
						(float) vargs.arg(3).checkdouble(),
						(float) vargs.arg(4).checkdouble()
				);
				
				return NIL;
			}
		});
			
		
	}
	
}
