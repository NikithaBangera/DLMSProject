package com.dlms.replicas.replica2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.PriorityQueue;

import com.dlms.replicas.replica2.MessageComparator;

public class ReplicaManager {

	private static String result = "";
	private static int Bugcount = 0;
	private static PriorityQueue<String> queue = new PriorityQueue<String>(new MessageComparator());
	private static PriorityQueue<String> messageBuffer = new PriorityQueue<String>(new MessageComparator());

	public static void main(String[] args) {
		try {
			ConcordiaServer concordiaServer = new ConcordiaServer();
			McgillServer mcgillServer = new McgillServer();
			MontrealServer montrealServer = new MontrealServer();
			concordiaServer.startUDP();
			mcgillServer.startUDP();
			montrealServer.startUDP();

			ActionServiceImpl actionServiceImpl = new ActionServiceImpl();

			MulticastSocket aSocket = new MulticastSocket(1314);

			aSocket.joinGroup(InetAddress.getByName("234.1.1.1"));

			System.out.println("\nReplica manager 2 Started............");
			new Thread(() -> {
				while (true) {
					byte[] buffer = new byte[1000];
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					try {
						aSocket.receive(request);
					} catch (IOException e) {
						e.printStackTrace();
					}

					String data = new String(request.getData()).trim();
					System.out.println("\n------------Request received at Replica 2 " + data + "---------");

					// set data in queue

					if (messageBuffer.contains(data)) {
						System.out.println("\n Duplicate message. Message already in queue");
						continue;
					} else {
						queue.add(data);
						messageBuffer.add(data);
					}

					String message[] = queue.poll().split(",");
					String sequenceNumber = message[0];
					String operation = message[1];
					String managerID = message[2];
					String userID = message[3];
					String newItemID = message[4];
					String oldItemID = message[5];
					String itemName = message[6];
					int quantity = Integer.parseInt(message[7]);
					int numberOfDays = Integer.parseInt(message[8]);
					String failureType = message[9];

					if (failureType.equalsIgnoreCase("faultyBug")) {
						Bugcount += 1;
						System.out.println(
								"\nNumber of fault intimation received by FE to Replica Manager 2: " + Bugcount);
					}
					if (failureType.equalsIgnoreCase("faultyCrash")) {

						result = actionServiceImpl.listItemAvailability(managerID);

					} else {
						if (operation.equalsIgnoreCase("addItem")) {
							result = actionServiceImpl.addItem(managerID, oldItemID, itemName, quantity);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("removeItem")) {
							result = actionServiceImpl.removeItem(managerID, oldItemID, quantity);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("listItemAvailability")) {
							result = actionServiceImpl.listItemAvailability(managerID);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("borrowItem")) {
							result = actionServiceImpl.borrowItem(userID, oldItemID, numberOfDays);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("waitList")) {
							result = actionServiceImpl.waitList(userID, oldItemID, numberOfDays);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("findItem")) {
							result = actionServiceImpl.findItem(userID, itemName);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("returnItem")) {
							result = actionServiceImpl.returnItem(userID, oldItemID);
							Bugcount = 0;
						} else if (operation.equalsIgnoreCase("exchangeItem")) {
							result = actionServiceImpl.exchangeItem(userID, newItemID, oldItemID);
							Bugcount = 0;
						}
					}
					System.out.println("\n---------RESULT in RM2 :" + result + "---------");
					sendUDPMessage(11111, "rm2:" + result);
				}

			}).start();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void sendUDPMessage(int serverPort, String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] msg = message.getBytes();
			InetAddress aHost = InetAddress.getByName("132.205.64.197"); // add Front End Address
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
