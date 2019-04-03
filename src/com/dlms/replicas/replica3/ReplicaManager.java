package com.dlms.replicas.replica3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.PriorityQueue;

import com.dlms.replicas.replica1.ActionServiceImpl;
import com.dlms.replicas.replica1.Concordia;
import com.dlms.replicas.replica1.McGill;
import com.dlms.replicas.replica1.MessageComparator;
import com.dlms.replicas.replica1.Montreal;

public class ReplicaManager {

	private static String result = "";
	private static PriorityQueue<String> queue = new PriorityQueue<String>(new MessageComparator());
	private static int crashCounter = 0;
	static ActionserviceImpl conStub;
	static ActionserviceImpl mcStub;
	static ActionserviceImpl monStub;

	public static void sendUDPMessage(int serverPort, String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] msg = message.getBytes();
			InetAddress aHost = InetAddress.getByName("132.205.64.201");
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
			ConcordiaLibrary.startConcordiaLibrary();
			MontrealLibrary.startMontrealLibrary();
			McGillLibrary.startMcGillLibrary();

			conStub = ConcordiaLibrary.conStub;
			mcStub = McGillLibrary.mcStub;
			monStub = MontrealLibrary.monStub;

			MulticastSocket aSocket = new MulticastSocket(1314);

			aSocket.joinGroup(InetAddress.getByName("234.1.1.1"));

			System.out.println("Server Started............");

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

					System.out.println("abcd---" + request.getData().toString());
					String data = new String(request.getData());
					System.out.println(data);
//				String dataArray[] = data.split(",");
					// set data in queue

					if (queue.contains(data)) {
						System.out.println("Duplicate message. Message already in queue");
					} else {
						queue.add(data);
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


					if (failureType.equalsIgnoreCase("faultyCrash")) {

						if (crashCounter == 0) {

							new Thread(() -> {
								conStub.crashListItemAvailability("CONM1234");
							});

							new Thread(() -> {
								mcStub.crashListItemAvailability("CONM2345");
							});

							new Thread(() -> {
								monStub.crashListItemAvailability("CONM4567");
							});
							crashCounter++;
						} else {

							ConcordiaLibrary.startConcordiaLibrary();
							MontrealLibrary.startMontrealLibrary();
							McGillLibrary.startMcGillLibrary();

							conStub = ConcordiaLibrary.conStub;
							mcStub = McGillLibrary.mcStub;
							monStub = MontrealLibrary.monStub;
							int size = queue.size();
							String crashedMessage = queue.poll();
							executeQueueMessages(crashedMessage);
							queue.add(crashedMessage);
							size--;
							while (size != 0) {

								String mess = queue.poll();
								executeQueueMessages(mess);
								queue.add(mess);
								size--;

							}
							sendUDPMessage(11111, conStub.listItemAvailability(managerID));
							crashCounter--;

						}

					} else {

						ActionserviceImpl action = new ActionserviceImpl("Montreal");
						System.out.println(userID + "userID");
						System.out.println(managerID + "managerID");
						if (!userID.equalsIgnoreCase("")) { 
							System.out.println("Inside If");
							String idPrefix = userID.substring(0, 3).toUpperCase().trim();
							action = idPrefix.equalsIgnoreCase("CON") ? conStub
									: idPrefix.equalsIgnoreCase("MCG") ? mcStub : monStub;
						} else if (!managerID.equalsIgnoreCase("")) {
							
							String idPrefix = managerID.substring(0, 3);
							action = idPrefix.equalsIgnoreCase("CON") ? conStub
									: idPrefix.equalsIgnoreCase("MCG") ? mcStub : monStub;
							

						} 

						if (operation.equalsIgnoreCase("addItem")) {

							result = action.addItem(managerID, oldItemID, itemName, quantity);
						} else if (operation.equalsIgnoreCase("removeItem")) {
							result = action.removeItem(managerID, oldItemID, quantity);
						} else if (operation.equalsIgnoreCase("listItemAvailability")) {
							result = action.listItemAvailability(managerID);
						} else if (operation.equalsIgnoreCase("borrowItem")) {
							result = action.borrowItem(userID, oldItemID, numberOfDays);
						} else if (operation.equalsIgnoreCase("waitList")) {
							result = action.waitList(userID, oldItemID, numberOfDays);
						} else if (operation.equalsIgnoreCase("findItem")) {
							result = action.findItem(userID, itemName);
						} else if (operation.equalsIgnoreCase("returnItem")) {
							result = action.returnItem(userID, oldItemID);
						} else if (operation.equalsIgnoreCase("exchangeItem")) {
							result = action.exchangeItem(userID, newItemID, oldItemID);

						}
					}
					System.out.println("Result RM3: "+ result);
					sendUDPMessage(11111, "rm3:"+result);

				}
			}).start();

			
		} catch (Exception e) {

		}

	}

	public static void executeQueueMessages(String message) {

		String m[] = message.split(",");
		String operation = m[0];
		String managerID = m[1];
		String userID = m[2];
		String itemID = m[3];
		String newItemID = m[4];
		String oldItemID = m[5];
		String itemName = m[6];
		int quantity = Integer.parseInt(m[7]);
		int numberOfDays = Integer.parseInt(m[8]);

		ActionserviceImpl action;
		if (managerID != null) {
			String idPrefix = managerID.substring(0, 3);
			action = idPrefix.equalsIgnoreCase("CON") ? conStub : idPrefix.equalsIgnoreCase("MCG") ? mcStub : monStub;
		} else {
			String idPrefix = userID.substring(0, 3);
			action = idPrefix.equalsIgnoreCase("CON") ? conStub : idPrefix.equalsIgnoreCase("MCG") ? mcStub : monStub;

		}

		if (operation.equalsIgnoreCase("addItem")) {

			result = action.addItem(managerID, oldItemID, itemName, quantity);
		} else if (operation.equalsIgnoreCase("removeItem")) {
			result = action.removeItem(managerID, oldItemID, quantity);
		} else if (operation.equalsIgnoreCase("listItemAvailability") || operation.equalsIgnoreCase("faultyCrash")) {
			result = action.listItemAvailability(managerID);
		} else if (operation.equalsIgnoreCase("borrowItem")) {
			result = action.borrowItem(userID, oldItemID, numberOfDays);
		} else if (operation.equalsIgnoreCase("waitList")) {
			result = action.waitList(userID, oldItemID, numberOfDays);
		} else if (operation.equalsIgnoreCase("findItem")) {
			result = action.findItem(userID, itemName);
		} else if (operation.equalsIgnoreCase("returnItem")) {
			result = action.returnItem(userID, oldItemID);
		} else if (operation.equalsIgnoreCase("exchangeItem")) {
			result = action.exchangeItem(userID, newItemID, oldItemID);

		}
		System.out.println("Operation: " + m[0] + "    result: " + result);
	}

}
