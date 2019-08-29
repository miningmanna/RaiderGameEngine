#version 400 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec4 color;
layout(location = 2) in vec3 normal;

out vec3 mPos;
out vec4 mColor;
out vec3 mNormal;

uniform mat4 camera;
uniform mat4 transform;

uniform bool useTex;
uniform sampler2D tex;

uniform vec4 ambientLight;

struct DirectionalLight {
	vec3 color;
	vec3 direction;
	float intensity;
	float clamp;
};
uniform DirectionalLight directionalLights[4];

struct PointLight {
	vec3 color;
	vec3 pos;
	float intensity;
	float clamp;
};
uniform PointLight pointLights[4];

void main() {
	
	gl_Position = camera * transform * vec4(pos, 1);
	
	mPos = (transform * vec4(pos, 1)).xyz;
	mColor = color;
	mNormal = normal;
}
