package com.base.engine;
import java.awt.Canvas;
import javax.swing.JOptionPane;
import javax.xml.crypto.Data;

import com.base.net.GameClient;
import com.base.net.GameServer;

public class MainComponent extends Canvas
{
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final String TITLE = "3D Game Engine";
	public static final double FRAME_CAP = 5000.0;
	
	private boolean isRunning;
	private Game game;
	
//	public GameServer socketServer;
//	public GameClient socketClient;

	public MainComponent()
	{
		System.out.println(RenderUtil.getOpenGLVersion());
		RenderUtil.initGraphics();

		isRunning = false;
		game = new Game();
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
		long frameCounter = 0;
		
		final double frameTime = 1.0/FRAME_CAP;
		
		long lastTime = Time.getTime();
		double unproccesedTime = 0;
	
		while(isRunning)
		{
			boolean render = false;
			
			long startTime = Time.getTime();
			long passedTime = startTime - lastTime;
			lastTime = startTime;
			
			unproccesedTime += passedTime / (double)Time.SECOND;
			frameCounter += passedTime;
			
			while(unproccesedTime > frameTime)
			{
				render = true;
				
				unproccesedTime -= frameTime;
				
				if(Window.isCloseRequested())
					Stop();
				
				Time.setDelta(frameTime);
				
				game.Input();
				Input.Update();
				game.Update();
				
				if(frameCounter >= Time.SECOND)
				{
					//System.out.println(frames);
					frames = 0;
					frameCounter = 0;
				}
			}
			
			if(render)
			{
				Render();
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
	
	private void Render()
	{
		RenderUtil.clearScreen();
		game.Render();
		Window.render();
	}
	
	private void CleanUp()
	{
		Window.dispose();
	}
	
	public static void main(String[] args)
	{
		Window.createWindow(WIDTH, HEIGHT, TITLE);
		
		MainComponent game = new MainComponent();
		game.Start();
	}
}
