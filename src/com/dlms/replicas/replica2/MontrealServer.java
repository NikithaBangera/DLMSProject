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

public class MontrealServer {

	public static Map<String, String> monLibrary = new TreeMap<String, String>();
	public static Map<String, String> monWaitlist = new TreeMap<String, String>();
	public static Map<String, List<String>> userHistory = new TreeMap<String, List<String>>();
	List<String> itemHistory = new ArrayList<String>();
	public static String monvalues = null;
	private static boolean running;
	String nextItem = "";

	/**
	 * Montreal server constructor which initializes the required TreeMaps.
	 */
	public MontrealServer() {

		monLibrary.put("MON2011", "Computer Networks,7");
		monLibrary.put("MON2012", "Systems Software,6");
		monLibrary.put("MON2013", "Distributed Systems,0");
		monLibrary.put("MON2014", "Machine Learning,0");
		monLibrary.put("MON2015", "Advanced Java,0");

		monWaitlist.put("MON2013", "CONU1017,MONU1018");
		monWaitlist.put("MON2014", "MCGU1019,MONU1020");
		monWaitlist.put("MON2015", "MONU1015,MCGU1016");

		itemHistory.add("MON2012,Borrowed");
		itemHistory.add("MON2013,Borrowed");
		itemHistory.add("CON2014,Borrowed");
		itemHistory.add("CON2015,Returned");
		itemHistory.add("MCG2014,Borrowed");
		itemHistory.add("MCG2015,Returned");
		itemHistory.add("MON2015,Returned");

		userHistory.put("MONU1011", itemHistory);
		userHistory.put("MONU1012", itemHistory);
		userHistory.put("MONU1013", itemHistory);
		userHistory.put("MONU1014", itemHistory);
		userHistory.put("MONU1015", itemHistory);
		userHistory.put("MONU1016", itemHistory);
		userHistory.put("MONU1017", itemHistory);
		userHistory.put("MONU1018", itemHistory);
		userHistory.put("MONU1019", itemHistory);
		userHistory.put("MONU1020", itemHistory);

	}

