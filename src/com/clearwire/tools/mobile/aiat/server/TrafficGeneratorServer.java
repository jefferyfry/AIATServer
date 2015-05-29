package com.clearwire.tools.mobile.aiat.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TrafficGeneratorServer {
	
	private static Log log = LogFactory.getLog(TrafficGeneratorServer.class);
	
	private static TCPDownlinkTrafficServer tcpDownlinkTrafficServer = new TCPDownlinkTrafficServer();
	private static TCPUplinkTrafficServer tcpUplinkTrafficServer = new TCPUplinkTrafficServer();
	private static UDPDownlinkTrafficServer udpDownlinkTrafficServer = new UDPDownlinkTrafficServer();
	private static UDPUplinkTrafficServer udpUplinkTrafficServer = new UDPUplinkTrafficServer();
	
	public static void main(String[] args){
		log.info("Starting servers...");
		tcpDownlinkTrafficServer.start();
		
		tcpUplinkTrafficServer.start();
		
		udpDownlinkTrafficServer.start();
		
		udpUplinkTrafficServer.start();
		
		log.info("Servers started!");
	}
	
	

}
