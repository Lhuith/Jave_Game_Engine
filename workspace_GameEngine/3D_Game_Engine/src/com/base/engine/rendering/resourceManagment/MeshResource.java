package com.base.engine.rendering.resourceManagment;

import static org.lwjgl.opengl.GL15.*;

public class MeshResource 
{
	private int vbo;
	private int ibo;
	private int size;
	private int refCount;
	
	public MeshResource(int size)
	{
		vbo = glGenBuffers();
		ibo = glGenBuffers();
		this.size = size;
		this.refCount = 1;
	}
	
	@Override
	protected void finalize()
	{

		glDeleteBuffers(vbo);
		glDeleteBuffers(ibo);
	}
	
	public void addRefrence()
	{
		refCount++;
	}
	
	public boolean removedRefrence()
	{
		refCount--;
		return refCount == 0;
	}
	
	public int getVbo() {
		return vbo;
	}


	public int getIbo() {
		return ibo;
	}

	public int getSize() {
		return size;
	}
}
