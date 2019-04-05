package com.dlms.replicas.replica1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.PriorityQueue;

public class ReplicaManager {

	private static String result = "";
	private static String replicaId = "rm1:";
	private static int Bugcount = 0;
	private static PriorityQueue<String> queue = new PriorityQueue<String>(new MessageComparator());
	private static PriorityQueue<String> messageBuffer = new PriorityQueue<String>(new MessageComparator());

	static public void sendUDPMessage(int serverPort, String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] msg = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost"); // Address of Sequencer
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

	public static void main(String[] args) {
		try {
			Concordia.startConcordiaServer();
			Montreal.startMontrealServer();
			McGill.startMcGillServer();

			ActionServiceImpl action = new ActionServiceImpl();

			MulticastSocket aSocket = new MulticastSocket(1314);

			aSocket.joinGroup(InetAddress.getByName("234.1.1.1"));

			System.out.println("\nReplica manager 1 Started.............");
			new Thread(() -> {
				while (true) {
					byte[] buffer = new byte[1000];
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					try {
						aSocket.receive(request);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String data = new String(request.getData()).trim();
					System.out.println("\n------------Request received at Replica 1 " + data + "---------");
					// set data in queue
					if (messageBuffer.contains(data)) {
						System.out.println("\n Duplicate message. Message already in queue");
						continue;
					} else {
						queue.add(data);
						messageBuffer.add(data);
					}

					String message[] = queue.poll().split(",");
					String seqNum = message[0];
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
								"\nNumber of fault intimation received by FE to Replica Manager 1: " + Bugcount);
					}
					if (failureType.equalsIgnoreCase("faultyCrash")) {
						result = action.listItemAvailability(managerID);

					} else {
						if (operation.equalsIgnoreCase("addItem")) {
							result = action.addItem(managerID, oldItemID, itemName, quantity);
						} else if (operation.equalsIgnoreCase("removeItem")) {
							result = action.removeItem(managerID, oldItemID, quantity);
						} else if (operation.equalsIgnoreCase("listItemAvailability")) {

							if (managerID.equalsIgnoreCase("CONM1013")) {
								if (Bugcount < 3) {
									result = "success:someJunkValue";
								} else {
									Bugcount = 0;
									result = action.listItemAvailability(managerID);

								}
							} else {
								Bugcount = 0;
								result = action.listItemAvailability(managerID);
							}

						} else if (operation.equalsIgnoreCase("borrowItem")) {
							Bugcount = 0;
							result = action.borrowItem(userID, oldItemID, numberOfDays);
						} else if (operation.equalsIgnoreCase("waitList")) {
							Bugcount = 0;
							result = action.waitList(userID, oldItemID, numberOfDays);
						} else if (operation.equalsIgnoreCase("findItem")) {
							Bugcount = 0;
							result = action.findItem(userID, itemName);
						} else if (operation.equalsIgnoreCase("returnItem")) {
							Bugcount = 0;
							result = action.returnItem(userID, oldItemID);
						} else if (operation.equalsIgnoreCase("exchangeItem")) {
							Bugcount = 0;
							result = action.exchangeItem(userID, newItemID, oldItemID);
						}

					}
					System.out.println("\n---------RESULT in RM1 :" + result + "---------");
					sendUDPMessage(11111, "rm1:" + result);

				}
			}).start();

		} catch (Exception e) {

		}

	}
}
