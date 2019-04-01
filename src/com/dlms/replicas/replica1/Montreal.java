package com.dlms.replicas.replica1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.PortableServer.POA;

public class Montreal {

	public static HashMap<String, String> Books = new HashMap<String, String>();
	public static HashMap<String, HashMap<String, Integer>> userlist = new HashMap<String, HashMap<String, Integer>>();
	public static ArrayList<String> managerUserList = new ArrayList<String>();
	public static LinkedHashMap<String, Integer> waitUserList = new LinkedHashMap<String, Integer>();
	public static HashMap<String, LinkedHashMap<String, Integer>> waitlistBook = new HashMap<String, LinkedHashMap<String, Integer>>();
	private static String sendRequestMessage;
	private static String sendRequestReceived;
	private static String dataReceived;
	private static String message;
	public static Logger logger;
	static private FileHandler fileHandler;
	private static boolean running;
	private static String success = "success: ";
	private static String fail = "fail: ";

	protected Montreal() throws RemoteException {
		super();
	}

	public static void startMontrealServer() throws Exception {
		logger = Logger.getLogger(Montreal.class.getName());
		Runnable task = () -> {
			receive();
		};
		Thread thread = new Thread(task);
		thread.start();

		logger.info("Montreal server started");

		System.out.println("Setting Library Details \n");
		logger.info("Setting Library Details \n");
		setLibraryDetails();

	}

