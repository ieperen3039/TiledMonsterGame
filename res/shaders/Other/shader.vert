#version 450

struct Matrices {
    mat4 model;
    mat4 view;
    mat4 proj;
    mat4 normal;
};

struct MatProperties {
    vec4 ambient;
    vec4 diffuse;
    vec4 specularShininess;
};

struct LightSource {
    vec4 position;
    vec4 colorPower;
};

layout(binding = 0) uniform UniformBufferObject {
    Matrices matrices;
    MatProperties material;
    LightSource light;
} ubo;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;

layout(location = 0) out vec3 outNormal;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    //    outNormal = (ubo.matrices.normal * vec4(inNormal, 0.0)).xyz;
    //    vec4 normal = ubo.matrices.normal * vec4(inNormal, 0.0);
    //    outNormal = normal.xyz / normal.w;
    outNormal = inNormal;
}
