#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_resolution;
uniform float u_time;
uniform float u_speed;

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

const float outerRadius = 0.4f, innerRadius = 0.1f, outerAlpha = 0.9f;

void main() {
    float cycle = (sin(u_speed*u_time*5.0f)/8.0f)+1.0f;
    vec4 color = vec4(0.0f, 0.0f, 0.0f, 1.0f);

	vec2 relativePosition = gl_FragCoord.xy / u_resolution - 0.5f;
	relativePosition.x *= u_resolution.x / u_resolution.y;
	float len = length(relativePosition);
	color.a = outerAlpha-((smoothstep(outerRadius*cycle, innerRadius*cycle, len))*outerAlpha);

	gl_FragColor = color;
}
