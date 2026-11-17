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
    float rad = min(RADIUS, min(halfSize.x, halfSize.y) - 0.5);
    float d = rounded(local - halfSize, halfSize, rad);
    float px = max(max(fwidth(local.x), fwidth(local.y)), 0.0001);
    float aa = max(fwidth(d) * 0.6, px * 0.5);

#if defined(SHADOW)
    // Only outside the shape, so it never darkens the glass itself.
    float outside = smoothstep(-aa, aa, d);
    float fall = 1.0 - clamp(d / SHADOW_SPREAD, 0.0, 1.0);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * outside * fall * fall) * ColorModulator;
#elif defined(STROKE)
    // Line centred half a pixel inside the edge, so all of it lies inside the quad.
    // Centred on the edge, half of it was cut off along the straight sides and only
    // the rounded corners showed the full line.
    float mask = 1.0 - smoothstep(0.5 - aa, 0.5 + aa, abs(d + 0.5));
    fragColor = vec4(vertexColor.rgb, vertexColor.a * mask) * ColorModulator;
#elif defined(GLASS)
    float mask = 1.0 - smoothstep(-aa, aa, d);
    float inside = max(-d, 0.0);
    vec2 uv = local / max(vec2(w, h), vec2(1.0));
    // Outward edge direction from the distance field, for light that comes from the top-left.
    vec2 grad = vec2(dFdx(d), dFdy(d));
    vec2 n = length(grad) > 1e-5 ? normalize(grad) : vec2(0.0, -1.0);
    float lit = clamp(dot(n, normalize(vec2(-0.45, -1.0))), 0.0, 1.0);
    float shade = clamp(dot(n, normalize(vec2(0.45, 1.0))), 0.0, 1.0);

    vec3 col = vertexColor.rgb;
    float alpha = vertexColor.a;
#ifdef FROST
    // Frosted: milky veil and a bit more body.
    col = mix(col, vec3(0.80, 0.84, 0.90), 0.05);
    alpha = mix(alpha, 1.0, 0.10);
#endif
    // Sheen: soft reflection across the top of the pane.
    float sheen = (1.0 - smoothstep(0.0, 0.6, uv.y)) * 0.045;
    // Thickness: light gathers just inside the edges.
    float glow = exp(-inside / 9.0) * 0.05;
    // Refraction rim: thin bright line on the lit edges, faint dark line on the far side.
    float rim = exp(-inside / 1.3);
    float rimLight = rim * (0.08 + 0.42 * lit);
    float rimShade = exp(-inside / 2.5) * shade * 0.22;

    col += vec3(sheen + glow);
    col = mix(col, vec3(1.0), rimLight);
    col *= 1.0 - rimShade;
#ifdef FROST
    float grainAmt = 0.035;
#else
    float grainAmt = 0.02;
#endif
    // Grain on a ~1 px grid that follows the panel, so it doesn't crawl when things animate.
    col += (hash(floor(local * 2.0)) - 0.5) * grainAmt;
    alpha = clamp(alpha + rimLight * 0.55 + sheen * 0.6 + glow * 0.4, 0.0, 1.0);
    fragColor = vec4(clamp(col, 0.0, 1.0), alpha * mask) * ColorModulator;
#else
    float mask = 1.0 - smoothstep(-aa, aa, d);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * mask) * ColorModulator;
#endif
}
