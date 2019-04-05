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

import javax.xml.ws.FaultAction;

import org.omg.CORBA.ORB;

import ActionServiceApp.ActionServicePOA;

public class FrontEndImplementation extends ActionServicePOA {
	private String replicaName;

	private ORB orb;
	private String timetaken;
	private long duration = 100000;
	private static String majorityElement = null;
	private static String majorityMessage = "";
	private int invalidElement;
	private int crashElement;
	private HashMap<Integer, Integer> badReplicaMap = new HashMap<Integer, Integer>();
	private Set<String> successSet = new HashSet<String>();
	private List<Long> waitTimeList = new ArrayList<Long>();
	private static int replicaNumber = 0;
	int bugC = 1000;

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

	public synchronized String addItem(String managerID, String itemID, String itemName, int quantity) {

		String result = sendToSequencer("addItem", managerID, "", null, itemID, itemName, quantity, 0, null);

		System.out.println("FE result " + result + ":");

		return result;

	}

	public synchronized String removeItem(String managerID, String itemID, int quantity) {
		String result = sendToSequencer("removeItem", managerID, "", null, itemID, null, quantity, 0, null);
		System.out.println(this);
		return result;
	}

	public synchronized String listItemAvailability(String managerID) {
		String result;
		if (managerID.contains("faultyCrash")) {
			managerID = managerID.split(":")[0];
			result = sendToSequencer("listItemAvailability", managerID, "", null, null, null, 0, 0, "faultyCrash");
		} else {
			result = sendToSequencer("listItemAvailability", managerID, "", null, null, null, 0, 0, null);
		}
		return result;
	}

	public synchronized String borrowItem(String userID, String itemID, int numberOfDays) {
		String result = sendToSequencer("borrowItem", "", userID, null, itemID, null, 0, numberOfDays, null);
		return result;
	}

	public synchronized String findItem(String userID, String itemName) {
		String result = sendToSequencer("findItem", "", userID, null, null, itemName, 0, 0, null);
		return result;
	}

	public synchronized String returnItem(String userID, String itemID) {
		String result = sendToSequencer("returnItem", "", userID, null, itemID, null, 0, 0, null);
		return result;
	}

	public synchronized String waitList(String userID, String itemID, int numberOfDays) {
		String result = sendToSequencer("waitList", "", userID, null, itemID, null, 0, numberOfDays, null);
		return result;
	}

	public synchronized String exchangeItem(String userID, String newItemID, String oldItemID) {
		String result = sendToSequencer("exchangeItem", "", userID, newItemID, oldItemID, null, 0, 0, null);
		return result;
	}

	// @Override
	// public boolean validateUser(String userID) {
	// String result = sendToSequencer("validateUser", null, userID, null, null,
	// null, 0, 0, null);
	// if (result.contains("success:")) {
	// return true;
	// } else {
	// return false;
	// }
	//
	// }

	// public synchronized String sendToSequencerUser(String operation, String
	// userID, String exchangeItemID,
	// String itemID, String itemName, String newItemID, String oldItemID, int
	// noOfDays, String failureType) {
	//
	// return null;
	//
	// }

