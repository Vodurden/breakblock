#version 330 core
layout (location = 0) in vec2 position;
layout (location = 1) in vec2 texCoords;

out vec2 TexCoords;

uniform mat4 transform; // Applies translation, rotation and scaling
uniform mat4 projection; // Project from world coordinates to normalized coordinates

void main() {
  TexCoords = texCoords;
  gl_Position = projection * transform * vec4(position, 0.0, 1.0);
}
