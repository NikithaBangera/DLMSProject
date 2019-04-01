package com.dlms.frontend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.omg.CORBA.ORB;

import ActionServiceApp.ActionServicePOA;

public class FrontEndImplementation extends ActionServicePOA {
	private String replicaName;

	private ORB orb;
	private String timetaken;
	private long duration = 1000;
	String majorityElement;
	private int invalidElement;
	private HashMap<Integer, Integer> badReplicaMap = new HashMap<Integer, Integer>();
	private Set<String> successSet = new HashSet<String>();

	public FrontEndImplementation(String replicaName) {

		this.replicaName = replicaName;
		badReplicaMap.put(1, 0);
		badReplicaMap.put(2, 0);
		badReplicaMap.put(3, 0);

	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	// params:
	// operation,managerID,userID,exchangeItemID,itemID,itemName,quantity,numberOfDays,failureType

	public String addItem(String managerID, String itemID, String itemName, int quantity) {
		
		String result = sendToSequencer("addItem", managerID, null, null, itemID, itemName, quantity, 0, null);

		return result;
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		String result = sendToSequencer("removeItem", managerID, null, null, itemID, null, quantity, 0, null);
		System.out.println(this);
		return result;
	}

	public String listItemAvailability(String managerID) {
		String result = sendToSequencer("listItemAvailability", managerID, null, null, null, null, 0, 0, null);
		return result;
	}

	public String borrowItem(String userID, String itemID, int numberOfDays) {
		String result = sendToSequencer("borrowItem", null, userID, null, itemID, null, 0, numberOfDays, null);
		return result;
	}

	public String findItem(String userID, String itemName) {
		String result = sendToSequencer("findItem", null, userID, null, null, itemName, 0, 0, null);
		return result;
	}

	public String returnItem(String userID, String itemID) {
		String result = sendToSequencer("returnItem", null, userID, null, itemID, null, 0, 0, null);
		return result;
	}

	public String waitList(String userID, String itemID, int numberOfDays) {
		String result = sendToSequencer("waitList", null, userID, null, itemID, null, 0, numberOfDays, null);
		return result;
	}

	public String exchangeItem(String userID, String newItemID, String oldItemID) {
		String result = sendToSequencer("exchangeItem", null, userID, newItemID, oldItemID, null, 0, 0, null);
		return result;
	}

//	@Override
//	public boolean validateUser(String userID) {
//		String result = sendToSequencer("validateUser", null, userID, null, null, null, 0, 0, null);
//		if (result.contains("success:")) {
//			return true;
//		} else {
//			return false;
//		}
//
//	}

//	public synchronized String sendToSequencerUser(String operation, String userID, String exchangeItemID,
//			String itemID, String itemName, String newItemID, String oldItemID, int noOfDays, String failureType) {
//
//		return null;
//
//	}

	public synchronized String sendToSequencer(String operation, String managerID, String userID, String exchangeItemID,
			String itemID, String itemName, int quantity, int numberOfDays, String failureType) {

		DatagramSocket aSocket = null;
		String messageReceived = null;
		String key = null;
		String message1, message2, message3;

		long startTime = 0;
		long endTime = 0;
		List<Long> waitTimeList = new ArrayList<Long>();

		try {

			key = operation + "," + managerID + "," + userID + "," + exchangeItemID + "," + itemID + "," + itemName
					+ "," + quantity + "," + numberOfDays + "," + failureType;
			aSocket = new DatagramSocket(11111);
			aSocket.setSoTimeout((int) duration * 2);

			byte[] mess = key.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost"); // address of Sequencer
			DatagramPacket request = new DatagramPacket(mess, key.length(), aHost, 22222); // creating a packet to send

			// to receiver at port 22222
			aSocket.send(request);
			aSocket.send(request);
			byte[] buffer1 = new byte[1000];
			byte[] buffer2 = new byte[1000];
			byte[] buffer3 = new byte[1000];

			DatagramPacket reply[] = new DatagramPacket[3];
			reply[0] = new DatagramPacket(buffer1, buffer1.length);
			reply[1] = new DatagramPacket(buffer2, buffer2.length);
			reply[2] = new DatagramPacket(buffer3, buffer3.length);

			for (int i = 0; i < 3; i++) {

				try {
					startTime = System.currentTimeMillis();
					aSocket.receive(reply[i]);
					endTime = System.currentTimeMillis();
				} catch (SocketTimeoutException t) {
					/*
					 * Sending a message to RM of crashed replica
					 */
					aSocket.send(new DatagramPacket("replicaCrashed".getBytes(), "replicaCrashed".length(),
							InetAddress.getByName("crashed replica address"), 55555));

				}
				/*
				 * Calculating the time taken by a request and adding the maximum time request
				 * to duration
				 */
				if (reply[i] != null) {

					waitTimeList.add(endTime - startTime);
				}
			}
			duration = Collections.max(waitTimeList);

			message1 = new String(reply[0].getAddress().equals("replica1") ? reply[0].getData()
					: reply[1].getAddress().equals("replica1") ? reply[1].getData() : reply[2].getData());
			message1 = message1.trim();
			message2 = new String(reply[0].getAddress().equals("replica2") ? reply[0].getData()
					: reply[1].getAddress().equals("replica2") ? reply[1].getData() : reply[2].getData());
			message2 = message2.trim();
			message3 = new String(reply[0].getAddress().equals("replica3") ? reply[0].getData()
					: reply[1].getAddress().equals("replica3") ? reply[1].getData() : reply[2].getData());
			message3 = message3.trim();

			majorityOfResult(message1, message2, message3);

			if (invalidElement == 1) {
				badReplicaMap.put(1, badReplicaMap.get(1) + 1);
				badReplicaMap.put(2, 0);
				badReplicaMap.put(3, 0);
				validateReplicaAndSend(1, aSocket, aHost);
			} else if (invalidElement == 2) {
				badReplicaMap.put(2, badReplicaMap.get(2) + 1);
				badReplicaMap.put(1, 0);
				badReplicaMap.put(3, 0);
				validateReplicaAndSend(2, aSocket, aHost);

			} else if (invalidElement == 3) {
				badReplicaMap.put(3, badReplicaMap.get(3) + 1);
				badReplicaMap.put(2, 0);
				badReplicaMap.put(1, 0);
				validateReplicaAndSend(3, aSocket, aHost);

			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		}

		finally {
			if (aSocket != null)
				aSocket.close();
		}

		return majorityElement;

	}

	public void validateReplicaAndSend(int i, DatagramSocket aSocket, InetAddress aHost) throws IOException {

		String key = "replace," + i;
		byte mess[] = key.getBytes();
		DatagramPacket request = new DatagramPacket(mess, key.length(), aHost, 22222);

		if (badReplicaMap.get(i) == 3) {

			aSocket.send(request);
		}

	}

	public String majorityOfResult(String message1, String message2, String message3) {

		majorityElement = null;
		invalidElement = 0;
		int i;
		successSet.clear();
		if(message1.contains("success") || message1.contains("Success")) {
			successSet.add(message1);
			i=1;
			
		}
		if(message2.contains("success") || message1.contains("Success")) {
			successSet.add(message2);
			i=2;
		}
		if(message3.contains("success") || message1.contains("Success")) {
			successSet.add(message3);
			i=3;
		}
		
		
		
		if (message1.equalsIgnoreCase(message2) && message2.equalsIgnoreCase(message3)) {

			majorityElement = message1;

		} else if (message1.equalsIgnoreCase(message2) && !message2.equalsIgnoreCase(message3)) {

			majorityElement = message1;
			invalidElement = 3;

		} else if (!message1.equalsIgnoreCase(message2) && message2.equalsIgnoreCase(message3)) {

			majorityElement = message2;
			invalidElement = 1;
		} else if (message1.equalsIgnoreCase(message3) && !message2.equalsIgnoreCase(message3)) {

			majorityElement = message1;
			invalidElement = 2;
		} else {
			invalidElement = 0;
		}

		return majorityElement + "," + invalidElement;

	}

}
