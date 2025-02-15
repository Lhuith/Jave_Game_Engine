package com.base.engine.rendering;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Resource;

import com.base.engine.components.BaseLight;
import com.base.engine.components.DirectionalLight;
import com.base.engine.components.PointLight;
import com.base.engine.components.SpotLight;
import com.base.engine.core.Matrix4f;
import com.base.engine.core.Transform;
import com.base.engine.core.Util;
import com.base.engine.core.Vector3f;
import com.base.engine.rendering.resourceManagment.ShaderResource;
import com.base.engine.rendering.resourceManagment.TextureResource;

public class Shader 
{
	private static HashMap<String, ShaderResource> loadedShaders = new HashMap<String, ShaderResource>();
	
	private ShaderResource resource;

	private String fileName;
	
	public Shader(String fileName)
	{
		
		this.fileName = fileName;
		ShaderResource oldResource = loadedShaders.get(fileName);
		
		if(oldResource != null)
		{
			resource = oldResource;
			resource.addRefrence();
		}
		else
		{
			resource = new ShaderResource();

			String vertexShaderText = loadShader(fileName + ".vs.glsl");
			String fragmentShaderText = loadShader(fileName +".fs.glsl");
			
			AddVertexShader(vertexShaderText);	
			AddFragmentShader(fragmentShaderText);
			
			addAllAttributes(vertexShaderText);
			
			compileShader();
			
			addAllUniforms(vertexShaderText);
			addAllUniforms(fragmentShaderText);
		}
		
	}
	
	public void bind()
	{
		glUseProgram(resource.getProgram());
	}
	
	public void updateUniforms(Transform transform, Material material, RenderingEngine renderingEngine)
	{
		Matrix4f ModelMatrix = transform.getTransformation();
		Matrix4f MVPMatrix = renderingEngine.getMainCamera().getViewProjection().mul(ModelMatrix);
		
		for(int i = 0; i < resource.getUniformNames().size(); i++)
		{
			String uniformName = resource.getUniformNames().get(i);
			String uniformType = resource.getUniformTypes().get(i);
			
			
			if(uniformType.equals("sampler2D"))
			{		
				int samplerSlot = renderingEngine.getSamplerSlot(uniformName);					
				material.getTexture(uniformName).bind(samplerSlot);
				setUniformi(uniformName, samplerSlot);
			}
			else if(uniformName.startsWith("T_"))
			{
				if(uniformName.equals("T_MVP"))
					setUniform(uniformName, MVPMatrix);
				else if(uniformName.equals("T_Model"))
					setUniform(uniformName, ModelMatrix);
				else
					throw new IllegalArgumentException(uniformName + " is not a valid component of Transform");
			}
			else if(uniformName.startsWith("R_"))
			{
				String unprefixedUniformName = uniformName.substring(2);
				if(uniformType.equals("vec3"))
					setUniform(uniformName, renderingEngine.getVector3f(unprefixedUniformName));
				else if(uniformType.equals("float"))
					setUniformf(uniformName, renderingEngine.getFloat(unprefixedUniformName));	
				else if(uniformType.equals("DirectionalLight"))
					setUniformDirectionalLight(uniformName, (DirectionalLight)renderingEngine.getActivelight());
				else if(uniformType.equals("PointLight"))
					setUniformPointLight(uniformName, (PointLight)renderingEngine.getActivelight());
				else if(uniformType.equals("SpotLight"))
					setUniformSpotLight(uniformName, (SpotLight)renderingEngine.getActivelight());
				else
					renderingEngine.updateUniformStruct(transform, material, this, uniformName, uniformType);
					
			}
			else if(uniformName.startsWith("C_"))
			{
				if(uniformName.equals("C_eyePos"))
					setUniform(uniformName,  renderingEngine.getMainCamera().getTransform().getTransformedPos());
				else
					throw new IllegalArgumentException(uniformName + " is not a valid component of Camera");
			}
			else
			{
			    if(uniformType.equals("vec3"))
					setUniform(uniformName, material.getVector3f(uniformName));
				else if(uniformType.equals("float"))
					setUniformf(uniformName, material.getFloat(uniformName));
				else
					throw new IllegalArgumentException(uniformName + " is not a valid component of Material");
			}
		}
	}
		
	private class GLSLStruct
	{
		public String name;
		public String type;
	}
	
