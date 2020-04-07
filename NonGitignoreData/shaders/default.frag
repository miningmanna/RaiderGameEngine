#version 400 core

in vec3 mPos;

out vec4 outColor;

void main() {
	
	outColor = vec4(mPos, 1);
	
}