#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec2 local;
in vec4 vertexColor;
in float packedSize;

out vec4 fragColor;

#ifndef RADIUS
#define RADIUS 16.0
#endif

// Soft outer shadow reach in GUI px (the Java side grows the quad by the same amount).
#define SHADOW_SPREAD 12.0

float rounded(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

// Stable per-position hash for the frost grain (no texture needed).
float hash(vec2 p) {
    p = fract(p * vec2(443.897, 441.423));
    p += dot(p, p.yx + 19.19);
    return fract((p.x + p.y) * p.x);
}

void main() {
    float w = floor(mod(packedSize, 4096.0) + 0.5);
    float h = floor(mod(packedSize / 4096.0, 4096.0) + 0.5);
    vec2 halfSize = vec2(w, h) * 0.5;
    float rad = min(RADIUS, min(halfSize.x, halfSize.y));
    float d = rounded(local - halfSize, halfSize, rad);
    float px = max(fwidth(d), max(fwidth(local.x), fwidth(local.y)));
    float aa = clamp(px <= 0.001 ? 1.0 : px, 0.75, 1.35);

#if defined(SHADOW)
    // Only outside the shape, so it never darkens the glass itself.
    float outside = smoothstep(-aa, aa, d);
    float fall = 1.0 - clamp(d / SHADOW_SPREAD, 0.0, 1.0);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * outside * fall * fall) * ColorModulator;
#elif defined(STROKE)
    float mask = smoothstep(-1.7 - aa, -1.7 + aa, d) * (1.0 - smoothstep(-aa, 0.0, d));
    fragColor = vec4(vertexColor.rgb, vertexColor.a * mask) * ColorModulator;
#elif defined(GLASS)
    float mask = 1.0 - smoothstep(-aa, aa, d);
    vec2 uv = local / max(vec2(w, h), vec2(1.0));
    float inside = max(-d, 0.0);
    vec3 col = vertexColor.rgb;
    float alpha = vertexColor.a;
    float sheen = pow(1.0 - smoothstep(0.0, 0.32, uv.y), 1.5) * 0.09;
    float edge = exp(-inside / 5.5) * 0.045;
    col += vec3(sheen + edge);
    alpha = clamp(alpha + sheen * 0.35 + edge * 0.2, 0.0, 1.0);
    fragColor = vec4(clamp(col, 0.0, 1.0), alpha * mask) * ColorModulator;
#else
    float mask = 1.0 - smoothstep(-aa, aa, d);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * mask) * ColorModulator;
#endif
}