	/**
	 * Main method of Montreal Library Server.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

	//	try {

//			MontrealServer monServer = new MontrealServer();
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
//			Object nameServiceRef = orb.resolve_initial_references("NameService");
//			NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceRef);
//
//			String name = "Montreal";
//			NameComponent path[] = nameService.to_name(name);
//			nameService.rebind(path, serverInterface);

//			System.out.println("Montreal server is running...");
//			startUDP();

//			orb.run();
//		} catch (Exception e) {
//			System.out.println("Error in Montreal Server :" + e);
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
	private static String sendMessage(int serverPort, String msg) {
		DatagramSocket aSocket = null;
		String replyMessage = null;
		try {
			System.out.println("Entering Montreal Server");
			aSocket = new DatagramSocket();
			byte[] message = msg.getBytes();
			Charset charsetToSend = Charset.forName("UTF-8");
			message = msg.getBytes(charsetToSend);
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from Montreal to server with port number " + serverPort + " is: "
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
			aSocket = new DatagramSocket(8888);
			System.out.println("Montreal Server for UDP/IP Started...");
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
	 * Method to return item back to the Montreal Library and also allocate the
	 * returned item to the user present in the waitlist.
	 * 
	 * @param userID
	 * @param itemID
	 * @return Success
	 */
	private static String returnItem(String userID, String itemID) {
		try {
			String value = MontrealServer.monLibrary.get(itemID);
			boolean itemAllocated = false;
			String[] split = value.split(",");
			int val1 = Integer.parseInt(split[1]) + 1;
			if (monWaitlist.get(itemID) != null) {
				itemAllocated = allocateItem(userID, itemID);
				if (itemAllocated) {
					val1 = val1 - 1;
				}
			}
			MontrealServer.monvalues = split[0] + "," + val1;
			MontrealServer.monLibrary.put(itemID, MontrealServer.monvalues);
			System.out.println("Hashmon:" + MontrealServer.monLibrary);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Success";
	}

	/**
	 * Method to find the required item in the Montreal Library.
	 * 
	 * @param userID
	 * @param itemName
	 * @return response
	 */
	private static String borrowItem(String userID, String itemID) {
		String response = "";
		try {
			System.out.println("Trying to borrow item from Montreal.");
			String value = monLibrary.get(itemID);
			System.out.println(value);
			String split[] = value.split(",");
			int quantity = Integer.parseInt(split[1]);
			if (quantity > 0) {
				quantity--;
				value = split[0] + "," + quantity;
				monLibrary.put(itemID, value);
				response = "Success";
				System.out.println("User " + userID + " has borrowed the item " + itemID
						+ " from Montreal library!! Availablity : " + value);
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

	private static String findItem(String userID, String itemName) {
		String response = "";
		Iterator<Entry<String, String>> mapIterator = monLibrary.entrySet().iterator();
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
	 * Method to add the user to the waitlist of Montreal Library for the item which is
	 * not currently available.
	 * 
	 * @param userID
	 * @param itemID
	 * @return returnMsg
	 */
	public static String waitList(String userID, String itemID) {
		String waitList = null;
		String returnMsg = "";
		waitList = monWaitlist.get(itemID);
		if (waitList == null) {
			monWaitlist.put(itemID, userID);
			returnMsg = "Success";
		} else {
			if (Arrays.asList(waitList.split(",")).contains(userID)) {
				returnMsg = "AlreadyAdded";
			} else {
				waitList += "," + userID;
				monWaitlist.put(itemID, waitList);
				returnMsg = "Success";
			}
		}
		System.out.println(monWaitlist.get(itemID));
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
		userHistoryList.addAll(MontrealServer.userHistory.get(userID));
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
			if (!itemID.substring(0, 3).equalsIgnoreCase("MON")) {
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

				String itemDetails = monWaitlist.get(itemID);
				List<String> userIDs = Arrays.asList(itemDetails.split(","));

				for (String user : userIDs) {
					String reqMsg = "AllocateItem" + "," + user + "," + itemID;
					String userIDType = user.substring(0, 3);
					if (userIDType.equalsIgnoreCase("MON")) {
						HashMap<String, String> userItems = new HashMap<String, String>();
						populateReturnItemsList(user, userItems);
						Iterator<Entry<String, String>> userItemsIT = userItems.entrySet().iterator();
						int i = 0;
						while (userItemsIT.hasNext()) {
							Entry<String, String> userItem = userItemsIT.next();
							System.out.println(userItem.getKey() + "," + userItem.getValue());

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
					} else if (userIDType.equalsIgnoreCase("MCG")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(7777, reqMsg));
					} else if (userIDType.equalsIgnoreCase("CON")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(6666, reqMsg));
					}
					if (itemAllocated) {
						allocatedUser = user;
						break;
					}
				}
			}
			if (itemAllocated && itemID.substring(0, 3).equalsIgnoreCase("MON")) {
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
		String userIDs = monWaitlist.get(itemID);
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
		MontrealServer.monWaitlist.put(itemID, updatedUserList);
	}

	/**
	 * Method to remove item from the userItemList.
	 * 
	 * @param itemID
	 * @return Success
	 */
	public static String removeUserItemsList(String itemID) {
		Iterator<Entry<String, List<String>>> userItemsIT = userHistory.entrySet().iterator();
		while (userItemsIT.hasNext()) {
			Entry<String, List<String>> userItem = userItemsIT.next();
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
			if (userItemMatch) {
				itemsList.remove(i);
				userHistory.put(userItem.getKey(), itemsList);
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

		MontrealServer.userHistory.put(userID, userHistoryList);
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
		itemCount = Integer.parseInt(MontrealServer.monLibrary.get(newItemID) != null
				? MontrealServer.monLibrary.get(newItemID).split(",")[1]
				: "0");
		if (itemCount > 0) {
			return String.valueOf(true);
		}
		return String.valueOf(false);
	}

}
