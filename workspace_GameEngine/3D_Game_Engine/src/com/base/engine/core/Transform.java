package com.base.engine.core;

public class Transform 
{
	private Transform parent;
	
	private Matrix4f parentMatrix;
	
	private Vector3f pos;
	private Quaternion rot;
	private Vector3f scale;
	
	private Vector3f oldpos;
	private Quaternion oldrot;
	private Vector3f oldscale;
	
	public Transform()
	{
		pos = new Vector3f(0,0,0);
		rot = new Quaternion(0,0,0,1);
		scale = new Vector3f(1,1,1);
		
		
		parentMatrix = new Matrix4f().initIdentity();
	}
	
	public void update()
	{
		
		if(oldpos != null)
		{
			oldpos.set(pos);
			oldrot.set(rot);
			oldscale.set(scale);
		}
		else
		{
			oldpos = new Vector3f(0,0,0).set(pos).add(1.0f);
			oldrot = new Quaternion(0,0,0,0).set(rot).mul(0.5f);
			oldscale = new Vector3f(0,0,0).set(scale).add(1.0f);
		}
	}
	
	public void rotate(Vector3f axis, float angle)
	{
		rot = new Quaternion(axis, angle).mul(rot).normalized();
	}
	
	public void lookAt(Vector3f point, Vector3f up)
	{
		rot = getLookAtDirection(point, up);
	}
	
	public Quaternion getLookAtDirection(Vector3f point, Vector3f up)
	{
		return new Quaternion(new Matrix4f().initRotation(point.sub(pos).normalized(), up));
	}
	
	public boolean hasChanged()
	{	
		if(parent != null && parent.hasChanged())
			return true;			
		if(!pos.equals(oldpos))
			return true;
		if(!rot.equals(oldrot))
			return true;
		if(!scale.equals(oldscale))
			return true;
		
		return false;
	}
	
	public Matrix4f getTransformation()
	{
		Matrix4f translationMatrix = new Matrix4f().initTranslation(pos.getX(), pos.getY(), pos.getZ());
		Matrix4f rotationMatrix = rot.toRotationMatrix();
		Matrix4f scaleMatrix = new Matrix4f().initScale(scale.getX(), scale.getY(), scale.getZ());
		
		return getParentMatrix().mul(translationMatrix.mul(rotationMatrix.mul(scaleMatrix)));
	}
	
	private Matrix4f getParentMatrix()
	{
		if(parent != null && parent.hasChanged())
			parentMatrix = parent.getTransformation();
		
		return parentMatrix;
	}
	
	public void setParent(Transform parent)
	{
		this.parent = parent;
	}

	public Vector3f getTransformedPos()
	{
		return getParentMatrix().transform(pos);
	}
	
	public Quaternion getTransformedRot()
	{
		Quaternion parentRotation = new Quaternion(0,0,0,1);
		
		if(parent != null)
			parentRotation = parent.getTransformedRot();
		
		return parentRotation.mul(rot);
	}
	
	public Vector3f getPos() {
		return pos;
	}
	
	public void setPos(Vector3f pos) {
		this.pos = pos;
	}
	
	public Quaternion getRot() {
		return rot;
	}

	public void setRot(Quaternion rotation) {
		this.rot = rotation;
	}
	

	public Vector3f setScale() {
		return scale;
	}

	public void setScale(Vector3f scaling) {
		this.scale = scaling;
	}
	

}
