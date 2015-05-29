package com.clearwire.tools.mobile.aiat.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UDPDownlinkTrafficServer extends Thread {
	
private static Log log = LogFactory.getLog(UDPUplinkTrafficServer.class);
	
	private int BUFFER_SIZE = 16 * 1024;
		
	private ServerSocket udpControlSocket;
	
	private DatagramSocket udpDownlinkSocket;
	
	private boolean running=true;
	
	public UDPDownlinkTrafficServer(){
		super();
		try {
			udpControlSocket = new ServerSocket(6792);
			udpDownlinkSocket = new DatagramSocket(6793);
		} catch (IOException e) {
			log.error("Failed to start traffic server.", e);
		}
	}
	
	public void run() {
		
		while(running){
			try {
				Socket newSocket = udpControlSocket.accept();
				log.info("Received UDP downlink traffic client connection.");
				
				SendDataThread sendDataThread = new SendDataThread(newSocket);
				(new CheckControlThread(newSocket, sendDataThread)).start();
				sendDataThread.start();
			}catch (IOException e) {
				log.error("Error accepting connection.", e);
			}
		}
		
	}
	
	public void shutdown(){
		running=false;
	}
	
	class SendDataThread extends Thread {
		
		private Socket socket;
		
		private boolean threadRunning=true;
		
		private byte[] data = new byte[BUFFER_SIZE];

		public SendDataThread(Socket socket) {
			super();
			this.socket = socket;
		}
		
		public void run(){
			
			try {
				DatagramPacket packet = new DatagramPacket(data, BUFFER_SIZE);
				packet.setAddress(socket.getInetAddress());
				while(running&&threadRunning)
					udpDownlinkSocket.send(packet);
				udpDownlinkSocket.close();
				log.info("Client disconnected: "+socket.getInetAddress().toString());
			} catch (IOException e) {
				log.error("Client disconnected: "+socket.getInetAddress().toString(), e);
			}
		}
		
		public void shutdownThread(){
			threadRunning=false;
		}
	}
	
	class CheckControlThread extends Thread {
		
		private Socket socket;
		private SendDataThread sendDataThread;
		
		public CheckControlThread(Socket socket,SendDataThread sendDataThread) {
			super();
			this.socket = socket;
			this.sendDataThread = sendDataThread;
		}
		
		public void run(){
			try {
				OutputStream out = socket.getOutputStream();
				long lastTime = System.currentTimeMillis();
				while(running){
					long newTime = System.currentTimeMillis();
					if((newTime-lastTime)>15000){
						out.write(1);
						lastTime=newTime;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sendDataThread.shutdownThread();
				log.error("Client disconnected: "+socket.getInetAddress().toString(), e);
			}
		}
	}

}