	private HashMap<String, ArrayList<GLSLStruct>> findUniformStructs(String shaderText)
	{
		
		HashMap<String, ArrayList<GLSLStruct>> result = new HashMap<String, ArrayList<GLSLStruct>>();
		
		final String STRUCT_KEYWORD = "struct";
		int structStartLocation = shaderText.indexOf(STRUCT_KEYWORD);
		while(structStartLocation != -1)
		{
			
			if(!(structStartLocation != 0
					&& (Character.isWhitespace( shaderText.charAt(structStartLocation - 1)) || shaderText.charAt(structStartLocation - 1) == ';')
					&& Character.isWhitespace(shaderText.charAt(structStartLocation + STRUCT_KEYWORD.length()))))
				continue;
			
			int nameBegin = structStartLocation + STRUCT_KEYWORD.length() + 1;
			int braceBegin = shaderText.indexOf("{", nameBegin); 
			int braceEnd = shaderText.indexOf("}", braceBegin + 1);
			
			String structName = shaderText.substring(nameBegin, braceBegin).trim();			
			ArrayList<GLSLStruct> glslStructs = new ArrayList<GLSLStruct>();
			
			int componentSemiColonPos = shaderText.indexOf(";", braceBegin);
			
			while(componentSemiColonPos != -1 && componentSemiColonPos < braceEnd)
			{				
				int componentNameEnd = componentSemiColonPos + 1;
				
				while(Character.isWhitespace(shaderText.charAt(componentNameEnd - 1)) || shaderText.charAt(componentNameEnd - 1) == ';')
					componentNameEnd--;
				
				int componentNameStart = componentSemiColonPos;
				
				while(!Character.isWhitespace(shaderText.charAt(componentNameStart - 1)))
					componentNameStart--;
							
				int componentTypeEnd = componentNameStart;
				
				while(Character.isWhitespace(shaderText.charAt(componentTypeEnd - 1)))
					componentTypeEnd--;
				
				int componentTypeStart = componentTypeEnd;			
				
				while(!Character.isWhitespace(shaderText.charAt(componentTypeStart - 1)))
					componentTypeStart--;
				
				
				String componentName = shaderText.substring(componentNameStart, componentNameEnd);
				String componentType = shaderText.substring(componentTypeStart, componentTypeEnd);

				
				GLSLStruct glslStruct = new GLSLStruct();
				glslStruct.name = componentName;
				glslStruct.type = componentType;
				
				glslStructs.add(glslStruct);
				
				componentSemiColonPos = shaderText.indexOf(";", componentSemiColonPos + 1);
				
			}
			
			result.put(structName, glslStructs);
			
			structStartLocation = shaderText.indexOf(STRUCT_KEYWORD, structStartLocation + STRUCT_KEYWORD.length());
		}
		
		return result;
	}
	
	private void addAllAttributes(String shaderText)
	{
		final String ATTRIBUTE_KEYWORD = "attribute";
		int attribStartLocation = shaderText.indexOf(ATTRIBUTE_KEYWORD);
		int atrribNumber = 0;
		while(attribStartLocation != -1)
		{
			
			if(!(attribStartLocation != 0
					&& (Character.isWhitespace( shaderText.charAt(attribStartLocation - 1)) || shaderText.charAt(attribStartLocation - 1) == ';')
					&& Character.isWhitespace(shaderText.charAt(attribStartLocation + ATTRIBUTE_KEYWORD.length()))))
				continue;
			
			int begin = attribStartLocation + ATTRIBUTE_KEYWORD.length() + 1;
			int end = shaderText.indexOf(";", begin);
			
			String attribLine = shaderText.substring(begin, end).trim();
			String attribName = attribLine.substring(attribLine.indexOf(' ') + 1, attribLine.length()).trim();
			
			setAttribLocation(attribName, atrribNumber);
			//AddUniform(attribName);
			atrribNumber++;
			attribStartLocation = shaderText.indexOf(ATTRIBUTE_KEYWORD, attribStartLocation + ATTRIBUTE_KEYWORD.length());
		}
	}
	
	private void addAllUniforms(String shaderText)
	{
		
		HashMap<String, ArrayList<GLSLStruct>> structs = findUniformStructs(shaderText);
		
		final String UNIFORM_KEYWORD = "uniform";
		int uniformStartLocation = shaderText.indexOf(UNIFORM_KEYWORD);
		
		while(uniformStartLocation != -1)
		{
			
			if(!(uniformStartLocation != 0
					&& (Character.isWhitespace(shaderText.charAt(uniformStartLocation - 1)) || shaderText.charAt(uniformStartLocation - 1) == ';')
					&& Character.isWhitespace(shaderText.charAt(uniformStartLocation + UNIFORM_KEYWORD.length()))))
				continue;
			
			int begin = uniformStartLocation + UNIFORM_KEYWORD.length() + 1;
			int end = shaderText.indexOf(";", begin);
			
			String uniformLine = shaderText.substring(begin, end);
			
			int whiteSpacePos = uniformLine.indexOf(' ');			
			String uniformName = uniformLine.substring(whiteSpacePos + 1, uniformLine.length()).trim();
			String uniformType = uniformLine.substring(0, whiteSpacePos).trim();

			resource.getUniformNames().add(uniformName);
			resource.getUniformTypes().add(uniformType);
			addUniform(uniformName, uniformType, structs);

			uniformStartLocation = shaderText.indexOf(UNIFORM_KEYWORD, uniformStartLocation + UNIFORM_KEYWORD.length());
		}
	}
	
