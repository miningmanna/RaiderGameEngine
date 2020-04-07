#version 400 core

layout(location = 0) in vec3 pos;

out vec3 mPos;

void main() {
	
	mPos = pos;
	gl_Position = vec4(pos, 1);
	
}
