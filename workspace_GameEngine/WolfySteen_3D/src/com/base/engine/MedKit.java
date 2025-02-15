package com.base.engine;

public class MedKit 
{
	
	public static final float PICKUP_DISTANCE = 0.5f;
	public static final int HEAL_AMOUNT = 20;
	private static Mesh mesh;
	
	public static final float SCALE = 0.15f;
	public static final float SIZEY = SCALE;
	public static final float SIZEX = (float) ((double)SIZEY / (1.0));
	public static final float START = 0;
	
	
	public static final float OFFSET_X = 0.0f;
	public static final float OFFSET_Y = 0.0f;
	
	public static final float OFFSET_FROM_GROUND = 0.0f;
	
	public static final float TEX_MIN_X = -OFFSET_X;
	public static final float TEX_MAX_X = 0.6f - OFFSET_X;
	
	public static final float TEX_MIN_Y = -OFFSET_Y;
	public static final float TEX_MAX_Y = 0.75f -OFFSET_Y;
	
	public Transform transform;
	
	public static Material material;
	
	public MedKit(Vector3f position)
	{
		if(mesh == null)
		{
			Vertex[] vertices = new Vertex[]
					{
							new Vertex(new Vector3f(-SIZEX,START,START),  new Vector2f(TEX_MAX_X, TEX_MAX_Y)),
							new Vertex(new Vector3f(-SIZEX, SIZEY,START), new Vector2f(TEX_MAX_X,TEX_MIN_Y)),
							new Vertex(new Vector3f(SIZEX, SIZEY,START),  new Vector2f(TEX_MIN_X, TEX_MIN_Y)),
							new Vertex(new Vector3f(SIZEX,START,START),   new Vector2f(TEX_MIN_X, TEX_MAX_Y)),
					};
			
			int[] indices = new int []{0, 1, 2,
									   0, 2, 3,};

			mesh = new Mesh(vertices, indices);
		}
		
		if(material == null)
		{
			material = new Material(new Texture("MEDIA0.png"));
		}
		
		transform = new Transform();
		transform.setTranslation(position);
		
	}
	
	public void update()
	{
		Vector3f directionToCamera = Transform.getCamera().getPos().sub(transform.getTranslation());		
		
		float angleToFaceTheCamera = (float)Math.toDegrees(Math.atan(directionToCamera.getZ() / directionToCamera.getX()));
		
		if(directionToCamera.getX() < 0)
			angleToFaceTheCamera += 180;
		
		transform.getRotation().setY(angleToFaceTheCamera + 90);
		
		if(directionToCamera.length() < PICKUP_DISTANCE)
		{
			Player player = Game.getLevel().getPlayer();
			
			if(player.getHealth() < player.getMaxHealth())	
				Game.getLevel().removeMedKit(this);
				player.damage(-HEAL_AMOUNT);			
		}
	}
	
	public void render()
	{
		Shader shader = Game.getLevel().getShader();
		shader.updateUniforms(transform.getTransformation(), transform.getProjectedTransformation(), material);
		mesh.draw();
	}
	
	public Transform getTransform()
	{
		return transform;
	}
	
}