	private static void sendMessage(int serverPort) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] message = sendRequestMessage.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(message, sendRequestMessage.length(), aHost, serverPort);
			aSocket.send(request);
			logger.info("Request message sent from the Montreal to server with port number " + serverPort + " is: "
					+ new String(request.getData()));
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			logger = Logger.getLogger(Montreal.class.getName());
			logger.setUseParentHandlers(false);
			try {
				// This block configure the logger with handler and formatter
				fileHandler = new FileHandler("Logs/Server/Montreal.log");
				logger.addHandler(fileHandler);

				SimpleFormatter formatter = new SimpleFormatter();
				fileHandler.setFormatter(formatter);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dataReceived = null;
			dataReceived = new String(reply.getData()).trim();
			logger.info("Reply received from the server with port number " + serverPort + " to Montreal server is: "
					+ dataReceived);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null) {
				aSocket.close();
				fileHandler.close();
			}

		}
	}

	private static void receive() {
		Thread receive = null;
		DatagramSocket aSocket = null;

		logger = Logger.getLogger(Montreal.class.getName());
		logger.setUseParentHandlers(false);
		try {
			// This block configure the logger with handler and formatter
			fileHandler = new FileHandler("Logs/Server/Montreal.log");
			logger.addHandler(fileHandler);

			SimpleFormatter formatter = new SimpleFormatter();
			fileHandler.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			String func = null;
			String repMessage = "";
			aSocket = new DatagramSocket(2222);

			System.out.println("Server 2222 Started............");
			while (true) {
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				sendRequestReceived = new String(request.getData());
				logger.info("Request received at Montreal Server");
				String[] params = sendRequestReceived.split(",");
				func = params[0].trim().toUpperCase();
				logger.info("Request received is for " + func);
				switch (func) {
				case "BORROW":
					String userID = params[1].trim();
					String itemID = params[2].trim();
					int numberOfDays = Integer.parseInt(params[3].trim());
					message = params[4].trim();
					repMessage = borrowBookToUser(userID, itemID, numberOfDays);
					break;
				case "WAIT":
					userID = params[1].trim();
					itemID = params[2].trim();
					numberOfDays = Integer.parseInt(params[3].trim());
					repMessage = addUserToWaitlist(userID, itemID, numberOfDays);
					break;
				case "RETURN":
					userID = params[1].trim();
					itemID = params[2].trim();
					message = params[3].trim();
					repMessage = returnBookFromUser(userID, itemID);
					break;
				case "FIND":
					String itemName = params[1].trim();
					repMessage = fetchonItemName(Books, itemName).toString();
					break;
				case "REMOVE":
					itemID = params[1].trim();
					removeItemFromUserlist(itemID);
					break;
				case "CHECKWAITLIST":
					userID = params[1].trim();
					itemID = params[2].trim();
					repMessage = checkAndRemoveWaitList(userID, itemID);
					break;
				case "AVAILABLE":
					String newItemID = params[1].trim();
					repMessage = checkAvailability(newItemID);
					break;

				}

				buffer = repMessage.getBytes();
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(),
						request.getPort());
				aSocket.send(reply);
				receive = new Thread("new Thread");
				receive.start();
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null) {
				aSocket.close();
				fileHandler.close();
			}
		}
	}

	public void run() {
		running = true;
	}

	private static synchronized void setLibraryDetails() {

		String[] managerIDs = { "MONM1122", "MONM2233", "MONM3344" };
		Collections.addAll(managerUserList, managerIDs);

		Books.put("MON1111", "Computer Networks,9");
		Books.put("MON2222", "Computer Organization,0");
		Books.put("MON3333", "Discrete Mathmatical Structure,1");
		Books.put("MON4444", "Data Structure,8");

		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		temp.put("MON1111", 12);
		temp.put("MON3333", 23);

		userlist = new HashMap<String, HashMap<String, Integer>>();
		userlist.put("MONU1122", temp);

		HashMap<String, Integer> temp1 = new HashMap<String, Integer>();
		temp1.put("MON3333", 2);
		temp1.put("CON1111", 18);
		userlist.put("MONU2233", temp1);

		logger.info("Books registered while initialization\n");
		Books.forEach((k, v) -> logger.info(("**  " + k + " " + v.split(",")[0] + " " + v.split(",")[1] + "\n")));
		logger.info("User registered while initialization\n");
		userlist.forEach((k, v) -> logger.info(("**  " + k + " " + v + "\n")));

		logger.info("Books WaitList registered while initialization\n");
		if (!waitlistBook.isEmpty())
			waitlistBook.forEach((k, v) -> logger.info(("**  " + k + " " + v + "\n")));
		else
			logger.info("NO Records");

	}

	public static boolean isUserAllowedInterLibraryBorrow(String library, String userID) {

		String key = "";
		HashMap<String, Integer> userinfo;
		logger.info("Checking User Info for accessibilty for requested book\n");
		boolean isUserAllowed = true;
		userinfo = userlist.get(userID);
		if (!userinfo.isEmpty()) {
			Iterator<Entry<String, Integer>> iterator = userinfo.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> thisEntry = iterator.next();
				key = thisEntry.getKey();
				if (key.substring(0, 3).equalsIgnoreCase(library)) {
					isUserAllowed = false;
					break;
				}
			}
		} else {
			isUserAllowed = true;
		}
		if (isUserAllowed)
			message = "Successfully";
		else
			message = key;
		return isUserAllowed;
	}

	private static String setUserDetails(String userID, String itemID, int numberOfDays) {
		HashMap<String, Integer> temp = new HashMap<String, Integer>();

		temp = userlist.get(userID);
		if (!temp.containsKey(itemID) || temp.isEmpty() || temp == null) {
			temp.put(itemID, numberOfDays);
			userlist.put(userID, temp);
			logger.info("Book with book id " + itemID + " Successfully borrowed by user " + userID
					+ ". Added the book to user's borrowed list\n");
			return success + "Book with book id " + itemID + " Successfully borrowed by user " + userID + ".";
		} else {
			logger.info("Item already available in user's borrowed list");
			return fail + "Requested book already exists in user's borrowed list. Cannot borrow the same book again.";
		}

	}

	private static String updateUserBookDetails(String userID, String itemID) {
		HashMap<String, Integer> temp = new HashMap<String, Integer>();

		temp = userlist.get(userID);
		if (temp.containsKey(itemID)) {
			temp.remove(itemID);
			userlist.put(userID, temp);
			logger.info(" Item returned Successfully to the Library and removed from user borrowed list.");
			return success + "Item returned Successfully to the Library and removed from user borrowed list.";
		} else {
			logger.info(" Item with Item ID : " + itemID + " does not exist in User's borrowed List of books.");
			return fail + "Book Not Present : Item with Item ID : " + itemID
					+ " does not exist in User's borrowed List of books.";
		}

	}

	public static String borrowBookToUser(String userID, String itemID, int numberOfDays) {
		String lib = itemID.substring(0, 3).toUpperCase();
		HashMap<String, Integer> userInfo = new HashMap<String, Integer>();
		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		switch (lib) {
		case "MON":

			if (Books.containsKey(itemID)) {
				int quantity = Integer.parseInt(Books.get(itemID).split(",")[1]);
				if (quantity > 0) {
					logger.info("Books in Montreal Library before user request " + Books.toString() + "\n");
					if (userID.contains("MON")) {
						logger.info(userID + " borrowed book details before borrowing " + itemID + ":"
								+ userlist.get(userID) + ".\n");
						message = setUserDetails(userID, itemID, numberOfDays);
						logger.info(userID + " borrowed book details after borrowing " + itemID + ":"
								+ userlist.get(userID) + ".\n");

					}
					if (message.contains("Successfully")) {
						quantity--;
						Books.put(itemID, Books.get(itemID).split(",")[0] + "," + quantity);
						if (waitlistBook.containsKey(itemID)) {
							waitUserList = waitlistBook.get(itemID);
							if (waitUserList.containsKey(userID)) {
								waitUserList.remove(userID);
								waitlistBook.put(itemID, waitUserList);
							}
							if (waitUserList.isEmpty()) {
								waitlistBook.remove(itemID);
							}
						}

						logger.info("Request completed successfully.\n");
					}

					logger.info("Books in Montreal Library after user request " + Books + ".\n");
				} else {
					temp = userlist.get(userID);
					if (waitlistBook.containsKey(itemID)) {
						userInfo = waitlistBook.get(itemID);
					}

					if (temp != null && temp.containsKey(itemID)) {
						logger.info("Request failed: Item requested is already available in user's borrowed list.\n");
						message = fail + "Item already available in user's borrowed list.Can't Borrow Same Item Again.";
					}

					else if (!userInfo.isEmpty() && userInfo.containsKey(userID)) {
						message = "User " + userID + " already present in " + itemID + " waitlist.";
						logger.info("Request failed: " + message);
					} else {
						message = fail + "Unavailable : Book requested is currently not available.";
					}
				}

			} else {
				message = fail + "Book ID is Invalid. No Book exist in library with provide Name.";
				logger.info("Request failed : Book ID Provded is invalid.");
			}
			break;

		case "CON":
			if (isUserAllowedInterLibraryBorrow(lib, userID)) {
				logger.info("User is allowed to borrow requested book.\n");
				logger.info("***********************************************\n");
				logger.info(userID + "User borrowed book details before borrowing " + itemID + ": "
						+ userlist.get(userID) + ".\n");
				sendRequestMessage = "BORROW" + "," + userID + "," + itemID + "," + numberOfDays + "," + message;
				sendMessage(1111);
				message = dataReceived;
				logger.info(message + ".\n");
				if (message.contains("Successfully")) {
					message = setUserDetails(userID, itemID, numberOfDays);
					logger.info(userID + "User borrowed book details after borrowing " + itemID + ": "
							+ userlist.get(userID) + ".\n");
				}

			} else {
				logger.info("Request failed: User is not allowed to borrow requested book. Already Borrowed one book.");
				if (Thread.currentThread().getStackTrace()[3].getMethodName().equalsIgnoreCase("returnItem")
						|| Thread.currentThread().getStackTrace()[3].getMethodName().equalsIgnoreCase("addItem")) {
					sendRequestMessage = "CheckWaitlist" + "," + userID + "," + itemID;
					sendMessage(1111);
					message = dataReceived;
					if (message.contains("removed")) {
						message = "User removed from waitlist";
						logger.info(
								"Request failed: User was not allowed to borrow requested book and is removed from waitlist\n");
					}
				} else {

					message = fail + userID + " has already borrowed one Concordia Library book(Book ID - " + message
							+ "). Maximum borrow limit is one.";
				}
			}
			break;

		case "MCG":
			if (isUserAllowedInterLibraryBorrow(lib, userID)) {
				logger.info("User is allowed to borrow requested book\n");
				logger.info("***********************************************\n");
				logger.info(userID + "User borrowed book details before borrowing " + itemID + ": "
						+ userlist.get(userID) + ".\n");
				sendRequestMessage = "BORROW" + "," + userID + "," + itemID + "," + numberOfDays + "," + message;
				sendMessage(3333);
				message = dataReceived;
				logger.info(message);
				if (message.contains("Successfully")) {
					message = setUserDetails(userID, itemID, numberOfDays);
					logger.info(userID + "User borrowed book details after borrowing " + itemID + ": "
							+ userlist.get(userID) + ".\n");
				}

			} else {
				logger.info("Request failed: User is not allowed to borrow requested book. Already Borrowed one book.");
				if (Thread.currentThread().getStackTrace()[3].getMethodName().equalsIgnoreCase("returnItem")
						|| Thread.currentThread().getStackTrace()[3].getMethodName().equalsIgnoreCase("addItem")) {
					sendRequestMessage = "CheckWaitlist" + "," + userID + "," + itemID;
					sendMessage(3333);
					message = dataReceived;
					if (message.contains("removed")) {
						logger.info(
								"Request failed: User was not allowed to borrow requested book and is removed from waitlist\n");
					}
				} else {
					message = fail + userID + " has already borrowed one McGill Library book(Book ID -" + message
							+ "). Maximum borrow limit is one.";
				}
			}
			break;
		}

		return message;

	}

	public static String addUserToWaitlist(String userID, String itemID, int numberOfDays) {

		String library = itemID.substring(0, 3).toUpperCase();
		LinkedHashMap<String, Integer> waitUList = new LinkedHashMap<String, Integer>();
		int position;
		switch (library) {
		case "MON":
			logger.info("*****Adding User to WaitList*******\n");
			logger.info("Wait list of Montreal Book before :\n");
			waitlistBook.forEach((k, v) -> logger.info(("**  " + k + " " + v + "\n")));
			if (waitlistBook.containsKey(itemID)) {
				logger.info("Adding " + userID + " to waitlist of itemID" + itemID);
				waitUList = waitlistBook.get(itemID);
				waitUList.put(userID, numberOfDays);
				position = waitUList.size();
				waitlistBook.put(itemID, waitUList);
			} else {
				logger.info("Adding " + userID + " to waitlist of itemID" + itemID);
				waitUList.put(userID, numberOfDays);
				position = waitUList.size();
				waitlistBook.put(itemID, waitUList);
			}

			message = success + userID + " added to " + itemID + " waitlist Successfully !!. You are at position  "
					+ position + " in the Queue.";
			logger.info("Request completed successfully.\n");
			logger.info(message);
			logger.info("Wait list of Montreal Book  After :\n");
			waitlistBook.forEach((k, v) -> logger.info(("**  " + k + " " + v + "\n")));
			break;

		case "CON":
			sendRequestMessage = "WAIT" + "," + userID + "," + itemID + "," + numberOfDays;
			sendMessage(1111);
			message = dataReceived;
			break;

		case "MCG":
			sendRequestMessage = "WAIT" + "," + userID + "," + itemID + "," + numberOfDays;
			sendMessage(3333);
			message = dataReceived;
			break;
		}
		return message;
	}

	public static String returnBookFromUser(String userID, String itemID) {

		String lib = itemID.substring(0, 3).toUpperCase();
		switch (lib) {
		case "MON":
			if (Books.containsKey(itemID)) {

				logger.info("Returning Book at Montreal Library\n");
				int quantity = Integer.parseInt(Books.get(itemID).split(",")[1]);
				logger.info("Books in Montreal Library before user request:\n" + Books + "\n");
				if (userID.contains("MON")) {

					message = updateUserBookDetails(userID, itemID);
					logger.info(userID + " borrowed book details after returning " + itemID + ":\n"
							+ userlist.get(userID) + ".\n");
				}
				if (message.contains("Successfully")) {
					quantity++;
					Books.put(itemID, Books.get(itemID).split(",")[0] + "," + quantity);
					logger.info("Request completed successfully.\n");
					logger.info(itemID + " returned successfully.\n");
					if (waitlistBook.containsKey(itemID)) {
						logger.info(" Checking for any available users from \"" + itemID + "\" waitlist");
						logger.info(" Wait List of \"" + itemID + "\" after user request:\n" + waitlistBook.get(itemID)
								+ "\n");
						HashMap<String, Integer> ulist = (HashMap<String, Integer>) waitlistBook.get(itemID);
						String userList = new String();
						if (!ulist.isEmpty()) {
							Iterator<Entry<String, Integer>> iterator = ulist.entrySet().iterator();
							while (iterator.hasNext()) {
								Entry<String, Integer> thisEntry = iterator.next();
								userList = userList.concat(thisEntry.getKey() + "-" + thisEntry.getValue() + ",");

							}
							message = "Borrow" + userList;
						}
					}
				}
				logger.info("Books in Montreal Library after user request :\n" + Books + "\n");

			} else {
				message = fail + "Book ID is Invalid. No Book exist in library with provide Name.";
				logger.info("Request failed.Invalid Book Id provided\n");
			}
			break;

		case "CON":
			logger.info("***********************************************\n");
			logger.info(
					userID + " borrowed book details before returning " + itemID + ":\n" + userlist.get(userID) + "\n");
			message = updateUserBookDetails(userID, itemID);
			if (message.contains("Successfully")) {
				sendRequestMessage = "RETURN" + "," + userID + "," + itemID + "," + message;
				sendMessage(1111);
				message = dataReceived;
			}
			logger.info(
					userID + " borrowed book details after returning " + itemID + ":\n" + userlist.get(userID) + "\n");
			break;

		case "MCG":
			logger.info("***********************************************\n");
			logger.info(
					userID + " borrowed book details before returning " + itemID + ":\n" + userlist.get(userID) + "\n");
			message = updateUserBookDetails(userID, itemID);
			if (message.contains("Successfully")) {
				sendRequestMessage = "RETURN" + "," + userID + "," + itemID + "," + message;
				sendMessage(3333);
				message = dataReceived;
			}
			logger.info(
					userID + " borrowed book details after returning " + itemID + ":\n" + userlist.get(userID) + "\n");
			break;
		}
		return message;
	}

	public static String findItem(String UserId, String itemName) {
		String display = "";
		display = fetchonItemName(Books, itemName);
		logger.info("***********************************************\n");
		sendRequestMessage = "FIND" + "," + itemName;
		sendMessage(1111);
		display = display.concat(dataReceived);
		logger.info("***********************************************\n");
		sendRequestMessage = "FIND" + "," + itemName;
		sendMessage(3333);
		display = display.concat(dataReceived);

		logger.info("Request completed successfully\n");
		return display;

	}

	public static String fetchonItemName(HashMap<String, String> books, String itemName) {
		String result = "";
		logger.info("Fetching " + itemName + " details from Montreal Library");
		for (Map.Entry<String, String> entry : books.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value.split(",")[0].trim().equalsIgnoreCase(itemName)) {
				result = result.concat(key + "-" + value + "'");
			}
		}
		logger.info(itemName + " details available in Montreal Library:\n" + result);

		return result;
	}

	public static String removeItemFromLibrary(String itemID, int quantity) {

		String[] itemInfo;
		String operation = "";
		if (Books.containsKey(itemID)) {
			HashMap<String, String> conBooks = Books;
			itemInfo = conBooks.get(itemID).split(",");
			int oldquantity = Integer.parseInt(itemInfo[1]);
			String itemName = itemInfo[0];
			if (oldquantity >= quantity) {
				int newQuantity = oldquantity - quantity;
				if (quantity != -1) {
					String keyValue = itemName + "," + newQuantity;
					conBooks.put(itemID, keyValue);
					operation = success + "Book's quantity decreased by " + quantity
							+ " Successfully  from the available list! ";
					logger.info("After removal:\n" + Books.toString() + "\n");
					logger.info("Request completed successfully");
				} else if (quantity == -1) {
					Books.remove(itemID);
					removeItemFromUserlist(itemID);
					logger.info("***********************************************\n");
					sendRequestMessage = "REMOVE" + "," + itemID;
					sendMessage(1111);
					logger.info("***********************************************\n");
					sendRequestMessage = "REMOVE" + "," + itemID;
					sendMessage(3333);

					operation = success + "Book removed Successfully and Borrowed List of User's.";
					logger.info("After removal, waitlist is cleared. Available Waitlist:\n" + waitlistBook + "\n");
				}

			}

			else if (oldquantity < quantity) {
				operation = fail + "Invalid Quantity , Quantity provided is more than available quantity.";
				logger.info("Request Failed :  Quantity provided is more than available quantity ");
			}
		} else {
			operation = fail + "Invalid Book : Book is not available in Library.";
			logger.info("Request Failed :  Book Id provided is not available in Library ");
		}
		return operation;
	}

	private static void removeItemFromUserlist(String itemId) {
		logger.info("Before Removal of " + itemId + " from library, Montreal user's Borrow List:\n"
				+ userlist.toString() + "\n");
		logger.info(
				"Before Removal of " + itemId + " from library, Montreal waitList:\n" + waitlistBook.toString() + "\n");
		Iterator<Entry<String, HashMap<String, Integer>>> coIterator = userlist.entrySet().iterator();
		while (coIterator.hasNext()) {
			Entry<String, HashMap<String, Integer>> pair = coIterator.next();
			HashMap<String, Integer> bookChecklist = (HashMap<String, Integer>) pair.getValue();
			if (bookChecklist.containsKey(itemId)) {
				bookChecklist.remove(itemId);
				userlist.put(pair.getKey(), bookChecklist);
				if (waitlistBook.containsKey(itemId)) {
					waitlistBook.remove(itemId);
				}

			}
		}
		logger.info("Before Removal of " + itemId + " from library, Montreal user's Borrow List:\n"
				+ userlist.toString() + "\n");
		logger.info(
				"Before Removal of " + itemId + " from library, Montreal waitList:\n" + waitlistBook.toString() + "\n");
	}

	private static String checkAndRemoveWaitList(String userID, String itemID) {
		message = "";
		if (waitlistBook.containsKey(itemID)) {
			waitUserList = waitlistBook.get(itemID);
			if (waitUserList.containsKey(userID)) {
				waitUserList.remove(userID);
				waitlistBook.put(itemID, waitUserList);
				message = "User removed from waitlist.";
				logger.info(userID + " removed from waitlist of " + itemID + ".\n");
			}
			if (waitUserList.isEmpty()) {
				waitlistBook.remove(itemID);
			}

		}

		return message;
	}

	public static String exchangeItem(String userID, String newItemID, String oldItemID) {
		String operation = "";
		HashMap<String, Integer> uBorrowList = new HashMap<String, Integer>();
		uBorrowList = userlist.get(userID);
		if (!uBorrowList.isEmpty() && uBorrowList.containsKey(oldItemID)) {
			if (!uBorrowList.containsKey(newItemID)) {
				String flag = checkAvailability(newItemID);
				int numberOfDays = uBorrowList.get(oldItemID);
				if (flag.equalsIgnoreCase("pass")) {
					operation = borrowBookToUser(userID, newItemID, numberOfDays);
					if (operation.contains("Success")) {
						operation = returnBookFromUser(userID, oldItemID);
						logger.info("Borrowed Book " + newItemID + " successfully from the Library.\n");
						logger.info("Returning Book " + oldItemID
								+ " successfully to the Library. Cheking for the Waitlist of the returned Book to perform further opertaion.\n");

					} else {
						if (newItemID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
							operation = returnBookFromUser(userID, oldItemID);
							borrowBookToUser(userID, newItemID, numberOfDays);
						} else {
							logger.info(operation);
							return operation;
						}
					}
				} else {
					operation = fail + "\nSorry cannot perform exchange! The requested book " + newItemID
							+ " is not available";
					logger.info(operation);
				}
			} else {
				operation = fail + "\nSorry cannot perform exchange as user already has " + newItemID
						+ " in his borrowed list.";
				logger.info(operation);
			}
		} else {
			operation = fail + "\nUser doesn't have " + oldItemID + " book in his borrowed list.";

			logger.info(operation);
		}
		return operation;
	}

	private static String checkAvailability(String newItemID) {
		String isBookAvailable = "fail";
		switch (newItemID.substring(0, 3).toUpperCase()) {
		case "MON":
			if (!Books.isEmpty() && Books.containsKey(newItemID)
					&& Integer.parseInt(Books.get(newItemID).split(",")[1].trim()) != 0)
				isBookAvailable = "pass";
			break;
		case "CON":
			sendRequestMessage = "AVAILABLE" + "," + newItemID;
			sendMessage(1111);
			isBookAvailable = dataReceived;
			System.out.println("mess  con" + message);
			break;
		case "MCG":
			sendRequestMessage = "AVAILABLE" + "," + newItemID;
			sendMessage(3333);
			isBookAvailable = dataReceived;
			System.out.println("mess  mcg" + message);
			break;

		}

		return isBookAvailable;
	}

//	public static String exchangeItem(String userID, String newItemID, String oldItemID) {
//		String operation = "";
//		HashMap<String, Integer> uBorrowList = new HashMap<String, Integer>();
//		uBorrowList = userlist.get(userID);
//		if (!uBorrowList.isEmpty() && !uBorrowList.containsKey(newItemID)) {
//			if (uBorrowList.containsKey(oldItemID)) {
//				int numberOfDays = uBorrowList.get(oldItemID);
//				message = borrowBookToUser(userID, newItemID, numberOfDays);
//				if (message.contains("Success")) {
//					returnBookFromUser(userID, oldItemID);
//				} else {
//					operation = message;
//				}
//			} else {
//
//				operation = "User doesn't have the item ID";
//			}
//
//		}
//		return operation;
//	}
}
