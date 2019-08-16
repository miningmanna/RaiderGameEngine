#version 400 core

layout(location = 0) in vec3 pos;

out vec3 mPos;

uniform mat4 camera;

void main() {
	
	gl_Position = camera * vec4(pos, 1);
	
	mPos = pos;
	
}