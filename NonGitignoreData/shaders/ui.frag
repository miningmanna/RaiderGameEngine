#version 400 core

in vec3 mPos;
in vec2 mTPos;

out vec4 color;

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
	vec3 position;
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

void main() {
	
	vec4 surfColor;
	if(useTex) {
		surfColor = texture2D(tex, mTPos);
		if(surfColor.w == 0)
			discard;
	} else {
		surfColor = vec4(1, 0, useTex, 1);
	}
	
	// --> Lighting here
	
	
	color = surfColor;
}