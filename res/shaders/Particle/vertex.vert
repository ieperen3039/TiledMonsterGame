#version 330

layout (location = 0) in vec3 middle;// position of middle
layout (location = 1) in vec3 movement;
layout (location = 2) in vec4 color;
layout (location = 3) in vec2 beginEndTime;// (beginTime, endTime)
layout (location = 4) in int randomInt;// (beginTime, endTime)

uniform mat4 viewProjectionMatrix;
uniform float currentTime;

out vec3 geoMiddle;// position of middle
out vec4 geoColor;
out vec2 geoBeginEndTime;// (beginTime, endTime)
out int randomNumber;

void main(){
    float t = currentTime - beginEndTime.x;
    vec4 position = vec4(middle + (movement * t), 1.0);
    // calculate vertex position in world-space
    geoMiddle = (viewProjectionMatrix * position).xyz;
    geoBeginEndTime = beginEndTime;
    geoColor = color;
    randomNumber = randomInt;
}
