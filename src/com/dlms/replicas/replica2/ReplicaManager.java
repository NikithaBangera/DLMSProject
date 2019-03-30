package com.dlms.replicas.replica2;

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
}
