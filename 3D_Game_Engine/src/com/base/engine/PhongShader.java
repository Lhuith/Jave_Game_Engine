package com.base.engine;

public class PhongShader extends Shader
{
	private static final PhongShader instance = new PhongShader();
	
	public static PhongShader getInstance()
	{
		return instance;
	}
	
	public PhongShader()
	{
		super();
		
		AddVertexShader(ResourceLoader.loadShader("phong.vs.glsl"));	
		AddFragmentShader(ResourceLoader.loadShader("phong.fs.glsl"));
		compileShader();
		
		AddUniform("transform");
		AddUniform("color");
	}
	
	
	public void updateUniforms(Matrix4f worldMatrix, Matrix4f projectedMatrix,  Material material)
	{
		if(material.getTexture() != null)
		{
			material.getTexture().bind();
		}
		else
			RenderUtil.unbindTextures();

		
		setUniform("transform", projectedMatrix);
		setUniform("color", material.getColor());
		
	}
}