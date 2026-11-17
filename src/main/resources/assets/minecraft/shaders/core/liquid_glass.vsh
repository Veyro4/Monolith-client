#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 local;
out vec4 vertexColor;
out float packedSize;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);
    // UV0 carries the position inside the rect in unscaled GUI units, so the
    // distance field stays exact when the GUI is scaled (open/hover/bounce animations).
    local = UV0;
    packedSize = Position.z;
    vertexColor = Color;
}