	public synchronized String sendToSequencer(String operation, String managerID, String userID, String exchangeItemID,
			String itemID, String itemName, int quantity, int numberOfDays, String failureType) {
		DatagramSocket aSocket = null;
		String messageReceived = null;
		String key = null;
		String message1 = null, message2 = null, message3 = null;

		long startTime = 0;
		long endTime = 0;

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
			// aSocket.send(request);
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
					System.out.println("Replica 3 crashed");

					String crashIntimation = 10000000 + "," + "listItemAvailability" + "," + managerID + "," + userID
							+ "," + exchangeItemID + "," + itemID + "," + itemName + "," + quantity + "," + numberOfDays
							+ "," + "faultyCrash";

					System.out.println("Sending message to replica manager at replica 3 to recover from crash");
					aSocket.send(new DatagramPacket(crashIntimation.getBytes(), crashIntimation.length(),
							InetAddress.getByName("132.205.64.22"), 1314)); // For Crash failure

					message1 = new String(reply[0].getData()).trim();
					System.out.println("message1: " + message1);
					message2 = new String(reply[1].getData()).trim();
					System.out.println("message2: " + message2);
					message3 = null;

					return majorityOfResult(message1, message2, message3);
				}
				/*
				 * Calculating the time taken by a request and adding the maximum time request
				 * to duration
				 */
				if (reply[i] != null) {

					waitTimeList.add(endTime - startTime);
				}
			}
			// duration = Collections.max(waitTimeList);
			new String(request.getData());
			message1 = new String(reply[0].getData()).trim();
			message2 = new String(reply[1].getData()).trim();
			message3 = new String(reply[2].getData()).trim();

			System.out.println(message1.trim() + " Before Majority message1");
			System.out.println(message2.trim() + " Before Majority message2");
			System.out.println(message3.trim() + " Before Majority message3");

			// message1 = new String(reply[0].getAddress().equals("replica1") ?
			// reply[0].getData()
			// : reply[1].getAddress().equals("replica1") ? reply[1].getData() :
			// reply[2].getData());
			// message1 = message1.trim();
			// message2 = new String(reply[0].getAddress().equals("replica2") ?
			// reply[0].getData()
			// : reply[1].getAddress().equals("replica2") ? reply[1].getData() :
			// reply[2].getData());
			// message2 = message2.trim();
			// message3 = new String(reply[0].getAddress().equals("replica3") ?
			// reply[0].getData()
			// : reply[1].getAddress().equals("replica3") ? reply[1].getData() :
			// reply[2].getData());
			// message3 = message3.trim();

			majorityElement = majorityOfResult(message1, message2, message3);

		
			if (replicaNumber > 0) {
				String faultIntimation = bugC++ + "," + "" + "," + "CONM1013" + "," + "" + "," + "" + "," + "" + "," + ""
						+ "," + 0 + "," + 0 + "," + "faultyBug";

				System.out.println("Sending intimation to faulty replica: " + faultIntimation);

				aSocket.send(new DatagramPacket(faultIntimation.getBytes(), faultIntimation.length(),
						InetAddress.getByName(replicaNumber == 1 ? "132.205.64.197"
								: replicaNumber == 2 ? "132.205.64.21" : "132.205.64.22"),
						1314));
			}

			System.out.println("Majority response sent to client : " + majorityElement);
//			if (invalidElement == 1) {
//				badReplicaMap.put(1, badReplicaMap.get(1) + 1);
//				badReplicaMap.put(2, 0);
//				badReplicaMap.put(3, 0);
//				validateReplicaAndSend(1, aSocket, aHost);
//			} else if (invalidElement == 2) {
//				badReplicaMap.put(2, badReplicaMap.get(2) + 1);
//				badReplicaMap.put(1, 0);
//				badReplicaMap.put(3, 0);
//				validateReplicaAndSend(2, aSocket, aHost);
//
//			} else if (invalidElement == 3) {
//				badReplicaMap.put(3, badReplicaMap.get(3) + 1);
//				badReplicaMap.put(2, 0);
//				badReplicaMap.put(1, 0);
//				validateReplicaAndSend(3, aSocket, aHost);
//
//			}

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

		return majorityElement.trim();

	}

