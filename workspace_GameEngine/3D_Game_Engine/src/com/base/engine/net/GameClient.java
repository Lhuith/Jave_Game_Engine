package com.base.engine.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.base.engine.core.CoreEngine;

public class GameClient extends Thread
{
	//in order to get this to run online, in socketClient = new GameClient(this, "YOURPUBLICIP"); , 
	//instead of YOURPUBLICIP, just google search "my public IP" and an address will come up.  
	//This is your actual public IP.  Then, in your router settings, port forward 1331.  I was then 
	//able to have my friends join me in my (currently very buggy) game.﻿

	
	private InetAddress ipAddress;
	private DatagramSocket socket;
	private CoreEngine mc;
	
	public GameClient(CoreEngine mc, String ipAddress)
	{
		this.mc = mc;
		
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		}catch(UnknownHostException e){
			e.printStackTrace();
		}
	}
	
	public void run(){
		while(true)
		{
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			System.out.println("SERVER > " + new String(packet.getData()));
		}
	}
	
	
	public void sendData(byte[] data)
	{
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1332);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
