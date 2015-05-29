package com.clearwire.tools.mobile.aiat.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TCPUplinkTrafficServer extends Thread {
	
	private static Log log = LogFactory.getLog(TCPDownlinkTrafficServer.class);
	
	private int BUFFER_SIZE = 16 * 1024;
		
	private ServerSocket tcpUplinkSocket;
	
	private boolean running=true;
	
	public TCPUplinkTrafficServer(){
		super();
		try {
			tcpUplinkSocket = new ServerSocket(6790);
		} catch (IOException e) {
			log.error("Failed to start traffic server.", e);
		}
	}
	
	public void run() {
		
		while(running){
			try {
				Socket newSocket = tcpUplinkSocket.accept();
				log.info("Received TCP uplink traffic client connection.");
				(new ReceiveDataThread(newSocket)).start();
			}catch (IOException e) {
				log.error("Error accepting connection.", e);
			}
		}
		
	}
	
	public void shutdown(){
		running=false;
	}
	
	class ReceiveDataThread extends Thread {
		
		private Socket socket;
		
		private byte[] data = new byte[BUFFER_SIZE];

		public ReceiveDataThread(Socket socket) {
			super();
			this.socket = socket;
		}
		
		public void run(){
			
			try {
				InputStream in = socket.getInputStream();
				while(running){
					in.read(data);
				}
			} catch (IOException e) {
				log.error("Client disconnected: "+socket.getInetAddress().toString(), e);
			}
		}
	}

}
