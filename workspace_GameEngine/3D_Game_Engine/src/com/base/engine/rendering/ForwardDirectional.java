package com.base.engine.rendering;

import com.base.engine.core.Matrix4f;
import com.base.engine.core.Transform;

public class ForwardDirectional extends Shader
{
	
	private static final ForwardDirectional instance = new ForwardDirectional();
	
	public static ForwardDirectional getInstance(){return instance;}
	
	public ForwardDirectional()
	{
		super();
		
		AddVertexShaderFromFile("foward-directional.vs.glsl");	
		AddFragmentShaderFromFile("foward-directional.fs.glsl");
		
		setAttribLocation("position", 0);
		setAttribLocation("texCoord", 1);
		setAttribLocation("normal", 2);
		
		compileShader();
		
		AddUniform("model");
		AddUniform("MVP");
		
		AddUniform("specularIntensity");
		AddUniform("specularPower");
		AddUniform("eyePos");
		
		AddUniform("directionalLight.base.color");
		AddUniform("directionalLight.base.intensity");
		AddUniform("directionalLight.direction");
		
	}
	
	
	public void updateUniforms(Transform transform,  Material material)
	{
		Matrix4f worldMatrix = transform.getTransformation();
		Matrix4f projectedMatrix = getRenderingEngine().getMainCamera().getViewProjection().mul(worldMatrix);
		
		
		if(material.getTexture() != null)
			material.getTexture().bind();
		
		setUniform("model", worldMatrix);	
		setUniform("MVP", projectedMatrix);
		
		
		setUniformf("specularIntensity", material.getSpecularIntensity());
		setUniformf("specularPower", material.getSpecularPower());
		
		setUniform("eyePos", getRenderingEngine().getMainCamera().getPos());
		
		setUniform("directionalLight", getRenderingEngine().getDirectionalLight());
	}
	
	public void setUniform(String uniformName, BaseLight baseLight)
	{
		setUniform(uniformName + ".color", baseLight.getColor());
		setUniformf(uniformName + ".intensity", baseLight.getIntensity());
	}
	
	public void setUniform(String uniformName, DirectionalLight directionalLight)
	{
		setUniform(uniformName + ".base", directionalLight.getBase());
		setUniform(uniformName + ".direction", directionalLight.getDirection());
	}
}