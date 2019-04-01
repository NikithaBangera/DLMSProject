package com.dlms.replicas.replica3;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.omg.CORBA.ORB;

import com.dlms.replicas.replica1.ActionServiceImpl;
import com.dlms.replicas.replica1.Concordia;
import com.dlms.replicas.replica1.McGill;
import com.dlms.replicas.replica1.Montreal;

public class ActionserviceImpl implements ActionService {

	/**
	 * 
	 */

	private ORB orb;

	public HashMap<String, HashMap<String, Integer>> libraryInfo = new HashMap<String, HashMap<String, Integer>>();

	public HashMap<String, ArrayList<String>> userInfo = new HashMap<String, ArrayList<String>>();

	HashMap<String, Queue<String>> waitListMap = new HashMap<String, Queue<String>>();

	public static HashMap<String, HashMap<String, Integer>> userlist = new HashMap<String, HashMap<String, Integer>>();
	public static ArrayList<String> managerUserList = new ArrayList<String>();

	Queue<String> waitingQueue = new LinkedList<String>();

	public Logger LOG = Logger.getLogger(ActionServiceImpl.class.getName());

	String id, message;

	int concordiaPort = 5555;
	int mcGillPort = 6666;
	int montrealPort = 7777;
	int flag;

	public String getCurrentDate() {

		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		return sdf.format(date);

	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	public void createLoggingFile(String library) {
		File file = null;
		FileHandler handler = null;
		LogManager.getLogManager().reset();

		file = new File("C:\\Users\\Arpit\\Desktop\\Distributed system\\Server\\logs");

		if (!file.exists()) {
			file.mkdirs();
		}

		// File f=new File(file, id + ".txt");
		File f = new File(file.getAbsolutePath().toString() + "\\" + library + ".txt");
		try {
			if (!f.exists()) {
				f.createNewFile();
			}

			handler = new FileHandler(f.getAbsolutePath(), true);

			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.INFO);
			LOG.addHandler(handler);
		} catch (SecurityException | IOException e) {

			e.printStackTrace();
		}

	}

	public ActionserviceImpl(String library) {

		createLoggingFile(library);

		if (library.equalsIgnoreCase("Concordia")) {
			libraryInfo.put("CON1111", new HashMap<String, Integer>());
			libraryInfo.get("CON1111").put("DISTRIBUTED SYSTEM DESIGN", 0);

			libraryInfo.put("CON6231", new HashMap<String, Integer>());
			libraryInfo.get("CON6231").put("DS", 2);

			libraryInfo.put("CON6651", new HashMap<String, Integer>());
			libraryInfo.get("CON6651").put("ADT", 2);

			libraryInfo.put("CON2222", new HashMap<String, Integer>());
			libraryInfo.get("CON2222").put("ALGORITHM DESIGN TECHNIQUES", 1);

			libraryInfo.put("CON3333", new HashMap<String, Integer>());
			libraryInfo.get("CON3333").put("ADVANCED PROGRAMMING PRACTICES", 3);

			libraryInfo.put("CON4444", new HashMap<String, Integer>());
			libraryInfo.get("CON4444").put("COMPUTER GRAPHICS", 3);

		} else if (library.equalsIgnoreCase("McGill")) {
			libraryInfo.put("MCG1111", new HashMap<String, Integer>());
			libraryInfo.get("MCG1111").put("DISTRIBUTED SYSTEM DESIGN", 1);

			libraryInfo.put("MCG2222", new HashMap<String, Integer>());
			libraryInfo.get("MCG2222").put("ALGORITHM DESIGN TECHNIQUES", 2);

			libraryInfo.put("MCG3333", new HashMap<String, Integer>());
			libraryInfo.get("MCG3333").put("ADVANCED PROGRAMMING PRACTICES", 2);

			libraryInfo.put("MCG4444", new HashMap<String, Integer>());
			libraryInfo.get("MCG4444").put("COMPUTER GRAPHICS", 5);

			userInfo.put("CONU1111", new ArrayList<String>());
			userInfo.get("CONU1111").add("MCG1111");

			userInfo.put("MONU1111", new ArrayList<String>());
			userInfo.get("MONU1111").add("MCG1111");

			userInfo.put("MCGU1111", new ArrayList<String>());
			userInfo.get("MCGU1111").add("MCG1111");

			userInfo.put("CONU4444", new ArrayList<String>());
			userInfo.get("CONU4444").add("MCG3333");

		}

		else {

			libraryInfo.put("MON1111", new HashMap<String, Integer>());
			libraryInfo.get("MON1111").put("DISTRIBUTED SYSTEM DESIGN", 2);

			libraryInfo.put("MON6231", new HashMap<String, Integer>());
			libraryInfo.get("MON6231").put("DS", 1);

			libraryInfo.put("MON2222", new HashMap<String, Integer>());
			libraryInfo.get("MON2222").put("ALGORITHM DESIGN TECHNIQUES", 2);

			libraryInfo.put("MON3333", new HashMap<String, Integer>());
			libraryInfo.get("MON3333").put("ADVANCED PROGRAMMING PRACTICES", 3);

			libraryInfo.put("MON4444", new HashMap<String, Integer>());
			libraryInfo.get("MON4444").put("COMPUTER GRAPHICS", 5);

		}

	}

