package com.clearwire.tools.mobile.aiat.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TCPDownlinkTrafficServer extends Thread {
	
	private static Log log = LogFactory.getLog(TCPDownlinkTrafficServer.class);
	
	private int BUFFER_SIZE = 16 * 1024;
		
	private ServerSocket tcpDownlinkSocket;
	
	private boolean running=true;
		
	public TCPDownlinkTrafficServer(){
		super();
		try {
			tcpDownlinkSocket = new ServerSocket(6789);
		} catch (IOException e) {
			log.error("Failed to start traffic server.", e);
		}
	}
	
	public void run() {
		
		while(running){
			try {
				Socket newSocket = tcpDownlinkSocket.accept();
				log.info("Received TCP downlink traffic client connection.");
				(new SendDataThread(newSocket)).start();
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
		
		private byte[] data = new byte[BUFFER_SIZE];

		public SendDataThread(Socket socket) {
			super();
			this.socket = socket;
		}
		
		public void run(){
			
			try {
				OutputStream out = socket.getOutputStream();
				while(running)
					out.write(data);
			} catch (IOException e) {
				log.error("Client disconnected: "+socket.getInetAddress().toString(), e);
			}
		}
	}

}