	private void addUniform(String uniformName, String uniformType, 	HashMap<String, ArrayList<GLSLStruct>> structs)
	{
			boolean addThis = true;
			ArrayList<GLSLStruct> structComponents = structs.get(uniformType);
			
			if(structComponents != null)
			{
				addThis = false;
				
				for(GLSLStruct struct : structComponents)
					addUniform(uniformName + "." + struct.name, struct.type, structs);			
			}
			
			if(!addThis)
				return;//AddUniform(uniformName);
			
			int uniformLocation = glGetUniformLocation(resource.getProgram(), uniformName);
			
			if(uniformLocation == 0xFFFFFFFF)
			{
				System.err.println("Error: Could not find uniform: " + uniformName);
				new Exception().printStackTrace();
				System.exit(1);
			}
			
			resource.getUniforms().put(uniformName, uniformLocation);
			
	}

	private void AddVertexShader(String text)
	{
		addProgram(text, GL_VERTEX_SHADER);
	}
	
	private void AddGeometryShader(String text)
	{
		addProgram(text, GL_GEOMETRY_SHADER);
	}
	
	private void AddFragmentShader(String text)
	{
		addProgram(text, GL_FRAGMENT_SHADER);
	}
	
	private void setAttribLocation(String attributeName, int location)
	{
		glBindAttribLocation(resource.getProgram(), location, attributeName);
	}
	
	private void compileShader()
	{
		glLinkProgram(resource.getProgram());
		
		if(glGetProgram(resource.getProgram(), GL_LINK_STATUS) == 0)
		{
			System.err.println(glGetProgramInfoLog(resource.getProgram(), 1024));
			System.exit(1);
		}
		
		glValidateProgram(resource.getProgram());
		
		if(glGetProgram(resource.getProgram(), GL_VALIDATE_STATUS) == 0)
		{
			System.err.println(glGetProgramInfoLog(resource.getProgram(), 1024));
			System.exit(1);
		}
	}
	
	@Override
	protected void finalize()
	{
		if(resource.removedRefrence() && !fileName.isEmpty())
		{
			loadedShaders.remove(fileName);
		}
	}
	
	
	private void addProgram(String text, int type)
	{
		int shader = glCreateShader(type);
		
		System.out.println(shader);
		
		if(shader == 0)
		{
			System.err.println("Shader Creation Failed: Could not find valid memory location when adding shader");
			System.exit(1);
		}
		
		glShaderSource(shader, text);
		glCompileShader(shader);
		
		if(glGetShader(shader, GL_COMPILE_STATUS) == 0)
		{
			System.err.println(glGetShaderInfoLog(shader, 1024));
			System.exit(1);
		}
		
		glAttachShader(resource.getProgram(), shader);
	}

	public void setUniformi(String uniformName, int value)
	{
		glUniform1i(resource.getUniforms().get(uniformName), value);
	}
	
	public void setUniformf(String uniformName, float value)
	{
		glUniform1f(resource.getUniforms().get(uniformName), value);
	}
	
	public void setUniform(String uniformName, Vector3f value)
	{
		glUniform3f(resource.getUniforms().get(uniformName), value.getX(), value.getY(), value.getZ());
	}
	
	public void setUniform(String uniformName, Matrix4f value)
	{
		glUniformMatrix4(resource.getUniforms().get(uniformName), true, Util.createFlippedBuffer(value));
	}
	
	
	public void setUniformBaseLight(String uniformName, BaseLight baseLight)
	{
		setUniform(uniformName + ".color", baseLight.getColor());
		setUniformf(uniformName + ".intensity", baseLight.getIntensity());
	}
	
	public void setUniformDirectionalLight(String uniformName, DirectionalLight directionalLight)
	{
		setUniformBaseLight(uniformName + ".base", directionalLight);
		setUniform(uniformName + ".direction", directionalLight.getDirection());
	}
	
	
	public void setUniformPointLight(String uniformName, PointLight pointLight)
	{
		setUniformBaseLight(uniformName + ".base", pointLight);
		setUniformf(uniformName + ".atten.constant", pointLight.getAttenuation().getConstant());
		setUniformf(uniformName + ".atten.linear", pointLight.getAttenuation().getLinear());
		setUniformf(uniformName + ".atten.exponent", pointLight.getAttenuation().getExponent());		
		setUniform(uniformName + ".position", pointLight.getTransform().getTransformedPos());
		setUniformf(uniformName + ".range", pointLight.getRange());
	}
	
	public void setUniformSpotLight(String uniformName, SpotLight spotLight)
	{
		setUniformPointLight(uniformName + ".pointLight", spotLight);
		setUniform(uniformName + ".direction", spotLight.getDirection());
		setUniformf(uniformName + ".cutoff", spotLight.getCutoff());
	}
	
	private static String loadShader(String fileName)
	{
		StringBuilder shaderSource = new StringBuilder();
		BufferedReader shaderReader = null;
		final String INCLUDE_DIRECTIVE = "#include";
		
		try
		{
			shaderReader = new BufferedReader(new FileReader("./res/shaders/" + fileName));
			String line;
			
			while((line = shaderReader.readLine()) != null)
			{
				if(line.startsWith(INCLUDE_DIRECTIVE))
				{
					shaderSource.append(loadShader(line.substring(INCLUDE_DIRECTIVE.length() + 2, line.length() - 1)));
				}
				else
					shaderSource.append(line).append("\n");
			}		
			
			shaderReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		return shaderSource.toString();
	}

}
