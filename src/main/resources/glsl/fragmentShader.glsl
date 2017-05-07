#version 330 core
in vec4 vertexColor;
in vec2 TexCoord;

out vec4 color;

uniform sampler2D ourTexture1;
uniform sampler2D ourTexture2;

void main() {
  vec2 invertedTexCoord = vec2(TexCoord.x, 1.0f - TexCoord.y);
  color = mix(texture(ourTexture1, TexCoord), texture(ourTexture2, invertedTexCoord), 0.2) * vertexColor;
}
