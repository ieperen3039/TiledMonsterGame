#version 450

layout (triangles) in;
layout (line_strip, max_vertices = 6) out;

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
    vec4 position;// Position.a = scale
    vec4 colorPower;
};

layout(binding = 0) uniform UniformBufferObject {
    Matrices matrices;
    MatProperties material;
    LightSource light;
} ubo;

layout (location = 0) in vec3 inNormal[3];

layout (location = 0) out vec3 outColor;

void main(void)
{
    float normalLength = 0.05 / ubo.light.position.a;
    for (int i=0; i < gl_in.length(); i++)
    {
        vec3 pos = gl_in[i].gl_Position.xyz;
        vec3 normal = normalize(inNormal[i].xyz);

        gl_Position = ubo.matrices.proj * ubo.matrices.view * ubo.matrices.model * vec4(pos, 1.0);
        outColor = vec3(1.0, 0.0, 0.0);
        EmitVertex();

        gl_Position = ubo.matrices.proj * ubo.matrices.view * ubo.matrices.model * vec4(pos + normal * normalLength, 1.0);
        outColor = vec3(0.0, 0.0, 1.0);
        EmitVertex();

        EndPrimitive();
    }
}
