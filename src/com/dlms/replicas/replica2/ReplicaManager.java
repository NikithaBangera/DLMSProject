package com.dlms.replicas.replica2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.PriorityQueue;

public class ReplicaManager {
	
	public static void main(String[] args) {
		try {
			ConcordiaServer.startUDP();
			McgillServer.startUDP();
			MontrealServer.startUDP();
			
			PriorityQueue<String> queue = new PriorityQueue<String>(new MessageComparator());

			String message[] = queue.poll().split(",");
			String operation = message[0];
			String managerID = message[1];
			String userID = message[2];
			String itemID = message[3];
			String newItemID = message[4];
			String oldItemID = message[5];
			String itemName = message[6];
			int quantity = Integer.parseInt(message[7]);
			String failureType = message[8];
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendUDPMessage(int serverPort, String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] msg = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(msg, msg.length, aHost, serverPort);
			aSocket.send(request);
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
}
