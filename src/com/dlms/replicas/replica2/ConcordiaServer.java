package com.dlms.replicas.replica2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ConcordiaServer {

	public static Map<String, String> conLibrary = new TreeMap<String, String>();
	public static Map<String, String> conWaitlist = new TreeMap<String, String>();
	public static Map<String, List<String>> userHistory = new TreeMap<String, List<String>>();
	public static HashMap<String, HashMap<String, Integer>> userlist = new HashMap<String, HashMap<String, Integer>>();
	public static ArrayList<String> managerUserList = new ArrayList<String>();
	List<String> itemHistory = new ArrayList<String>();
	public static String convalues = null;
	private static boolean running;
	String nextItem = "";

	/**
	 * Concordia server constructor which initializes the required TreeMaps.
	 */
	public ConcordiaServer() {

		conLibrary.put("CON2011", "Distributed Systems,5");
		conLibrary.put("CON2012", "Advanced Programming,6");
		conLibrary.put("CON2013", "Pattern Recognition,0");
		conLibrary.put("CON2014", "Machine learning,0");
		conLibrary.put("CON2015", "Advanced Java,0");

		conWaitlist.put("CON2013", "CONU1015,MONU1016");
		conWaitlist.put("CON2014", "MCGU1017,MONU1018");
		conWaitlist.put("CON2015", "MONU1019,MCGU1020");

		itemHistory.add("CON2012,Borrowed");
		itemHistory.add("CON2013,Borrowed");
		itemHistory.add("MON2014,Borrowed");
		itemHistory.add("MON2015,Returned");
		itemHistory.add("MCG2014,Borrowed");
		itemHistory.add("MCG2015,Returned");
		itemHistory.add("CON2015,Returned");

		userHistory.put("CONU1011", itemHistory);
		userHistory.put("CONU1012", itemHistory);
		userHistory.put("CONU1013", itemHistory);
		userHistory.put("CONU1014", itemHistory);
		userHistory.put("CONU1015", itemHistory);
		userHistory.put("CONU1016", itemHistory);
		userHistory.put("CONU1017", itemHistory);
		userHistory.put("CONU1018", itemHistory);
		userHistory.put("CONU1019", itemHistory);
		userHistory.put("CONU1020", itemHistory);

	}

	/**
	 * Main method of Concordia Library Server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

	//	try {

//			ConcordiaServer conServer = new ConcordiaServer();
//
//			ORB orb = ORB.init(args, null);
//
//			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
//			rootPOA.the_POAManager().activate();
//
//			ActionServiceImpl serverImpl = new ActionServiceImpl();
//			serverImpl.setOrb(orb);
//
//			Object serverInterfaceRef = rootPOA.servant_to_reference(serverImpl);
//			LibraryIDLInterface serverInterface = LibraryIDLInterfaceHelper.narrow(serverInterfaceRef);
//
//			Object nameServerRef = orb.resolve_initial_references("NameService");
//			NamingContextExt nameServer = NamingContextExtHelper.narrow(nameServerRef);
//
//			String name = "Concordia";
//			NameComponent path[] = nameServer.to_name(name);
//			nameServer.rebind(path, serverInterface);

//			System.out.println("Concordia Server is Running...");
//			startUDP();

//			orb.run();

//		} catch (Exception e) {
			// TODO: handle exception
//			System.out.println("Error in Concordia Server :" + e);
//			e.printStackTrace();
//		}

	}

	/**
	 * Method to start the thread for UDP communication.
	 */
	public static void startUDP() {

		// UDP communication
		Runnable task = () -> {
			receive();
		};
		Thread thread = new Thread(task);
		thread.start();
	}

	/**
	 * Method to send the request message to other server and wait for the reply
	 * message.
	 * 
	 * @param serverPort Port number of the server
	 * @param msg        request message to be sent
	 * @return replyMessage a reply message
	 */
	public static String sendMessage(int serverPort, String msg) {
		DatagramSocket aSocket = null;
		String replyMessage = null;
		try {
			System.out.println("Entering Concordia Server");
			aSocket = new DatagramSocket();
			byte[] message = msg.getBytes();
			Charset charsetToSend = Charset.forName("UTF-8");
			message = msg.getBytes(charsetToSend);
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from Concordia to server with port number " + serverPort + " is: "
					+ new String(request.getData()));
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			System.out.println(
					"Reply received from the server with port number " + serverPort + " is: " + data(reply.getData()));
			replyMessage = data(reply.getData()).toString();

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return replyMessage;
	}

	/**
	 * UDP method to receive messages and send reply back to the server through
	 * socket communication.
	 */
	public static void receive() {
		DatagramSocket aSocket = null;
		String returnAction = "";
		Thread receive = null;
		try {
			aSocket = new DatagramSocket(6666);
			System.out.println("Concordia Server for UDP/IP Started...");
			while (true) {
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				if (request.getData() != null) {
					String[] data = new String(data(request.getData())).split(",");
					if (data[0].equalsIgnoreCase("Borrow")) {
						returnAction = borrowItem(data[1], data[2]);
					} else if (data[0].equalsIgnoreCase("FindItem")) {
						returnAction = findItem(data[1], data[2]);
					} else if (data[0].equalsIgnoreCase("ReturnItem")) {
						returnAction = returnItem(data[1], data[2]);
					} else if (data[0].equalsIgnoreCase("WaitList")) {
						returnAction = waitList(data[1], data[2]);
					} else if (data[0].equalsIgnoreCase("RemoveAllItems")) {
						returnAction = removeUserItemsList(data[1]);
					} else if (data[0].equalsIgnoreCase("AllocateItem")) {
						returnAction = String.valueOf(allocateItem(data[1], data[2]));
					} else if (data[0].equalsIgnoreCase("FindExchangeItem")) {
						returnAction = findExchangeItemAvailability(data[1], data[2]);
					}
				}
				DatagramPacket reply = new DatagramPacket(returnAction.getBytes(), returnAction.length(),
						request.getAddress(), request.getPort());
				aSocket.send(reply);
				receive = new Thread("new Thread");
				receive.start();
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	/**
	 * Method to borrow item from Concordia Library.
	 * 
	 * @param userID
	 * @param itemID
	 * @return response
	 */
	private static String borrowItem(String userID, String itemID) {
		String response = "";
		try {
			System.out.println("Trying to borrow item from Concordia.");
			String value = conLibrary.get(itemID);
			System.out.println(value);
			String split[] = value.split(",");
			int quantity = Integer.parseInt(split[1]);
			if (quantity > 0) {
				quantity--;
				value = split[0] + "," + quantity;
				conLibrary.put(itemID, value);
				response = "Success";
				System.out.println("User " + userID + " has borrowed the item " + itemID
						+ " from Concordia library!! Availablity : " + value);
			} else if (quantity == 0) {
				response = "Cannot be Borrowed";
			}
		} catch (Exception e) {
			response = "Failure";
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * Method for converting byte into a string representation.
	 * 
	 * @param a
	 * @return ret
	 */
	public static StringBuilder data(byte[] a) {
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}

	/**
	 * Method to find the required item in the Concordia Library.
	 * 
	 * @param userID
	 * @param itemName
	 * @return response
	 */
	private static String findItem(String userID, String itemName) {
		String response = "";
		Iterator<Entry<String, String>> mapIterator = conLibrary.entrySet().iterator();
		while (mapIterator.hasNext()) {
			Entry<String, String> item = mapIterator.next();
			String[] itemDetails = item.getValue().split(",");
			if (itemDetails[0].equalsIgnoreCase(itemName)) {
				response = item.getKey() + " " + itemDetails[1];
			}
		}
		return response;
	}

	/**
	 * Method to return item back to the Concordia Library and also allocate the
	 * returned item to the user present in the waitlist.
	 * 
	 * @param userID
	 * @param itemID
	 * @return Success
	 */
	private static String returnItem(String userID, String itemID) {
		try {
			boolean itemAllocated = false;
			String value = ConcordiaServer.conLibrary.get(itemID);
			String[] split = value.split(",");
			int val1 = Integer.parseInt(split[1]) + 1;
			if (conWaitlist.get(itemID) != null) {
				itemAllocated = allocateItem(userID, itemID);
				if (itemAllocated) {
					val1 = val1 - 1;
				}
			}
			ConcordiaServer.convalues = split[0] + "," + val1;
			ConcordiaServer.conLibrary.put(itemID, ConcordiaServer.convalues);
			System.out.println("Hashcon:" + ConcordiaServer.conLibrary);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Success";
	}

	/**
	 * Method to add the user to the waitlist of Concordia Library for the item
	 * which is not currently available.
	 * 
	 * @param userID
	 * @param itemID
	 * @return returnMsg
	 */
	public static String waitList(String userID, String itemID) {
		String waitList = null;
		String returnMsg = "";
		waitList = conWaitlist.get(itemID);
		System.out.println("Before: " + waitList);
		if (waitList == null) {
			conWaitlist.put(itemID, userID);
			returnMsg = "Success";
		} else {
			if (Arrays.asList(waitList.split(",")).contains(userID)) {
				returnMsg = "AlreadyAdded";
			} else {
				waitList += "," + userID;
				conWaitlist.put(itemID, waitList);
				returnMsg = "Success";
			}
		}
		System.out.println("After: " + conWaitlist.get(itemID));
		return returnMsg;
	}

	public void run() {
		running = true;
	}

	/**
	 * Method to populate the hashmap.
	 * 
	 * @param userID
	 * @param returnItems
	 * @throws Exception
	 */
	public static void populateReturnItemsList(String userID, HashMap<String, String> returnItems) throws Exception {
		List<String> userHistoryList = new ArrayList<String>();
		userHistoryList.addAll(ConcordiaServer.userHistory.get(userID));
		for (String userHistory : userHistoryList) {
			String[] itemStatus = userHistory.split(",");
			returnItems.put(itemStatus[0], itemStatus[1]);
		}
	}

	/**
	 * Method to allocate item to the user in the waitlist.
	 * 
	 * @param userID
	 * @param itemID
	 * @return itemAllocated
	 * @throws Exception
	 */
	public static boolean allocateItem(String userID, String itemID) throws Exception {
		boolean itemAllocated = false;
		try {

			String allocatedUser = "";
			if (!itemID.substring(0, 3).equalsIgnoreCase("CON")) {
				int i = 0;
				HashMap<String, String> userItems = new HashMap<String, String>();
				populateReturnItemsList(userID, userItems);
				Iterator<Entry<String, String>> userItemsIT = userItems.entrySet().iterator();
				while (userItemsIT.hasNext()) {
					Entry<String, String> userItem = userItemsIT.next();
					if (itemID.substring(0, 3).equalsIgnoreCase(userItem.getKey().substring(0, 3))
							&& userItem.getValue().equalsIgnoreCase("Borrowed")) {
						i++;
					}
				}
				if (i > 0) {
					itemAllocated = false;
				} else {
					userItems.put(itemID, "Borrowed");
					saveUserHistoryDetails(userID, userItems);
					itemAllocated = true;
					allocatedUser = userID;
					System.out.println(itemID + " has been allocated to " + userID + " from library Waitlist");
				}
			} else {

				String itemDetails = conWaitlist.get(itemID);
				List<String> userIDs = Arrays.asList(itemDetails.split(","));

				for (String user : userIDs) {
					String reqMsg = "AllocateItem" + "," + user + "," + itemID;
					String userIDType = user.substring(0, 3);
					if (userIDType.equalsIgnoreCase("CON")) {
						HashMap<String, String> userItems = new HashMap<String, String>();
						populateReturnItemsList(user, userItems);
						Iterator<Entry<String, String>> userItemsIT = userItems.entrySet().iterator();
						int i = 0;
						while (userItemsIT.hasNext()) {
							Entry<String, String> userItem = userItemsIT.next();
							if (itemID.equalsIgnoreCase(userItem.getKey())
									&& userItem.getValue().equalsIgnoreCase("Borrowed")) {
								i++;
							}
						}
						if (i > 0) {
							itemAllocated = false;
						} else {
							userItems.put(itemID, "Borrowed");
							saveUserHistoryDetails(user, userItems);
							itemAllocated = true;
							allocatedUser = user;
							System.out.println(itemID + " has been allocated to " + user + " from library Waitlist");
						}
					} else if (userIDType.equalsIgnoreCase("MON")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(8888, reqMsg));
					} else if (userIDType.equalsIgnoreCase("MCG")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(7777, reqMsg));
					}
					if (itemAllocated) {
						allocatedUser = user;
						break;
					}
				}
			}
			if (itemAllocated && itemID.substring(0, 3).equalsIgnoreCase("CON")) {
				removeUserFromWaitList(allocatedUser, itemID);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return itemAllocated;
	}

	/**
	 * Method to remove user from waitlist after the item is allocated to the user.
	 * 
	 * @param allocatedUser
	 * @param itemID
	 */
	private static void removeUserFromWaitList(String allocatedUser, String itemID) {
		String userIDs = conWaitlist.get(itemID);
		String updatedUserList = "";
		List<String> userIDList = Arrays.asList(userIDs.split(","));
		List<String> updatedList = new ArrayList<String>();
		int i = 0;
		for (String userID : userIDList) {
			if (!userID.equalsIgnoreCase(allocatedUser)) {
				updatedList.add(userID);
			}
			i++;
		}
		for (String userID : updatedList) {
			updatedUserList += updatedUserList.length() > 0 ? "," + userID : userID;
		}
		System.out.println("New List after removing user:" + updatedUserList);
		ConcordiaServer.conWaitlist.put(itemID, updatedUserList);
	}

	/**
	 * Method to remove item from the userItemList.
	 * 
	 * @param itemID
	 * @return Success
	 */
	public static String removeUserItemsList(String itemID) {
		Iterator<Entry<String, List<String>>> userItemsIT = ConcordiaServer.userHistory.entrySet().iterator();
		while (userItemsIT.hasNext()) {
			Entry<String, List<String>> userItem = userItemsIT.next();
		//	System.out.println("Before remove item: " + userItem.getKey());
			List<String> itemsList = new ArrayList<String>();
			itemsList.addAll(userItem.getValue());
			int i = 0;
			boolean userItemMatch = false;
			for (String item : itemsList) {
				String userItemID = item.split(",")[0];
				if (itemID.equalsIgnoreCase(userItemID)) {
					userItemMatch = true;
					break;
				}
				i++;
			}
			System.out.println(i);
			if (userItemMatch) {
				itemsList.remove(i);
				ConcordiaServer.userHistory.put(userItem.getKey(), itemsList);
			}
		}
		return "Success";
	}

	/**
	 * Method to update the user history list.
	 * 
	 * @param userID
	 * @param itemsHistory
	 */
	public static void saveUserHistoryDetails(String userID, HashMap<String, String> itemsHistory) {
		List<String> userHistoryList = new ArrayList<String>();
		Iterator<Entry<String, String>> returnItemsIT = itemsHistory.entrySet().iterator();

		while (returnItemsIT.hasNext()) {
			Entry<String, String> historyItem = returnItemsIT.next();
			userHistoryList.add(historyItem.getKey() + "," + historyItem.getValue());
		}

		ConcordiaServer.userHistory.put(userID, userHistoryList);

	}

	/**
	 * Method to check the availability of the item in the library for exchange.
	 * 
	 * @param userID
	 * @param newItemID
	 * @return true or false
	 */
	private static String findExchangeItemAvailability(String userID, String newItemID) {
		int itemCount;
		itemCount = Integer.parseInt(ConcordiaServer.conLibrary.get(newItemID) != null
				? ConcordiaServer.conLibrary.get(newItemID).split(",")[1]
				: "0");
		if (itemCount > 0) {
			return String.valueOf(true);
		}
		return String.valueOf(false);
	}

}
