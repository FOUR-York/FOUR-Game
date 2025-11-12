#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_resolution;
uniform float u_time;
uniform float u_cycle;

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

const float outerRadius = 0.4, innerRadius = 0.1, outerAlpha = 0.9;

void main() {
    float cycle = u_cycle;
    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

	vec2 relativePosition = gl_FragCoord.xy / u_resolution - 0.5;
	relativePosition.x *= u_resolution.x / u_resolution.y;
	float len = length(relativePosition);
	color.a = outerAlpha-((smoothstep(outerRadius*cycle, innerRadius*cycle, len))*outerAlpha);

	gl_FragColor = color;
}
