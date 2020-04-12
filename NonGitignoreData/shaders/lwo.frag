#version 400 core

in vec3 mPos;
in vec4 mColor;
in vec2 mTPos;
in vec3 mNormal;

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
		surfColor = mColor;
	}
	
	// --> Lighting here
	
	vec3 camPos = -vec3(camera[3].xyz);				// CAMPOSITION IS NOT CORRECT!
	float dist2Cam = length(mPos - camPos);
	
	mat3 normalMatrix = transpose(inverse(mat3(transform)));
	vec3 normal = normalize(normalMatrix * mNormal);
	if(determinant(mat3(transform)) < 0) // Surface flipped!
		normal *= -1;
	
	vec4 ambientRes = vec4(ambientLight.xyz*surfColor.xyz*ambientLight.w, surfColor.w);
	
	vec3 dirRes = vec3(0);
	for(int i = 0; i < 4; i++) {
		if(directionalLights[i].intensity == 0)
			continue;
		
		float k = dot(-normalize(directionalLights[i].direction), normal);
		k *= directionalLights[i].intensity; 											// / (1.0 + pow(dist2Cam, 2.0)); old inverse square law
		k = clamp(k, 0.0, directionalLights[i].clamp);
		
		dirRes += k * directionalLights[i].color * surfColor.xyz;
	}
	
	vec3 pointRes = vec3(0, 0, 0);
	for(int i = 0; i < 4; i++) {
		if(pointLights[i].intensity == 0)
			continue;
		
		vec3 dir = mPos - pointLights[i].position;
		float distPoint2Surf = length(dir);
		dir = normalize(dir);
		
		float intensity = pointLights[i].intensity / (1.0 + pow(distPoint2Surf, 2.0));
		
		float k = dot(-dir, normal);
		k *= intensity;																	// / (1.0 + pow(dist2Cam, 2.0)); old inverse square law
		k = clamp(k, 0.0, pointLights[i].clamp);
		
		pointRes += k * pointLights[i].color * surfColor.xyz;
	}
	
	vec3 spotRes = vec3(0, 0, 0);
	for(int i = 0; i < 4; i++) {
		
		vec3 dir = mPos - spotLights[i].position;
		float distPoint2Surf = length(dir);
		dir = normalize(dir);
		
		float intensity = spotLights[i].intensity / (1.0 + pow(distPoint2Surf, 2.0));
		
		float coneCos = dot(normalize(spotLights[i].direction), dir);
		float k = (coneCos - spotLights[i].cutoff)/(1 - spotLights[i].cutoff);
		if(k < 0)
			continue;
		
		k *= clamp(intensity, 0.0, spotLights[i].clamp);
		k *= dot(-dir, normal);
		k = clamp(k, 0, abs(k));
		
		spotRes += k * spotLights[i].color * surfColor.xyz;
	}
	
	//color = vec4(0.5 + normal/2, 1.0);
	color = ambientRes + vec4(dirRes, 0) + vec4(pointRes, 0) + vec4(spotRes, 0);
}