	public synchronized String addItem(String managerID, String itemID, String itemName, int quantity) {

		if (managerID.charAt(3) == 'U' | managerID.charAt(3) == 'u') {
			message = "Access Denied: A user is not allowed to perform manager operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}

		String message1 = null;

		LOG.info("The current Date and time for this request is: " + getCurrentDate());

		LOG.info("Starting -----ADD------ ITEM operation for Manager with ID: " + managerID + " and ITEM NAME: "
				+ itemName + " and QUANTITY: " + quantity);

		if (libraryInfo.containsKey(itemID)) {

			String str = libraryInfo.get(itemID).entrySet().iterator().next().getKey();
			if (str.equalsIgnoreCase(itemName)) {

				int a = libraryInfo.get(itemID).get(itemName.toUpperCase());
				libraryInfo.get(itemID).put(itemName.toUpperCase(), a + quantity);
				int b = libraryInfo.get(itemID).get(itemName.toUpperCase());

				message1 = "success:Quantity of Item with Item ID:  " + itemID
						+ "  has been successfully increased in the library";
				LOG.info("SUCCESS");

				if (a == 0) {
					String result = waitList(null, itemID, 0);
					message1 = message1 + "\nsuccess:The following user from wait list have been issued the item";

					String answer = null;
					while (result != null && b != 0) {

						String s[] = result.split(",");
						answer = borrowItem(s[0], s[1], 10);

						message1 = message1 + "\n" + answer;
						result = waitList(null, itemID, 0);
						--b;
						libraryInfo.get(itemID).put(itemName.toUpperCase(), b);
						LOG.info(result + "\n");
						LOG.info("---SUCCESS---");

					}

				}

			}

			else {
				message1 = "fail:Invalid Item Name corresponding to the Item ID: " + itemID + "\n";
				LOG.info("----FAILED----");

			}

		} else {
			libraryInfo.put(itemID, new HashMap<String, Integer>());
			libraryInfo.get(itemID).put(itemName.toUpperCase(), quantity);
			message1 = "success:Item with Item ID:  " + itemID + "  has been successfully added in the library";
			LOG.info("SUCCESS");
			int b = libraryInfo.get(itemID).get(itemName.toUpperCase());
			String result = waitList(null, itemID, 0);

			String answer = null;
			while (result != null && b != 0) {

				String s[] = result.split(",");
				answer = borrowItem(s[0], s[1], 10);

				message1 = message1 + "\n" + answer;
				result = waitList(null, itemID, 0);
				--b;
				libraryInfo.get(itemID).put(itemName.toUpperCase(), b);

			}

		}
		LOG.info(message1);

		return message1;

	}

	// removing an Item in the Library --------------------------------

	public synchronized String removeItem(String managerID, String itemID, int quantity) {

		String initialID;
		initialID = managerID.substring(0, 3);
		String itemprefix = itemID.substring(0, 3);

		String reply1;
		String reply2;
		if (managerID.charAt(3) == 'U' | managerID.charAt(3) == 'u') {
			message = "Access Denied: A user is not allowed to perform manager operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}

		// Item has to be deleted from the library completely-- To be completed
		LOG.info("The current Date and time for this request is: " + getCurrentDate());

		LOG.info("Starting -----REMOVE ITEM operation for Manager with ID: " + managerID + "and ITEM ID: " + itemID
				+ " and QUANTITY: " + quantity);

		if (libraryInfo.containsKey(itemID)) {

			if (quantity == 0) {

				libraryInfo.remove(itemID);

				if (initialID.equalsIgnoreCase(itemprefix)) {
					for (String str : userInfo.keySet()) {
						if (userInfo.get(str).contains(itemID)) {

							userInfo.get(str).remove(itemID);

						}

					}
					LOG.info("--SUCCESS--");

					message = "success:Successfully removed itemID. All associated users have been dealt with.";

				} else if (initialID.equalsIgnoreCase("CON")) {
					reply1 = send("removeitem", itemID, null, 6666, managerID);
					reply2 = send("removeitem", itemID, null, 7777, managerID);
					message = reply1 + "\n" + reply2;

					LOG.info("--SUCCESS--");
				}

				else if (initialID.equalsIgnoreCase("MCG")) {
					reply1 = send("removeitem", itemID, null, 5555, managerID);
					reply2 = send("removeitem", itemID, null, 7777, managerID);
					message = reply1 + "\n" + reply2;
					LOG.info("--SUCCESS--");
				}

				else {
					reply1 = send("removeitem", itemID, null, 7777, managerID);
					reply2 = send("removeitem", itemID, null, 6666, managerID);
					message = reply1 + "\n" + reply2;
					LOG.info("--SUCCESS--");
				}

			} else {
				Map.Entry<String, Integer> entry = libraryInfo.get(itemID).entrySet().iterator().next();
				if (entry.getValue() < quantity) {
					message = "fail:Quantity of item entered is greater than the available items in the library. Please try again later with different quantity ";
					LOG.info("--FAILED--");
				} else {
					libraryInfo.get(itemID).put(entry.getKey(), entry.getValue() - quantity);
					message = "success:Item ID " + itemID + " has been successully decreased.";
					LOG.info("--SUCCESS--");
				}
			}

		}

		else {
			message = "fail:Item ID: " + itemID + "does not exist in the library so cannot perform deletion";
			LOG.info("--FAILED--");

		}
		LOG.info(message);
		return message;
	}

	public synchronized String listItemAvailability(String managerID) {

		message = null;
		if (managerID.charAt(3) == 'U' | managerID.charAt(3) == 'u') {
			message = "Access Denied: A user is not allowed to perform manager operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}

		String itemName;
		int quantity;
		message = "Below is the list of all the available items in the Library: \n";
		LOG.info("The current Date and time for this request is: " + getCurrentDate());
		LOG.info("Starting -----LIST ALL AVAILABLE ITEMS----- operation in the library for Manager with ID: "
				+ managerID);

		for (String s : libraryInfo.keySet()) {

			Map.Entry<String, Integer> entry = libraryInfo.get(s).entrySet().iterator().next();
			itemName = entry.getKey();
			quantity = entry.getValue();
//			booksInLibrary = booksInLibrary.concat(thisEntry.getKey() + "-" + thisEntry.getValue().split(",")[0] + ","
//					+ thisEntry.getValue().split(",")[1] + ";");
			message = message + "Item Id: " + s + "    Item Name: " + itemName + "    Quantity: " + quantity + "\n";

		}
		LOG.info("--SUCCESS--");

		LOG.info(message);

		return message;
	}

	@Override
	public synchronized String borrowItem(String userID, String itemID, int numberOfDays) {

		String reply1;
		String userPrefix = userID.substring(0, 3);
		String itemPrefix = itemID.substring(0, 3);

		if (userID.charAt(3) == 'M' | userID.charAt(3) == 'm') {
			message = "Access Denied: A manager is not allowed to perform user operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}
		for (int i = 0; i < 5; i++) {
			System.out.println(i);
		}

		LOG.info("The current Date and time for this request is: " + getCurrentDate());

		LOG.info("Starting -----BORROW ITEM------  operation for User with ID: " + userID + " and ITEM ID: " + itemID
				+ " and number of days: " + numberOfDays);

		if (userPrefix.equalsIgnoreCase(itemPrefix) || numberOfDays == 0) {

			if (libraryInfo.containsKey(itemID))

			{

				Map.Entry<String, Integer> entry = libraryInfo.get(itemID).entrySet().iterator().next();
				if (entry.getValue() > 0) {
					if (userInfo.containsKey(userID) && !userInfo.get(userID).contains(itemID)) {
						userInfo.get(userID).add(itemID);
						entry.setValue(entry.getValue() - 1);
						libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
						message = "success: Item ID: " + itemID
								+ " has been successfully issued to the user with user ID: " + userID + ";\n";
						LOG.info("----SUCCESS----");
						LOG.info(message);

						return message;
					} else if (!userInfo.containsKey(userID)) {
						userInfo.put(userID, new ArrayList<String>());
						userInfo.get(userID).add(itemID);
						entry.setValue(entry.getValue() - 1);
						libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
						message = "success: Item ID: " + itemID
								+ " has been successfully issued to the user with user ID: " + userID + ";\n";
						LOG.info("----SUCCESS----");
						LOG.info(message);

						return message;

					} else {

						message = "fail: Item ID: " + itemID + "is already issued to the user with user ID: " + userID
								+ ";\n";
						LOG.info("----FAILED----");
						LOG.info(message);
					}

				} else {
					message = "waitingqueue";

					LOG.info(
							"Item not available. Sending request to the user If a user would like to be added in the waiting queue");
					return message;

				}

			} else {
				message = "fail: Item does not exist in the library";
				message = "waitingqueue";
				LOG.info(
						"Item not available. Sending request to the user If a user would like to be added in the waiting queue");

				return message;

			}
		}

		else if (itemPrefix.equalsIgnoreCase("CON")) {

			LOG.info("Item belongs to CONCORDIA Library. SENDING REQUEST to CONCORDIA library to borrow an item");
			reply1 = send("borrowitem", itemID, null, 5555, userID);
			LOG.info("RESPONSE recieved from CONCORDIA library.");
			message = reply1;

		} else if (itemPrefix.equalsIgnoreCase("MCG")) {

			LOG.info("Item belongs to MCGILL Library. SENDING REQUEST to MCGILL library to borrow an item");

			reply1 = send("borrowitem", itemID, null, 6666, userID);
			LOG.info("RESPONSE recieved from MCGILL library.");
			message = reply1;

		} else {

			LOG.info("Item belongs to MONTREAL Library. SENDING REQUEST to MONTREAL library to borrow an item");

			reply1 = send("borrowitem", itemID, null, 7777, userID);
			LOG.info("RESPONSE recieved from MONTREAL library.");

			message = reply1;

		}

		LOG.info("result is: " + message);
		LOG.info("Sending result back to the user.");
		return message;
	}

	@Override
	public synchronized String findItem(String userID, String itemName) {

		String reply1;
		String reply2;

		if (userID.charAt(3) == 'M' | userID.charAt(3) == 'm') {
			message = "Access Denied: A manager is not allowed to perform user operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}

		LOG.info("The current Date and time for this request is: " + getCurrentDate());

		LOG.info(
				"Starting -----FIND ITEM------  operation for User with ID: " + userID + " and ITEM NAME: " + itemName);

		message = "fail: Item with name: " + itemName + " does not exist in your library.";

		for (String s : libraryInfo.keySet()) {

			if (libraryInfo.get(s).containsKey(itemName)) {
				message = "success: " + s + " " + libraryInfo.get(s).get(itemName) + "\n";
				LOG.info("----SUCCESS----");
				break;
			}
		}

		if (userID.substring(0, 3).equalsIgnoreCase("CON")) {

			LOG.info(" SENDING REQUEST to MCGILL library to find an item");
			reply1 = send("finditem", null, itemName, 6666, userID);
			LOG.info("RESPONSE recieved from MCGILL library.");

			LOG.info(" SENDING REQUEST to MONTREAL library to find an item");
			reply2 = send("finditem", null, itemName, 7777, userID);
			LOG.info("RESPONSE recieved from MONTREAL library.");
			message = message + "\n" + reply1 + "\n" + reply2;
		}

		else if (userID.substring(0, 3).equalsIgnoreCase("MCG")) {

			LOG.info(" SENDING REQUEST to CONCORDIA library to find an item");
			reply1 = send("finditem", null, itemName, 5555, userID);
			LOG.info("RESPONSE recieved from CONCORDIA library.");

			LOG.info(" SENDING REQUEST to MONTREAL library to find an item");
			reply2 = send("finditem", null, itemName, 7777, userID);
			LOG.info("RESPONSE recieved from MONTREAL library.");

			message = message + "\n" + reply1 + "\n" + reply2;
		} else {

			LOG.info(" SENDING REQUEST to MCGILL library to find an item");
			reply1 = send("finditem", null, itemName, 6666, userID);
			LOG.info("RESPONSE recieved from MCGILL library.");

			LOG.info(" SENDING REQUEST to CONCORDIA library to find an item");
			reply2 = send("finditem", null, itemName, 5555, userID);
			LOG.info("RESPONSE recieved from CONCORDIA library.");

			message = message + "\n" + reply1 + "\n" + reply2;

		}

		LOG.info("Result is: " + message);
		LOG.info("Sending result back to the user.");

		return message;
	}

	@Override
	public synchronized String returnItem(String userID, String itemID) {

		String userprefix = userID.substring(0, 3);
		String itemprefix = itemID.substring(0, 3);

		if (userID.charAt(3) == 'M' | userID.charAt(3) == 'm') {
			message = "Access Denied: A manager is not allowed to perform user operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}

		LOG.info("The current Date and time for this request is: " + getCurrentDate());

		LOG.info("Starting -----RETURN ITEM------  operation for User with ID: " + userID + " and ITEM ID: " + itemID);

		String reply1;

		if (userprefix.equalsIgnoreCase(itemprefix)) {
			if (libraryInfo.containsKey(itemID)) {
				if (userInfo.containsKey(userID) && userInfo.get(userID).contains(itemID)) {
					userInfo.get(userID).remove(itemID);
					Map.Entry<String, Integer> entry = libraryInfo.get(itemID).entrySet().iterator().next();
					libraryInfo.get(itemID).put(entry.getKey(), entry.getValue() + 1);

					LOG.info("Extracting out first user from waiting queue to issue this item.");

					String result = waitList(null, itemID, 0);

					if (result == null) {

						LOG.info("----SUCCESS----");
						message = "success: Item returned to the library successfully. Have a nice day!";

					} else {

						String s[] = result.split(",");
						String answer = borrowItem(s[0], s[1], 0);
						message = "success: Item returned to the library successfully. Have a nice day!";
						LOG.info("----SUCCESS----");
						message = message + "Waiting queue result: " + answer;

					}

					// entry.setValue(entry.getValue() - 1);

				} else {

					message = "fail: Item: " + itemID + " is not issued to user" + userID + "\n";
					LOG.info("----FAILED----");
				}

			}

			else {

				message = "fail: No such Item ID Exist in the library";
				LOG.info("----FAILED----");
			}

		}

		else if (itemprefix.equalsIgnoreCase("CON")) {

			LOG.info("Item belongs to CONCORDIA Library. SENDING REQUEST to CONCORDIA library to borrow an item");
			reply1 = send("returnitem", itemID, null, 5555, userID);
			LOG.info("RESPONSE recieved from CONCORDIA library. ");
			message = reply1;

		} else if (itemprefix.equalsIgnoreCase("MCG")) {

			LOG.info("Item belongs to MCGILL Library. SENDING REQUEST to MCGILL library to borrow an item");
			reply1 = send("returnitem", itemID, null, 6666, userID);
			LOG.info("RESPONSE recieved from MCGILL library. ");

			message = reply1;

		} else {

			LOG.info("Item belongs to MONTREAL Library. SENDING REQUEST to MONTREAL library to borrow an item");
			reply1 = send("returnitem", itemID, null, 7777, userID);
			LOG.info("RESPONSE recieved from MONTREAL library. ");

			message = reply1;
		}

		LOG.info("The Result is: ");
		LOG.info(message);
		LOG.info("Sending reply back to the client (User) ");

		return message;

	}

	public String waitList(String userID, String itemID, int numberOfDays) {

		if (!waitListMap.containsKey(itemID) & userID != null) {

			LOG.info("Adding user with userID: " + userID + " in the waiting list for an item with itemID: " + itemID);
			LOG.info("----SUCCESS----");
			waitListMap.put(itemID, new LinkedList<String>());
			waitListMap.get(itemID).add(userID);

		} else if (waitListMap.containsKey(itemID) & userID != null) {

			LOG.info("Adding user with userID: " + userID + " in the waiting list for an item with itemID: " + itemID);
			waitListMap.get(itemID).add(userID);
			LOG.info("--SUCCESS");
		} else {
			if (waitListMap.containsKey(itemID) && !waitListMap.get(itemID).isEmpty()) {
				userID = waitListMap.get(itemID).poll();
				LOG.info(" UserID: " + userID + "Removing this user ID from the Waiting queue.");
				System.out.println(userID + "added");
				LOG.info("----SUCCESS----");
				return userID + "," + itemID;
			} else {
				LOG.info("Wait list for itemID: " + itemID + " is empty");

				return null;
			}
		}
		LOG.info("User has been added in the wait List. Sending response back to the client.");
		return "User has been added in the wait list";

	}

	String send(String key, String itemID, String itemName, int serverPort, String userID) {
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {

			key = key + "," + itemID + "," + itemName + "," + serverPort + "," + userID;
			aSocket = new DatagramSocket();
			byte[] mess = key.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(mess, key.length(), aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.receive(reply);

			messageReceived = new String(reply.getData());
			messageReceived = messageReceived.trim();

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

		return messageReceived;

	}

	@Override
	public synchronized String exchangeItem(String userID, String newItemID, String oldItemID) {

		String reply1;
		String userPrefix = userID.substring(0, 3);
		String itemPrefix = oldItemID.substring(0, 3);

		if (userID.charAt(3) == 'M' | userID.charAt(3) == 'm') {
			message = "Access Denied: A manager is not allowed to perform user operations.";
			LOG.info("--FAILED--Access Denied");
			LOG.info(message);

			return message;
		}

		LOG.info("The current Date and time for this request is: " + getCurrentDate());

		LOG.info("Starting -----EXCHANGE ITEM------  operation for User with ID: " + userID + " and OLD ITEM ID: "
				+ oldItemID + " and NEW ITEM ID: " + newItemID);
		System.out.println(userID + " " + newItemID + " " + oldItemID);
		if (userPrefix.equalsIgnoreCase(itemPrefix)) {
			if (userInfo.containsKey(userID)) {
				if (userInfo.get(userID).contains(oldItemID)) {
					if (userPrefix.equalsIgnoreCase(newItemID.substring(0, 3))) {
						if (libraryInfo.get(newItemID) != null) {
							Map.Entry<String, Integer> entry = libraryInfo.get(newItemID).entrySet().iterator().next();
							if (getItemCount(newItemID) > 0) {
								entry.setValue(entry.getValue() - 1);
								libraryInfo.get(newItemID).put(entry.getKey(), entry.getValue());
								// userInfo.put(userID, new ArrayList<String>());
								userInfo.get(userID).add(newItemID);
								String x = returnItem(userID, oldItemID);
								message = "success: Item with ITEM ID: " + oldItemID
										+ " has been successfully exchanged with item ID: " + newItemID + " result: "
										+ x;
								LOG.info("----SUCCESS----");
								LOG.info(message);
							} else {
								message = "fail: Sorry. Item to be exchanged is currently not available";
								LOG.info("----FAILURE----");
								LOG.info(message);
							}
						} else {
							message = "fail: Item cannot be exchanged as it does not exist in the library.";
							LOG.info("----FAILURE----");
							LOG.info(message);
						}

					} else if (newItemID.substring(0, 3).equalsIgnoreCase("CON")) {
						LOG.info("...Sending Request to CONCORDIA library to borrow an item...");

						reply1 = send("borrowitem", newItemID, null, 5555, userID);
						LOG.info("...Request recieved from CONCORDIA library to borrow an item...");

						if (reply1 != null && reply1.endsWith(userID)) {
							String res = returnItem(userID, oldItemID);
							if (res.endsWith("!")) {
								message = "success: Item with ITEM ID: " + oldItemID
										+ " has been successfully exchanged with item ID: " + newItemID;
								LOG.info("----SUCCESS----");
								LOG.info(message);
							} else {
								reply1 = send("returnitem", newItemID, null, 5555, userID);
								message = "fail: Item to be returned is invalid so cannot exchange Item";
								LOG.info("----FAILURE----");
								LOG.info(message);
							}
						} else {

							if (reply1.equalsIgnoreCase("waitingqueue")) {
								message = "fail: Sorry. Item to be exchanged is currently not available";
								LOG.info("----FAILURE----");
								LOG.info(message);
							} else {
								LOG.info("----FAILURE----");
								LOG.info(message);
								message = reply1;
							}
						}

					} else if (newItemID.substring(0, 3).equalsIgnoreCase("MCG")) {
						LOG.info("...Sending Request to MCGILL library to borrow an item...");
						reply1 = send("borrowitem", newItemID, null, 6666, userID);
						LOG.info("...Request recieved from MCGILL library to borrow an item...");
						if (reply1 != null && reply1.endsWith(userID)) {
							String res = returnItem(userID, oldItemID);
							if (res.endsWith("!")) {
								message = "success: Item with ITEM ID: " + oldItemID
										+ " has been successfully exchanged with item ID: " + newItemID;
								LOG.info("----SUCCESS----");
								LOG.info(message);
							} else {
								reply1 = send("returnitem", newItemID, null, 6666, userID);
								message = reply1 + ", Item to be returned is invalid So cannot exchange Item";
								LOG.info("----FAILURE----");
								LOG.info(message);
							}
						} else {

							if (reply1.equalsIgnoreCase("waitingqueue")) {
								message = "fail: Sorry. Item to be exchanged is currently not available";
								LOG.info("----FAILURE----");
								LOG.info(message);
							} else {
								message = reply1;
								LOG.info("----FAILURE----");
								LOG.info(message);
							}
						}

					} else {
						LOG.info("...Sending Request to Montreal library to borrow an item...");
						reply1 = send("borrowitem", newItemID, null, 7777, userID);
						LOG.info("...Request recieved from Montreal library to borrow an item...");
						if (reply1 != null && reply1.endsWith(userID)) {
							String res = returnItem(userID, oldItemID);
							if (res.endsWith("!")) {
								message = "success: Item with ITEM ID: " + oldItemID
										+ " has been successfully exchanged with item ID: " + newItemID;
								LOG.info("----SUCCESS----");
								LOG.info(message);

							} else {
								reply1 = send("returnitem", newItemID, null, 7777, userID);
								message = "fail: Item to be returned is invalid So cannot exchange Item";
								LOG.info("----FAILURE----");
								LOG.info(message);
							}
						} else {
							if (reply1.equalsIgnoreCase("waitingqueue")) {
								message = "fail: Sorry. Item to be exchanged is currently not available";
								LOG.info("----FAILURE----");
								LOG.info(message);
							} else {
								message = reply1;
								LOG.info("----FAILURE----");
								LOG.info(message);
							}
						}

					}

				} else {
					message = "fail: Item with ITEM ID: " + oldItemID + " is not issued to user with user ID: "
							+ userID;
					LOG.info("----FAILURE----");
					LOG.info(message);

				}
			} else {
				message = "fail: User with user ID " + userID
						+ "does not exist. Item is not issued to this user. Try again later ";
				LOG.info("----FAILURE----");
				LOG.info(message);
			}

		} else if (itemPrefix.equalsIgnoreCase("CON")) {

			LOG.info(
					"Old Item that is to exchanged belongs to CONCORDIA Library. SENDING REQUEST to CONCORDIA library to BORROW an item");
			if (newItemID.substring(0, 3).equalsIgnoreCase(userID.substring(0, 3))
					&& libraryInfo.containsKey(newItemID)) {
				Map.Entry<String, Integer> entry = libraryInfo.get(newItemID).entrySet().iterator().next();
				entry.setValue(entry.getValue() - 1);
				libraryInfo.get(newItemID).put(entry.getKey(), entry.getValue());
				// userInfo.put(userID, new ArrayList<String>());
				if (userInfo.containsKey(userID)) {
					userInfo.get(userID).add(newItemID);
				} else {
					userInfo.put(userID, new ArrayList<String>());
					userInfo.get(userID).add(newItemID);
				}
				reply1 = userID;
			} else {
				reply1 = send("exchangeitem", newItemID, null, getPortNumber(userID, newItemID), userID);
			}

			if (reply1.endsWith(userID)) {
				int reply2 = exchangeItemHelper(userID, newItemID, oldItemID);
				if (reply2 == 0) {
					if (newItemID.substring(0, 3).equalsIgnoreCase(userID.substring(0, 3))) {
						returnItem(userID, newItemID);
					} else {
						reply1 = send("returnitem", newItemID, null, getPortNumber(userID, newItemID), userID);
					}
					message = "fail: Item to be returned has not been issued to the user";
					LOG.info("----FAILURE----");
					LOG.info(message);
				} else {
					message = "success: Item with ITEM ID: " + oldItemID
							+ " has been successfully exchanged with item ID: " + newItemID;
					LOG.info("----SUCCESS----");
					LOG.info(message);
				}
			} else {
				if (reply1.equalsIgnoreCase("waitingqueue")) {
					message = "fail: Sorry. Item to be exchanged is currently not available";
					LOG.info("----FAILURE----");
					LOG.info(message);
				} else {
					message = reply1;
					LOG.info("----FAILURE----");
					LOG.info(message);
				}
			}

		} else if (itemPrefix.equalsIgnoreCase("MCG")) {

			LOG.info(
					"Old Item that is to be exchanged belongs to MCGILL Library. SENDING REQUEST to MCGILL library to BORROW an item");
			if (newItemID.substring(0, 3).equalsIgnoreCase(userID.substring(0, 3))
					&& libraryInfo.containsKey(newItemID)) {
				Map.Entry<String, Integer> entry = libraryInfo.get(newItemID).entrySet().iterator().next();
				entry.setValue(entry.getValue() - 1);
				libraryInfo.get(newItemID).put(entry.getKey(), entry.getValue());
				// userInfo.put(userID, new ArrayList<String>());
				if (userInfo.containsKey(userID)) {
					userInfo.get(userID).add(newItemID);
				} else {
					userInfo.put(userID, new ArrayList<String>());
					userInfo.get(userID).add(newItemID);
				}
				reply1 = userID;
			} else {
				reply1 = send("exchangeitem", newItemID, null, getPortNumber(userID, newItemID), userID);
			}

			LOG.info("RESPONSE recieved from MCGILL library. ");

			if (reply1.endsWith(userID)) {
				int reply2 = exchangeItemHelper(userID, newItemID, oldItemID);
				if (reply2 == 0) {
					if (newItemID.substring(0, 3).equalsIgnoreCase(userID.substring(0, 3))) {
						returnItem(userID, newItemID);
					} else {
						reply1 = send("returnitem", newItemID, null, getPortNumber(userID, newItemID), userID);
					}
					message = "fail: Item to be returned has not been issued to the user";
					LOG.info("----FAILURE----");
					LOG.info(message);
				} else {
					message = "success: Item with ITEM ID: " + oldItemID
							+ " has been successfully exchanged with item ID: " + newItemID;
					LOG.info("----SUCCESS----");
					LOG.info(message);
				}
			} else {
				if (reply1.equalsIgnoreCase("waitingqueue")) {
					message = "fail: Sorry. Item to be exchanged is currently not available";
					LOG.info("----FAILURE----");
					LOG.info(message);
				} else {
					message = reply1;
					LOG.info("----FAILURE----");
					LOG.info(message);
				}
			}

		} else {

			LOG.info(
					"Old Item that is to be exchanged belongs to Montreal Library. SENDING REQUEST to Montreal library to BORROW an item");
			if (newItemID.substring(0, 3).equalsIgnoreCase(userID.substring(0, 3))
					&& libraryInfo.containsKey(newItemID)) {
				Map.Entry<String, Integer> entry = libraryInfo.get(newItemID).entrySet().iterator().next();
				entry.setValue(entry.getValue() - 1);
				libraryInfo.get(newItemID).put(entry.getKey(), entry.getValue());
				// userInfo.put(userID, new ArrayList<String>());
				if (userInfo.containsKey(userID)) {
					userInfo.get(userID).add(newItemID);
				} else {
					userInfo.put(userID, new ArrayList<String>());
					userInfo.get(userID).add(newItemID);
				}
				reply1 = userID;
			} else {
				reply1 = send("exchangeitem", newItemID, null, getPortNumber(userID, newItemID), userID);
			}
			LOG.info("RESPONSE recieved from MONTREAL library. ");

			if (reply1.endsWith(userID)) {
				int reply2 = exchangeItemHelper(userID, newItemID, oldItemID);
				if (reply2 == 0) {
					if (newItemID.substring(0, 3).equalsIgnoreCase(userID.substring(0, 3))) {
						returnItem(userID, newItemID);
					} else {
						reply1 = send("returnitem", newItemID, null, getPortNumber(userID, newItemID), userID);
					}
					message = "fail: Item to be returned has not been issued to the user";
					LOG.info("----FAILURE----");
					LOG.info(message);
				} else {
					message = "success: Item with ITEM ID: " + oldItemID
							+ " has been successfully exchanged with item ID: " + newItemID;
					LOG.info("----SUCCESS----");
					LOG.info(message);
				}
			} else {
				if (reply1.equalsIgnoreCase("waitingqueue")) {
					message = "fail: Sorry. Item to be exchanged is currently not available";
					LOG.info("----FAILURE----");
					LOG.info(message);
				} else {
					message = reply1;
					LOG.info("----SUCCESS----");
					LOG.info(message);
				}
			}
		}

		return message;
	}

	public synchronized int exchangeItemHelper(String userID, String newItemID, String oldItemID) {
		String reply = null;
		int flag = 0;
		if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
			reply = returnItem(userID, oldItemID);
		} else if (oldItemID.substring(0, 3).equalsIgnoreCase("CON")) {
			reply = send("returnItem", oldItemID, null, 5555, userID);
		} else if (oldItemID.substring(0, 3).equalsIgnoreCase("MCG")) {
			reply = send("returnItem", oldItemID, null, 6666, userID);

		} else if (oldItemID.substring(0, 3).equalsIgnoreCase("MON")) {
			reply = send("returnItem", oldItemID, null, 7777, userID);

		}
		System.out.println(reply);
		if (reply.endsWith("successfully")) {
			flag = 1;
		}
		return flag;

	}

	public synchronized int getPortNumber(String userID, String itemID) {
		if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
			return 5555;
		} else if (itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
			return 6666;
		} else if (itemID.substring(0, 3).equalsIgnoreCase("MON")) {
			return 7777;
		} else {
			return 0;
		}

	}

	public synchronized int getItemCount(String itemID) {
		Map.Entry<String, Integer> entry = libraryInfo.get(itemID).entrySet().iterator().next();
		return entry.getValue();

	}

	@Override
	public boolean validateUser(String userID) {
		boolean flag = false;
		switch (userID.substring(0, 3)) {
		case "CON":
			if (userID.charAt(3) == 'U') {
				if (userlist.containsKey(userID))
					flag = true;
			} else {
				if (managerUserList.contains(userID)) {
					flag = true;
				}
			}
			break;
		case "MON":
			if (userID.charAt(3) == 'U') {
				if (userlist.containsKey(userID))
					flag = true;
			} else {
				if (managerUserList.contains(userID)) {
					flag = true;
				}
			}
			break;

		case "MCG":
			if (userID.charAt(3) == 'U') {
				if (userlist.containsKey(userID))
					flag = true;
			} else {
				if (managerUserList.contains(userID)) {
					flag = true;
				}
			}
			break;
		}
		return flag;
	}

}
