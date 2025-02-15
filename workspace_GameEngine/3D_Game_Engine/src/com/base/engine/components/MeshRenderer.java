package com.base.engine.components;

import com.base.engine.rendering.Material;
import com.base.engine.rendering.Mesh;
import com.base.engine.rendering.RenderingEngine;
import com.base.engine.rendering.Shader;

public class MeshRenderer extends GameComponent
{
	private Material material;
	private Mesh mesh;
	
	public MeshRenderer(Mesh mesh, Material material)
	{
		this.mesh = mesh;
		this.material = material;
	}
	
	@Override
	public void render(Shader shader, RenderingEngine renderingEngine)
	{
		//transform.getTransformation(), transform.getProjectedTransformation()
		shader.bind();
		shader.updateUniforms(getTransform(), material, renderingEngine);
		mesh.draw();		
	}

}