//	public synchronized void validateReplicaAndSend(int i, DatagramSocket aSocket, InetAddress aHost)
//			throws IOException {
//
//		String key = "replace," + i;
//		byte mess[] = key.getBytes();
//		DatagramPacket request = new DatagramPacket(mess, key.length(), aHost, 22222);
//
//		if (badReplicaMap.get(i) == 3) {
//
//			aSocket.send(request);
//		}
//
//	}

	public synchronized String majorityOfResult(String message1, String message2, String message3) {

		invalidElement = 0;
		majorityMessage = "";
		boolean isJunk = false;
		replicaNumber = 0;

		// Crash
		if (message1 == null) {
			majorityMessage = message2.split(":", 3)[2];
		} else if (message2 == null) {
			majorityMessage = message1.split(":", 3)[2];
		} else if (message3 == null) {
			majorityMessage = message1.split(":", 3)[2];
		}
		// Faulty Bug
		else if ((message1.split(":")[1].equalsIgnoreCase("success"))
				&& (message1.split(":")[2].equalsIgnoreCase("somejunkvalue"))) {
			isJunk = true;
			replicaNumber = Integer.parseInt(message1.split(":")[0].substring(2, 3));
			majorityMessage = message2.split(":", 3)[2];

		} else if ((message2.split(":")[1].equalsIgnoreCase("success"))
				&& (message2.split(":")[2].equalsIgnoreCase("somejunkvalue"))) {
			replicaNumber = Integer.parseInt(message2.split(":")[0].substring(2, 3));
			isJunk = true;
			majorityMessage = message1.split(":", 3)[2];
		} else if ((message3.split(":")[1].equalsIgnoreCase("success"))
				&& (message3.split(":")[2].equalsIgnoreCase("somejunkvalue"))) {
			replicaNumber = Integer.parseInt(message3.split(":")[0].substring(2, 3));
			isJunk = true;
			majorityMessage = message2.split(":", 3)[2];
		}

		// } else if (message1.equalsIgnoreCase("someJunkValue")) {
		// int replica = Integer.parseInt(message1.split(":")[0].substring(2, 3));
		// invalidElement = replica;
		//
		// } else if (message2.equalsIgnoreCase("someJunkValue")) {
		// int replica = Integer.parseInt(message2.split(":")[0].substring(2, 3));
		// invalidElement = replica;
		//
		// } else if (message3.equalsIgnoreCase("someJunkValue")) {
		// int replica = Integer.parseInt(message3.split(":")[0].substring(2, 3));
		// invalidElement = replica;

		else if (message1.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Success")) {
			majorityMessage = message1.split(":", 3)[2];
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message3.split(":")[1].equalsIgnoreCase("Fail")) {
			majorityMessage = message1.split(":", 3)[2];
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Fail")) {
			majorityMessage = message1.split(":", 3)[2];
			invalidElement = 3;
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Success")) {
			majorityMessage = message1.split(":", 3)[2];
			invalidElement = 2;
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Success")) {
			majorityMessage = message2.split(":", 3)[2];
			invalidElement = 1;
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Success")) {
			majorityMessage = message1.split(":", 3)[2];
			invalidElement = 3;
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Fail")) {
			majorityMessage = message1.split(":", 3)[2];
			invalidElement = 2;
		} else if (message1.split(":", 3)[1].equalsIgnoreCase("Success")
				&& message2.split(":", 3)[1].equalsIgnoreCase("Fail")
				&& message3.split(":", 3)[1].equalsIgnoreCase("Fail")) {
			majorityMessage = message2.split(":", 3)[2];
			invalidElement = 1;
		}
		return majorityMessage;

	}

	// public String majorityOfResult(String message1, String message2, String
	// message3) {
	//
	// majorityElement = null;
	// invalidElement = 0;
	// int i;
	// successSet.clear();
	//
	// if(message1.contains("success") || message1.contains("Success")) {
	// successSet.add(message1);
	// i=1;
	//
	// }
	// if(message2.contains("success") || message1.contains("Success")) {
	// successSet.add(message2);
	// i=2;
	// }
	// if(message3.contains("success") || message1.contains("Success")) {
	// successSet.add(message3);
	// i=3;
	// }
	//
	//
	//
	// if (message1.equalsIgnoreCase(message2) &&
	// message2.equalsIgnoreCase(message3)) {
	//
	// majorityElement = message1;
	//
	// } else if (message1.equalsIgnoreCase(message2) &&
	// !message2.equalsIgnoreCase(message3)) {
	//
	// majorityElement = message1;
	// invalidElement = 3;
	//
	// } else if (!message1.equalsIgnoreCase(message2) &&
	// message2.equalsIgnoreCase(message3)) {
	//
	// majorityElement = message2;
	// invalidElement = 1;
	// } else if (message1.equalsIgnoreCase(message3) &&
	// !message2.equalsIgnoreCase(message3)) {
	//
	// majorityElement = message1;
	// invalidElement = 2;
	// } else {
	// invalidElement = 0;
	// }
	//
	// return majorityElement + "," + invalidElement;
	//
	// }

}
