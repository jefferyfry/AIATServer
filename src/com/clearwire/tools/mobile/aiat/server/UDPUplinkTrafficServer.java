package com.clearwire.tools.mobile.aiat.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UDPUplinkTrafficServer extends Thread {
	
	private static Log log = LogFactory.getLog(UDPUplinkTrafficServer.class);
	
	private int BUFFER_SIZE = 16 * 1024;
		
	private ServerSocket udpControlSocket;
	
	private DatagramSocket udpUplinkSocket;
	
	private boolean running=true;
	
	public UDPUplinkTrafficServer(){
		super();
		try {
			udpControlSocket = new ServerSocket(6794);
			udpUplinkSocket = new DatagramSocket(6795);
		} catch (IOException e) {
			log.error("Failed to start traffic server.", e);
		}
	}
	
	public void run() {
		
		while(running){
			try {
				Socket newSocket = udpControlSocket.accept();
				log.info("Received UDP uplink traffic client connection.");
				ReceiveDataThread receiveDataThread = new ReceiveDataThread(newSocket);
				(new SendReportThread(newSocket,receiveDataThread)).start();
				receiveDataThread.start();
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
		private int packets;
		private byte[] data = new byte[BUFFER_SIZE];
		private boolean threadRunning=true;
		public ReceiveDataThread(Socket socket) {
			super();
			this.socket = socket;
		}
		
		/**
		 * @return the packets
		 */
		public int getPackets() {
			return packets;
		}
		
		public void run(){
			
			try {
				while(running&&threadRunning){
					udpUplinkSocket.receive(new DatagramPacket(data, BUFFER_SIZE));
					packets++;
				}
				log.info("Client disconnected: "+socket.getInetAddress().toString());
			} catch (IOException e) {
				log.error("Client disconnected: "+socket.getInetAddress().toString(), e);
			}
		}
		
		public void shutdownThread(){
			threadRunning=false;
		}
	}
	
	class SendReportThread extends Thread {
		
		private Socket socket;
		private ReceiveDataThread receiveDataThread;
		
		public SendReportThread(Socket socket,ReceiveDataThread receiveDataThread) {
			super();
			this.socket = socket;
			this.receiveDataThread = receiveDataThread;
		}
		
		public void run(){
			int packetCount=receiveDataThread.getPackets();
			long lastTime = System.currentTimeMillis();
			try {
				OutputStream out = socket.getOutputStream();
				while(running&&!socket.isClosed()){
					Thread.sleep(1000);
					long newTime = System.currentTimeMillis();
					int newCount = receiveDataThread.getPackets();
					double bits = (newCount-packetCount) * BUFFER_SIZE * 8;
					double time = (newTime-lastTime)/1000d;
					double rate = bits/time;
					out.write(longToByteArray(Double.doubleToRawLongBits(rate)));
					packetCount=newCount;
					lastTime=newTime;
				}
			} catch (IOException e) {
				log.error("Client disconnected: "+socket.getInetAddress().toString(), e);
				receiveDataThread.shutdownThread();
			} catch (InterruptedException e) {
				log.error("Report interval failed: "+socket.getInetAddress().toString(), e);
			}
		}
	}
	
	public static byte[] longToByteArray(long data) {
	    return new byte[] {
	        (byte)((data >> 56) & 0xff),
	        (byte)((data >> 48) & 0xff),
	        (byte)((data >> 40) & 0xff),
	        (byte)((data >> 32) & 0xff),
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8 ) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}

}
