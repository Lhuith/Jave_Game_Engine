package com.base.engine.core;
import java.awt.Canvas;

import com.base.engine.rendering.RenderingEngine;
import com.base.engine.rendering.Window;

public class CoreEngine extends Canvas
{
	private boolean isRunning;
	private Game game;
	private RenderingEngine renderingEngine;
	private int width;
	private int height;
	
	private double frameTime;
	
	public CoreEngine(int width, int height, double framerate, Game game)
	{		
		isRunning = false;
		this.game = game;
		this.width = width;
		this.height = height;
		this.frameTime = 1.0/framerate;
		game.setEngine(this);
	}
	
	public void createWindow(String title)
	{
		Window.createWindow(width, height, title);
		this.renderingEngine = new RenderingEngine();
	}
	
	public void Start()
	{
		
		if(isRunning)
			return;
		
		Run();		
		
	}
	
	public void Stop()
	{
		if(!isRunning)
			return;
		
		isRunning = false;
	}
	
	private void Run()
	{
		isRunning = true;
		
		int frames = 0;
		double frameCounter = 0;
		
		game.init();
		
		double lastTime = Time.getTime();
		double unproccesedTime = 0;
	
		while(isRunning)
		{
			boolean render = false;
			
			double startTime = Time.getTime();
			double passedTime = startTime - lastTime;
			lastTime = startTime;
			
			unproccesedTime += passedTime;
			frameCounter += passedTime;
			
			while(unproccesedTime > frameTime)
			{
				render = true;
				
				unproccesedTime -= frameTime;
				
				if(Window.isCloseRequested())
					Stop();
				
				game.input((float)frameTime);
				Input.Update();
				
				game.update((float)frameTime);

				if(frameCounter >= 1.0f)
				{
					System.out.println(frames);
					frames = 0;
					frameCounter = 0;
				}
			}
			
			if(render)
			{
				game.render(renderingEngine);
				Window.render();
				frames ++;
			}
			else
				try {
					Thread.sleep(1);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
		}
		
		CleanUp();			
	}
	
	private void CleanUp()
	{
		Window.dispose();
	}
	
	public RenderingEngine getRenderingEngine()
	{
		return renderingEngine;
	}
}
