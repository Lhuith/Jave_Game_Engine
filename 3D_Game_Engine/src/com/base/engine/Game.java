package com.base.engine;

import org.lwjgl.input.Keyboard;

public class Game 
{
	private Mesh mesh;
	private Shader shader;
	private Transform transform;
	
	
	public Game()
	{
		mesh = ResourceLoader.loadMesh("Cube.obj");//new Mesh();
		shader = new Shader();
		
	//	Vertex[] vertices = new Vertex[] {
////				new Vertex(new Vector3f(-1, -1, +0)),
  //              new Vertex(new Vector3f(+0, +1, +0)),
	//		    new Vertex(new Vector3f(1, -1, +0)),
	//		    new Vertex(new Vector3f(0, -1, 1))};
	//	
	//	int[] indices = new int[] {0, 1, 3, 
	//							   3, 1, 2, 
	///							   2, 1, 0,
	//							   0, 2, 3};
		
	//	mesh.AddVertices(vertices, indices);
		
		transform = new Transform();
		Transform.setProjection(70f, Window.getWidth(), Window.GetHeight(), 0.1f, 1000);
		
		shader.AddVertexShader(ResourceLoader.loadShader("basic.vs.glsl"));	
		shader.AddFragmentShader(ResourceLoader.loadShader("basic.fs.glsl"));

		shader.compileShader();
		
		shader.AddUniform("transform");
	}
	
	public void Input()
	{
		if(Input.GetKeyDown(Keyboard.KEY_UP))
			System.out.println("We just pressed Up!!!");
		
		if(Input.GetKeyUp(Keyboard.KEY_UP))
			System.out.println("We just Released Up!!!");
		
		
		if(Input.GetMouseDown(0))
			System.out.println("We just pressed LeftMouseButton!!!" + Input.GetMousePosition().toString());
		
		if(Input.GetMouseUp(0))
			System.out.println("We just released LeftMouseButton!!!");
	}
	
	float temp = 0.0f;
	
	public void Update()
	{
		temp += Time.getDelta();
		
		float sin = (float)Math.sin(temp);
		
		transform.setTranslation(0, 0 , 5);
		transform.setRotation(0, sin * 180 , 0);
		//transform.setScale(0.5f, 0.5f , 0.5f);
	}
	
	public void Render()
	{
		shader.bind();
		shader.setUniform("transform", transform.getProjectedTransformation());
		mesh.draw();
	}
}
