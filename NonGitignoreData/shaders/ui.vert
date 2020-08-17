#version 400 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 tPos;

out vec3 mPos;
out vec2 mTPos;

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

struct SpotLight {
	vec3 color;
	vec3 position;
	vec3 direction;
	float intensity;
	float clamp;
	float cutoff;
};
uniform SpotLight spotLights[4];

uniform float scale = 1;

void main() {
	
	gl_Position = vec4(pos*scale, 1);
	
	mPos = pos*scale;
	mTPos = tPos;
}
