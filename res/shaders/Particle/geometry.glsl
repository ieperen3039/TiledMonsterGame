#version 450

layout (points) in;
layout (triangle_strip, max_vertices = 3) out;

in vec3 geoMiddle;// position of middle
in vec4 geoColor;
in vec2 geoBeginEndTime;// (beginTime, endTime)
in int randomNumber;

out vec4 fragColor;

int randomState;
const vec3 A = vec3(1.0, 0.0, 0.0);
const vec3 B = vec3(0.7, 0.0, 0.0);// cos(60)
const vec3 C = vec3(-0.7, 0.0, 0.0);

/* The state word must be initialized to non-zero 
 */
float rand() {
    int x = randomState;
    /* Algorithm "xor" from p. 4 of Marsaglia, "Xorshift RNGs" */
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    randomState = x;
    return (x / 0x7fffffff);
}

void generateVertex(vec3 rel){
    fragColor = geoColor;
    float t = currentTime - geoBeginEndTime.x;

    vec3 rot = vec3(rand(), rand(), rand());
    float angle = t * rand() + rand();
    float sin = sin(angle);
    float cos = cos(angle);

    rel = vec3(
    rel.x * cos + sin * (rot.y * rel.z - rot.z * rel.y) + (1.0 - cos) * rot.x,
    rel.y * cos + sin * (rot.z * rel.x - rot.x * rel.z) + (1.0 - cos) * rot.y,
    rel.z * cos + sin * (rot.x * rel.y - rot.y * rel.x) + (1.0 - cos) * rot.z
    );

    gl_Position = vec4(geoMiddle + rel, 1.0);
    EmitVertex();
}

void main() {
    randomState = randomNumber;// set random seed

    // only draw if still visible
    if (currentTime < beginEndTime.y) {

        generateVertex(A);
        generateVertex(B);
        generateVertex(C);

        EndPrimitive();
    }
}
