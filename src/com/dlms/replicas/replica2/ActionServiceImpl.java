package com.dlms.replicas.replica2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.omg.CORBA.ORB;

import com.dlms.replicas.replica1.Concordia;
import com.dlms.replicas.replica1.McGill;
import com.dlms.replicas.replica1.Montreal;

public class ActionServiceImpl implements ActionService {
	private static final long serialVersionUID = 1L;
//	ConcordiaServer conObj = new ConcordiaServer();
//	McgillServer mcgObj = new McgillServer();
//	MontrealServer monObj = new MontrealServer();
	public String userID, itemID;
	public int numberOfDays;
	public String response = "";
	Map<String, String> waitList = null;
	HashMap<String, String> userItems = new HashMap<String, String>();
//	private ORB orb;

	/**
	 * Method to add item into the library which is performed by the library
	 * managers.
	 */
	public String addItem(String managerID, String itemID, String itemName, int quantity) {
		String serverName = (managerID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (managerID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (managerID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		boolean itemAllocated = false;
		try {
			populateWaitList(managerID.substring(0, 3));

			switch (managerID.substring(0, 3)) {
			case "CON":
				if (ConcordiaServer.conLibrary.containsKey(itemID)) {
					String value = ConcordiaServer.conLibrary.get(itemID);
					int val1 = Integer.parseInt(value.substring(value.lastIndexOf(',') + 1)) + quantity;
					if (waitList.get(itemID) != null) {
						int i = 0;
						while (val1 > 0 && !(itemAllocated) && val1 > i) {
							itemAllocated = allocateItem(managerID, itemID, "CON");
							if (itemAllocated) {
								val1--;
								itemAllocated = false;
							}
							i++;
						}
					}
					ConcordiaServer.convalues = itemName + "," + val1;
					ConcordiaServer.conLibrary.put(itemID, ConcordiaServer.convalues);

					logInformationOnServer(managerID, itemID, "Success", "AddItem", true, serverName);
					return ("Success:"+"Item ID's " + itemID + " quantity updated in Concordia Library.");
				} else if (!(ConcordiaServer.conLibrary.containsKey(itemID))) {
					if (waitList.get(itemID) != null) {
						int i = 0;
						while (quantity > 0 && !itemAllocated && quantity > i) {
							itemAllocated = allocateItem(managerID, itemID, "CON");
							if (itemAllocated) {
								quantity--;
								itemAllocated = false;
							}
							i++;
						}
					}
					ConcordiaServer.convalues = itemName + "," + quantity;
					ConcordiaServer.conLibrary.put(itemID, ConcordiaServer.convalues); // add book to hashmap
					logInformationOnServer(managerID, itemID, "Success", "AddItem", true, serverName);
					return ("Success:"+"Item ID " + itemID + " added to the Concordia Library.");
				} else
					logInformationOnServer(managerID, itemID, "Failure", "AddItem", false, serverName);
				return ("Fail:"+"Concordia:Add item operation not successfull");

			case "MCG":
				if (McgillServer.mcgLibrary.containsKey(itemID)) {
					String value = McgillServer.mcgLibrary.get(itemID);
					int val1 = Integer.parseInt(value.substring(value.lastIndexOf(',') + 1)) + quantity;
					if (waitList.get(itemID) != null) {
						int i = 0;
						while (val1 > 0 && !itemAllocated && val1 > i) {
							itemAllocated = allocateItem(managerID, itemID, "MCG");
							if (itemAllocated) {
								val1--;
								itemAllocated = false;
							}
							i++;
						}
					}
					McgillServer.mcgvalues = itemName + "," + val1;
					McgillServer.mcgLibrary.put(itemID, McgillServer.mcgvalues);
					logInformationOnServer(managerID, itemID, "Success", "AddItem", true, serverName);
					return ("Success:"+"Item ID's " + itemID + " quantity updated in Mcgill Library.");
				} else if (!(McgillServer.mcgLibrary.containsKey(itemID))) {
					if (waitList.get(itemID) != null) {
						int i = 0;
						while (quantity > 0 && !itemAllocated && quantity > i) {
							itemAllocated = allocateItem(managerID, itemID, "MCG");
							if (itemAllocated) {
								quantity--;
								itemAllocated = false;
							}
							i++;
						}
					}
					McgillServer.mcgvalues = itemName + "," + quantity;
					McgillServer.mcgLibrary.put(itemID, McgillServer.mcgvalues); // add book to hashmap
					logInformationOnServer(managerID, itemID, "Success", "AddItem", true, serverName);
					return ("Success:"+"Item ID " + itemID + " added to the Mcgill Library.");
				} else
					logInformationOnServer(managerID, itemID, "Failure", "AddItem", false, serverName);
				return ("Fail:"+"Mcgill:Add item operation not successfull");

			case "MON":
				if (MontrealServer.monLibrary.containsKey(itemID)) {
					String value = MontrealServer.monLibrary.get(itemID);
					int val1 = Integer.parseInt(value.substring(value.lastIndexOf(',') + 1)) + quantity;
					if (waitList.get(itemID) != null) {
						int i = 0;
						while (val1 > 0 && !itemAllocated && val1 > i) {
							itemAllocated = allocateItem(managerID, itemID, "MON");
							if (itemAllocated) {
								val1--;
								itemAllocated = false;
							}
							i++;
						}
					}
					MontrealServer.monvalues = itemName + "," + val1;
					MontrealServer.monLibrary.put(itemID, MontrealServer.monvalues);
					logInformationOnServer(managerID, itemID, "Success", "AddItem", true, serverName);
					return ("Success:"+"Item ID's " + itemID + " quantity updated in Montreal Library.");
				} else if (!(MontrealServer.monLibrary.containsKey(itemID))) {
					if (waitList.get(itemID) != null) {
						int i = 0;
						while (quantity > 0 && !itemAllocated && quantity > i) {
							itemAllocated = allocateItem(managerID, itemID, "MON");
							if (itemAllocated) {
								quantity--;
								itemAllocated = false;
							}
							i++;
						}
					}
					MontrealServer.monvalues = itemName + "," + quantity;
					MontrealServer.monLibrary.put(itemID, MontrealServer.monvalues); // add book to hashmap
					logInformationOnServer(managerID, itemID, "Success", "AddItem", true, serverName);
					return ("Success:"+"Item ID " + itemID + " added to the Montreal Library.");
				} else
					logInformationOnServer(managerID, itemID, "Failure", "AddItem", false, serverName);
				return ("Fail:"+"Montreal:Add item operation not successfull");
			}

		} catch (Exception e) {
			System.out.println("Exception caught at AddItem");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to remove item from the library which is performed by the library
	 * managers.
	 */
	public String removeItem(String managerID, String itemID, int quantity) {
		String serverName = (managerID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (managerID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (managerID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		try {
			switch (managerID.substring(0, 3)) {
			case "CON":
				if (quantity == -1) {
					ConcordiaServer.conLibrary.remove(itemID);
					removeItemFromUserList(itemID, quantity, "Concordia");
					logInformationOnServer(managerID, itemID, "Success", "RemoveItem", true, serverName);
					return ("Success:"+"Item ID " + itemID + " is removed from the Concordia Library");
				} else if (quantity > 0) {
					String value = ConcordiaServer.conLibrary.get(itemID);
					String splitt[] = value.split(",");
					int value1 = Integer.parseInt(splitt[1]);
					if (value1 < quantity) {
						logInformationOnServer(managerID, itemID, "Failure", "RemoveItem", false, serverName);
						return ("Fail:"+"Concordia: Cannot reduce the quantity of the item" + itemID);
					} else {
						int value2 = value1 - quantity;
						ConcordiaServer.convalues = splitt[0] + "," + value2;
						ConcordiaServer.conLibrary.put(itemID, ConcordiaServer.convalues);
						logInformationOnServer(managerID, itemID, "Success", "RemoveItem", true, serverName);
						return ("Success:"+"Concordia: Quantity of item Id " + itemID + " has been reduced.");
					}
				} else
					logInformationOnServer(managerID, itemID, "Failure", "RemoveItem", false, serverName);
				return ("Fail:"+"Concordia:Remove item operation not successfull");

			case "MCG":
				if (quantity == -1) {
					McgillServer.mcgLibrary.remove(itemID);
					removeItemFromUserList(itemID, quantity, "Mcgill");
					logInformationOnServer(managerID, itemID, "Success", "RemoveItem", true, serverName);
					return ("Success:"+"Item ID " + itemID + " is removed from the Mcgill Library");
				} else if (quantity > 0) {
					String value = McgillServer.mcgLibrary.get(itemID);
					String splitt[] = value.split(",");
					int value1 = Integer.parseInt(splitt[1]);
					if (value1 < quantity) {
						logInformationOnServer(managerID, itemID, "Failure", "RemoveItem", false, serverName);
						return ("Fail:"+"Mcgill: Cannot reduce the quantity of the item" + itemID);
					} else {
						int value2 = value1 - quantity;
						McgillServer.mcgvalues = splitt[0] + "," + value2;
						McgillServer.mcgLibrary.put(itemID, McgillServer.mcgvalues);
						logInformationOnServer(managerID, itemID, "Success", "RemoveItem", true, serverName);
						return ("Success:"+"Mcgill: Quantity of item Id " + itemID + " has been reduced.");
					}
				} else
					logInformationOnServer(managerID, itemID, "Failure", "RemoveItem", false, serverName);
				return ("Fail:"+"Mcgill:Remove item operation not successfull");

			case "MON":
				if (quantity == -1) {
					MontrealServer.monLibrary.remove(itemID);
					removeItemFromUserList(itemID, quantity, "Montreal");
					logInformationOnServer(managerID, itemID, "Success", "RemoveItem", true, serverName);
					return ("Success:"+"Item ID " + itemID + " is removed from the Montreal Library");
				} else if (quantity > 0) {
					String value = MontrealServer.monLibrary.get(itemID);
					String splitt[] = value.split(",");
					int value1 = Integer.parseInt(splitt[1]);
					if (value1 < quantity) {
						logInformationOnServer(managerID, itemID, "Failure", "RemoveItem", false, serverName);
						return ("Fail:"+"Montreal: Cannot reduce the quantity of the item" + itemID);
					} else {
						int value2 = value1 - quantity;
						MontrealServer.monvalues = splitt[0] + "," + value2;
						MontrealServer.monLibrary.put(itemID, MontrealServer.monvalues);
						logInformationOnServer(managerID, itemID, "Success", "RemoveItem", true, serverName);
						return ("Success:"+"Montreal: Quantity of item Id " + itemID + " has been reduced.");
					}
				} else
					logInformationOnServer(managerID, itemID, "Failure", "RemoveItem", false, serverName);
				return ("Fail:"+"Montreal:Remove item operation not successfull");
			}

		} catch (Exception e) {
			System.out.println("Exception caught at RemoveItem");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to remove items from userHistory list.
	 * 
	 * @param itemID
	 * @param quantity
	 * @param serverName
	 */
	private void removeItemFromUserList(String itemID, int quantity, String serverName) {
		HashMap<String, List<String>> userItems = new HashMap<String, List<String>>();
		String reqMsg = "RemoveAllItems" + "," + itemID;
		if (serverName.equalsIgnoreCase("Concordia")) {
			userItems.putAll(ConcordiaServer.userHistory);
			sendMessage(7777, reqMsg);
			sendMessage(8888, reqMsg);
		} else if (serverName.equalsIgnoreCase("Mcgill")) {
			userItems.putAll(McgillServer.userHistory);
			sendMessage(6666, reqMsg);
			sendMessage(8888, reqMsg);
		} else if (serverName.equalsIgnoreCase("Montreal")) {
			userItems.putAll(MontrealServer.userHistory);
			sendMessage(6666, reqMsg);
			sendMessage(7777, reqMsg);
		}
		Iterator<Entry<String, List<String>>> userItemsIT = userItems.entrySet().iterator();
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
			}
			if (serverName.equalsIgnoreCase("Concordia")) {
				ConcordiaServer.userHistory.put(userItem.getKey(), itemsList);
			} else if (serverName.equalsIgnoreCase("Mcgill")) {
				McgillServer.userHistory.put(userItem.getKey(), itemsList);
			} else if (serverName.equalsIgnoreCase("Montreal")) {
				MontrealServer.userHistory.put(userItem.getKey(), itemsList);
			}
		}
	}

	/**
	 * Method to list the items available in the library which performed by the
	 * library manager.
	 */
	public String listItemAvailability(String managerID) {
		String serverName = (managerID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (managerID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (managerID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		String itemsList = "";
		try {
			switch (managerID.substring(0, 3)) {
			case "CON":
				Iterator mapIterator = ConcordiaServer.conLibrary.entrySet().iterator();
				List<String> conItems = new ArrayList<String>();
				while (mapIterator.hasNext()) {
					Map.Entry pair = (Map.Entry) mapIterator.next();
					conItems.add(pair.getKey() + "," + pair.getValue());
					itemsList = itemsList.length() > 0 ? itemsList.concat(";" + pair.getKey() + "-" + pair.getValue())
							: itemsList.concat(pair.getKey() + "-" + pair.getValue());
				}
				String[] conItemsArray = new String[conItems.size()];
				logInformationOnServer(managerID, itemID, "Success", "ListItem", true, serverName);
				// conItemsArray = conItems.toArray(conItemsArray);
				return "Success:"+itemsList;

			case "MCG":
				Iterator mapIterator1 = McgillServer.mcgLibrary.entrySet().iterator();
				List<String> mcgItems = new ArrayList<String>();
				while (mapIterator1.hasNext()) {
					Map.Entry pair = (Map.Entry) mapIterator1.next();
					mcgItems.add(pair.getKey() + "," + pair.getValue());
					itemsList = itemsList.length() > 0 ? itemsList.concat(";" + pair.getKey() + "-" + pair.getValue())
							: itemsList.concat(pair.getKey() + "-" + pair.getValue());
		
				}
				logInformationOnServer(managerID, itemID, "Success", "ListItem", true, serverName);
				String[] mcgItemsArray = new String[mcgItems.size()];
				// mcgItemsArray = mcgItems.toArray(mcgItemsArray);
				return "Success:"+itemsList;

			case "MON":
				Iterator mapIterator2 = MontrealServer.monLibrary.entrySet().iterator();
				List<String> monItems = new ArrayList<String>();
				while (mapIterator2.hasNext()) {
					Map.Entry pair = (Map.Entry) mapIterator2.next();
					monItems.add(pair.getKey() + "," + pair.getValue());
					itemsList = itemsList.length() > 0 ? itemsList.concat(";" + pair.getKey() + "-" + pair.getValue())
							: itemsList.concat(pair.getKey() + "-" + pair.getValue());

				}
				logInformationOnServer(managerID, itemID, "Success", "ListItem", true, serverName);
				String[] monItemsArray = new String[monItems.size()];
				// monItemsArray = monItems.toArray(monItemsArray);
				return "Success:"+itemsList;
			}
		} catch (Exception e) {
			System.out.println("Exception caught at ListItem");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to borrow item from the library which is performed by the library
	 * user.
	 */
	public String borrowItem(String userID, String itemID, int numberOfDays) {
		// The user borrows the book.
		String borrowResponse = "";
		String serverName = (userID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (userID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (userID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		try {
			if (validateBookAvailability(userID, itemID)) {
				populateReturnItemsList(userID, userItems);
				switch (userID.substring(0, 3)) {
				case "CON":
					if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
						if (userItems.get(itemID) == null || (userItems.get(itemID) != null
								&& userItems.get(itemID).equalsIgnoreCase("Returned"))) {
							String value = ConcordiaServer.conLibrary.get(itemID);
							String split[] = value.split(",");
							int quantity = Integer.parseInt(split[1]);
							int old_quant = quantity;
							if (quantity > 0) {
								quantity--;
								value = split[0] + "," + quantity;
								ConcordiaServer.conLibrary.put(itemID, value);
								userItems.put(itemID, "Borrowed");
								saveUserHistoryDetails(userID, userItems);
								logInformationOnServer(userID, itemID, "Success", "BorrowItem", true, "Concordia");
								return ("Success:"+"User " + userID + " has borrowed the item " + itemID
										+ " from Concordia library.\n");
							} else if (quantity == 0) {
								logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, "Concordia");
							//	return ("Failure:"+"Cannot be Borrowed");
								return ("Fail:Unavailable");
							}
						} else {
							logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, "Concordia");
							return ("Fail:"+"Item already borrowed");
						}
					} else if (itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
						return borrowItemHandler(7777, userID, itemID);

					} else {
						return borrowItemHandler(8888, userID, itemID);
					}

				case "MCG":
					if (itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
						if (userItems.get(itemID) == null || (userItems.get(itemID) != null
								&& userItems.get(itemID).equalsIgnoreCase("Returned"))) {

							// System.out.println("Borrow item for MCG");
							String value = McgillServer.mcgLibrary.get(itemID);
							String split[] = value.split(",");
							int quantity = Integer.parseInt(split[1]);
							int old_quant = quantity;
							if (quantity > 0) {
								quantity--;
								value = split[0] + "," + quantity;
								McgillServer.mcgLibrary.put(itemID, value);
								userItems.put(itemID, "Borrowed");
								saveUserHistoryDetails(userID, userItems);
								logInformationOnServer(userID, itemID, "Success", "BorrowItem", true, "Mcgill");
								return ("Success:"+"User " + userID + " has borrowed the item " + itemID
										+ " from Mcgill library.\n");
							} else if (quantity == 0) {
								logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, "Mcgill");
								//return ("Failure:"+"Cannot be Borrowed");
								return ("Fail:Unavailable");
							}

						} else {
							logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, "Mcgill");
							return ("Fail:"+"Item Already borrowed");
						}
					} else if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
						return borrowItemHandler(6666, userID, itemID);
					} else {
						return borrowItemHandler(8888, userID, itemID);

					}

				case "MON":
					if (itemID.substring(0, 3).equalsIgnoreCase("MON")) {
						if (userItems.get(itemID) == null || (userItems.get(itemID) != null
								&& userItems.get(itemID).equalsIgnoreCase("Returned"))) {

							// System.out.println("Borrow item for MON");
							String value = MontrealServer.monLibrary.get(itemID);
							String split[] = value.split(",");
							int quantity = Integer.parseInt(split[1]);
							int old_quant = quantity;
							if (quantity > 0) {
								quantity--;
								value = split[0] + "," + quantity;
								MontrealServer.monLibrary.put(itemID, value);
								userItems.put(itemID, "Borrowed");
								saveUserHistoryDetails(userID, userItems);
								logInformationOnServer(userID, itemID, "Success", "BorrowItem", true, "Montreal");
								return ("Success:"+"User " + userID + " has borrowed the item " + itemID
										+ " from Montreal library.\n");
							} else if (quantity == 0) {
								logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, "Montreal");
							//	return ("Failure:"+"Cannot be Borrowed");
								return("Fail:Unavailable");
							}
						} else {
							logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, "Montreal");
							return ("Fail:"+"Item Already borrowed");
						}
					} else if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
						return borrowItemHandler(6666, userID, itemID);
					} else {
						return borrowItemHandler(7777, userID, itemID);

					}
				}
			} else {
				logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, serverName);
				return ("Fail:"+"Item Not Available in " + serverName + " Library");
			}
		} catch (Exception e) {
			System.out.println("Exception caught at BorrowItem");
			e.printStackTrace();

		}
		return null;
	}

	/**
	 * Method the validate whether the book is available in the library.
	 * 
	 * @param userID
	 * @param itemID
	 * @return itemAvailable
	 */
	private boolean validateBookAvailability(String userID, String itemID) {
		boolean itemAvailable = true;
		if (userID.substring(0, 3).equalsIgnoreCase("CON") && itemID.substring(0, 3).equalsIgnoreCase("CON")) {
			itemAvailable = (ConcordiaServer.conLibrary.get(itemID) != null) ? true : false;
		} else if (userID.substring(0, 3).equalsIgnoreCase("MCG") && itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
			itemAvailable = (McgillServer.mcgLibrary.get(itemID) != null) ? true : false;
		} else if (userID.substring(0, 3).equalsIgnoreCase("MON") && itemID.substring(0, 3).equalsIgnoreCase("MON")) {
			itemAvailable = (MontrealServer.monLibrary.get(itemID) != null) ? true : false;
		}

		return itemAvailable;
	}

	/**
	 * Method to log information on the respective server files.
	 * 
	 * @param userID
	 * @param item
	 * @param serverResponse
	 * @param typeOfRequest
	 * @param requestStatus
	 * @param serverName
	 */
	public void logInformationOnServer(String userID, String item, String serverResponse, String typeOfRequest,
			boolean requestStatus, String serverName) {
		String reqStatus = "ERROR: Request Failed";
		Calendar calender = new GregorianCalendar();
		Date dateTime = calender.getTime();
		try {
			if (requestStatus) {
				reqStatus = "Request Completed Successfully";
			}
			FileWriter fw = new FileWriter(System.getProperty("user.dir") + "\\src\\Logs\\" + serverName + "Logs.txt",
					true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.newLine();
			bw.write(dateTime + "\t" + typeOfRequest + "\t" + userID + "\t" + item + "\t" + reqStatus
					+ (requestStatus ? "\t" : "\t\t\t") + serverResponse);
			bw.close();
		} catch (Exception e) {
			System.out.println("Error while writing logs to server " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Method to find item in the library which is performed by the library user.
	 */
	public String findItem(String userID, String itemName) {
		boolean itemFound = false;
		String response = "", conResponse = "", mcgResponse = "", monResponse = "";
		switch (userID.substring(0, 3)) {
		case "CON":
			Iterator<Entry<String, String>> mapIterator = ConcordiaServer.conLibrary.entrySet().iterator();
			while (mapIterator.hasNext()) {
				Entry<String, String> item = mapIterator.next();
				String[] itemDetails = item.getValue().split(",");
				if (itemDetails[0].equalsIgnoreCase(itemName)) {
					conResponse = item.getKey() + " " + itemDetails[1];
					itemFound = true;
					break;
				}
			}
			if (itemFound) {
				logInformationOnServer(userID, itemID, "Success", "FindItem", true, "Concordia");
			} else {
				logInformationOnServer(userID, itemID, "No items", "FindItem", true, "Concordia");
			}

			mcgResponse = findItemHandler(7777, userID, itemName);
			monResponse = findItemHandler(8888, userID, itemName);

			conResponse = conResponse.length() > 0 ? conResponse + ", " : "";
			mcgResponse = mcgResponse.length() > 0 ? mcgResponse + ", " : "";

			response = conResponse + mcgResponse + monResponse;

			System.out.println(response);
			return "Success:"+response;

		case "MCG":
			Iterator<Entry<String, String>> mapIterator1 = McgillServer.mcgLibrary.entrySet().iterator();
			while (mapIterator1.hasNext()) {
				Entry<String, String> item = mapIterator1.next();
				String[] itemDetails = item.getValue().split(",");
				if (itemDetails[0].equalsIgnoreCase(itemName)) {
					mcgResponse = item.getKey() + " " + itemDetails[1];
					itemFound = true;
					break;
				}
			}
			if (itemFound) {
				logInformationOnServer(userID, itemID, "Success", "FindItem", true, "Mcgill");
			} else {
				logInformationOnServer(userID, itemID, "No items", "FindItem", true, "Mcgill");
			}

			conResponse = findItemHandler(6666, userID, itemName);

			monResponse = findItemHandler(8888, userID, itemName);

			conResponse = conResponse.length() > 0 ? conResponse + ", " : "";
			mcgResponse = mcgResponse.length() > 0 ? mcgResponse + ", " : "";

			response = conResponse + mcgResponse + monResponse;

			System.out.println(response);
			return "Success:"+response;

		case "MON":
			Iterator<Entry<String, String>> mapIterator2 = MontrealServer.monLibrary.entrySet().iterator();
			while (mapIterator2.hasNext()) {
				Entry<String, String> item = mapIterator2.next();
				String[] itemDetails = item.getValue().split(",");
				if (itemDetails[0].equalsIgnoreCase(itemName)) {
					monResponse = item.getKey() + " " + itemDetails[1];
					itemFound = true;
					break;
				}

			}
			if (itemFound) {
				logInformationOnServer(userID, itemID, "Success", "FindItem", true, "Montreal");
			} else {
				logInformationOnServer(userID, itemID, "No items", "FindItem", true, "Montreal");
			}
			conResponse = findItemHandler(6666, userID, itemName);
			mcgResponse = findItemHandler(7777, userID, itemName);

			conResponse = conResponse.length() > 0 ? conResponse + ", " : "";
			mcgResponse = mcgResponse.length() > 0 ? mcgResponse + ", " : "";

			response = conResponse + mcgResponse + monResponse;

			System.out.println(response);
			return "Success:"+response;
		}
		return null;
	}

	/**
	 * Method to return item back to the library which is performed by the library
	 * user.
	 */
	public String returnItem(String userID, String itemID) {
		HashMap<String, String> returnItems = new HashMap<String, String>();
		String userServer = (userID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (userID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (userID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		String message = "";
		String logItem = "";
		int val1;
		boolean itemAllocated = false;
		populateWaitList(userID.substring(0, 3));
		populateReturnItemsList(userID, returnItems);

		String itemCode = itemID.substring(0, 3);
		if (returnItems.containsKey(itemID) && returnItems.get(itemID).equalsIgnoreCase("Borrowed")) {
			switch (userID.substring(0, 3)) {

			case "CON":
				if (itemCode.equalsIgnoreCase("MCG")) {
					return returnItemHandler(7777, userID, itemID, returnItems);
				} else if (itemCode.equalsIgnoreCase("MON")) {
					return returnItemHandler(8888, userID, itemID, returnItems);
				} else {
					String value = ConcordiaServer.conLibrary.get(itemID);
					String[] split = value.split(",");
					val1 = Integer.parseInt(split[1]) + 1;

					if (waitList.get(itemID) != null) {
						itemAllocated = allocateItem(userID, itemID, "CON");
						if (itemAllocated) {
							val1 = val1 - 1;
						}
					}
					ConcordiaServer.conLibrary.put(itemID, split[0] + "," + val1);
					returnItems.put(itemID, "Returned");
					saveUserHistoryDetails(userID, returnItems);
					logInformationOnServer(userID, itemID, "Success", "ReturnItem", true, "Concordia");
					return "Success:"+"Item " + itemID + " returned by user " + userID + " successfully!";
				}

			case "MCG":
				if (itemCode.equalsIgnoreCase("CON")) {
					return returnItemHandler(6666, userID, itemID, returnItems);
				} else if (itemCode.equalsIgnoreCase("MON")) {
					return returnItemHandler(8888, userID, itemID, returnItems);
				} else {
					String value = McgillServer.mcgLibrary.get(itemID);
					String[] split = value.split(",");
					val1 = Integer.parseInt(split[1]) + 1;
					if (waitList.get(itemID) != null) {
						itemAllocated = allocateItem(userID, itemID, "MCG");
						if (itemAllocated) {
							val1 = val1 - 1;
						}
					}
					McgillServer.mcgLibrary.put(itemID, split[0] + "," + val1);
					returnItems.put(itemID, "Returned");
					saveUserHistoryDetails(userID, returnItems);
					logInformationOnServer(userID, itemID, "Success", "ReturnItem", true, "Mcgill");
					return "Success:"+"Item " + itemID + " returned by user " + userID + " successfully!";
				}

			case "MON":
				if (itemCode.equalsIgnoreCase("CON")) {
					return returnItemHandler(6666, userID, itemID, returnItems);
				} else if (itemCode.equalsIgnoreCase("MCG")) {
					return returnItemHandler(7777, userID, itemID, returnItems);
				} else {
					String value = MontrealServer.monLibrary.get(itemID);
					String[] split = value.split(",");
					val1 = Integer.parseInt(split[1]) + 1;
					if (waitList.get(itemID) != null) {
						itemAllocated = allocateItem(userID, itemID, "MON");
						if (itemAllocated) {
							val1 = val1 - 1;
						}
					}
					MontrealServer.monLibrary.put(itemID, split[0] + "," + val1);
					returnItems.put(itemID, "Returned");
					saveUserHistoryDetails(userID, returnItems);
					logInformationOnServer(userID, itemID, "Success", "ReturnItem", true, "Montreal");
					return "Success:"+"Item " + itemID + " returned by user " + userID + " successfully!";
				}

			}
			returnItems.put(itemID, "Returned");

		} else {
			logInformationOnServer(userID, itemID, "Failure", "ReturnItem", false, userServer);
			message = "Fail:"+"Return not possible.";
		}
		return message;
	}

	/**
	 * Populating the userItems hashmap
	 * 
	 * @param userID
	 * @param returnItems
	 * @throws Exception
	 */
	public void populateReturnItemsList(String userID, HashMap<String, String> returnItems) {
		List<String> userHistoryList = new ArrayList<String>();
		if (userID.substring(0, 3).equalsIgnoreCase("CON")) {

			userHistoryList.addAll(ConcordiaServer.userHistory.get(userID));
		} else if (userID.substring(0, 3).equalsIgnoreCase("MCG")) {
			userHistoryList.addAll(McgillServer.userHistory.get(userID));
		} else if (userID.substring(0, 3).equalsIgnoreCase("MON")) {
			userHistoryList.addAll(MontrealServer.userHistory.get(userID));
		}
		for (String userHistory : userHistoryList) {
			String[] itemStatus = userHistory.split(",");
			returnItems.put(itemStatus[0], itemStatus[1]);
		}
	}

	/**
	 * Method to allocate item to the user who is the waitlist.
	 * 
	 * @param userID
	 * @param itemID
	 * @param server
	 * @return itemAllocated
	 */
	private boolean allocateItem(String userID, String itemID, String server) {
		boolean itemAllocated = false;
		try {

			String allocatedUser = "";
			if (!itemID.substring(0, 3).equalsIgnoreCase(server)) {
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

				String itemDetails = waitList.get(itemID);
				List<String> userIDs = Arrays.asList(itemDetails.split(","));

				for (String user : userIDs) {
					String reqMsg = "AllocateItem" + "," + user + "," + itemID;
					String userIDType = user.substring(0, 3);
					if (userIDType.equalsIgnoreCase(server)) {
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
					} else if (userIDType.equalsIgnoreCase("CON")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(6666, reqMsg));
					} else if (userIDType.equalsIgnoreCase("MCG")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(7777, reqMsg));
					} else if (userIDType.equalsIgnoreCase("Mon")) {
						itemAllocated = Boolean.parseBoolean(sendMessage(8888, reqMsg));
					}
					if (itemAllocated) {
						allocatedUser = user;
						break;
					}
				}
			}
			if (itemAllocated) {
				removeUserFromWaitList(allocatedUser, itemID);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return itemAllocated;
	}

	/**
	 * Method to remove user from the waitlist.
	 * 
	 * @param allocatedUser
	 * @param itemID
	 */
	private void removeUserFromWaitList(String allocatedUser, String itemID) {
		String userIDs = waitList.get(itemID);
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
		if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
			ConcordiaServer.conWaitlist.put(itemID, updatedUserList);
		} else if (itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
			McgillServer.mcgWaitlist.put(itemID, updatedUserList);
		} else if (itemID.substring(0, 3).equalsIgnoreCase("MON")) {
			MontrealServer.monWaitlist.put(itemID, updatedUserList);
		}
	}

	/**
	 * Method to populate the waitlist.
	 * 
	 * @param server
	 */
	private void populateWaitList(String server) {
		waitList = new HashMap<String, String>();
		if (server.equalsIgnoreCase("CON")) {
			waitList.putAll(ConcordiaServer.conWaitlist);
		} else if (server.equalsIgnoreCase("MCG")) {
			waitList.putAll(McgillServer.mcgWaitlist);
		} else if (server.equalsIgnoreCase("MON")) {
			waitList.putAll(MontrealServer.monWaitlist);
		}
	}

	public static String sendMessage(int serverPort, String msg) {
		DatagramSocket aSocket = null;
		String replyMessage = null;
		byte[] message = null;
		try {
			aSocket = new DatagramSocket();
			Charset charsetToSend = Charset.forName("UTF-8");
			message = msg.getBytes(charsetToSend);
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, msg.length(), aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from user server to server with port number " + serverPort
					+ " is: " + new String(request.getData()));
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			System.out.println("Reply received from the server with port number " + serverPort + " is: "
					+ data(reply.getData()).toString());
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

	public String borrowItemHandler(int port, String userID, String itemID) {
		String borrowResponse = "";
		String reqMsg = "Borrow" + "," + userID + "," + itemID;
		String serverName = (port == 6666 ? "Concordia" : (port == 7777 ? "Mcgill" : (port == 8888 ? "Montreal" : "")));
		System.out.println(reqMsg);
		if (!(borrowItemFromOtherLibrary(itemID))) {
			logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, serverName);
			return "Fail:"+"Cannot borrow more books from this library.";
		}
		borrowResponse = sendMessage(port, reqMsg);
		if (borrowResponse.equalsIgnoreCase("Success")) {
			userItems.put(itemID, "Borrowed");
			saveUserHistoryDetails(userID, userItems);
			logInformationOnServer(userID, itemID, "Success", "BorrowItem", true, serverName);
			return "Success:"+"Item " + itemID + " borrowed by user " + userID + " successfully!";
		} else if (borrowResponse.equalsIgnoreCase("Cannot be Borrowed")) {
			logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, serverName);
			return "Fail:"+borrowResponse;
		} else {
			logInformationOnServer(userID, itemID, "Failure", "BorrowItem", false, serverName);
			return "Fail:"+"Error/No response";
		}

	}

	public boolean borrowItemFromOtherLibrary(String itemID) {
		boolean borrowAllowed = true;
		String itemLibrary = itemID.substring(0, 3);
		Iterator<Entry<String, String>> userItemsIt = userItems.entrySet().iterator();
		while (userItemsIt.hasNext()) {
			Entry<String, String> userItem = userItemsIt.next();
			if (userItem.getKey().substring(0, 3).equalsIgnoreCase(itemLibrary)
					&& userItem.getValue().equalsIgnoreCase("Borrowed")) {
				borrowAllowed = false;
			}
		}
		return borrowAllowed;
	}

	public String findItemHandler(int port, String userID, String itemName) {
		String findResponse = "";
		String serverName = (port == 6666 ? "Concordia" : (port == 7777 ? "Mcgill" : (port == 8888 ? "Montreal" : "")));
		String reqMsg = "FindItem" + "," + userID + "," + itemName;
		findResponse = sendMessage(port, reqMsg);
		if (findResponse != null && findResponse.length() > 0) {
			logInformationOnServer(userID, itemID, "Success", "FindItem", true, serverName);
			return findResponse;
		} else {
			logInformationOnServer(userID, itemID, "No items", "FindItem", true, serverName);
			return "";
		}
	}

	public String returnItemHandler(int port, String userID, String itemID, HashMap<String, String> returnItems) {
		String returnResponse = "";
		String serverName = (port == 6666 ? "Concordia" : (port == 7777 ? "Mcgill" : (port == 8888 ? "Montreal" : "")));
		String reqMsg = "ReturnItem" + "," + userID + "," + itemID;
		returnResponse = sendMessage(port, reqMsg);
		if (returnResponse.equalsIgnoreCase("Success")) {
			returnItems.put(itemID, "Returned");
			saveUserHistoryDetails(userID, returnItems);
			logInformationOnServer(userID, itemID, "Success", "ReturnItem", true, serverName);
			return "Success:"+"Item " + itemID + " returned by user " + userID + " successfully!";
		} else {
			logInformationOnServer(userID, itemID, "Failure", "ReturnItem", false, serverName);
			return "Fail:"+"Error/No response";
		}
	}

	public String addItems(String itemID, String itemName, int quantity, String location) throws Exception {
		FileWriter writer = new FileWriter(location, true);
		BufferedWriter bw = new BufferedWriter(writer);
		bw.newLine();

		return "";

	}

	public String waitList(String userID, String itemID, int numberOfDays) {
		// TODO Auto-generated method stub
		String returnMsg = "";
		String userServer = (userID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (userID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (userID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));

		populateWaitList(userID.substring(0, 3));

		if (validateForUserWaitList(userID, itemID)) {
			return "Fail:Already Added";
		} else {
			switch (userID.substring(0, 3)) {
			case "CON":
				if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
					returnMsg = ConcordiaServer.waitList(userID, itemID);
					if (returnMsg.equalsIgnoreCase("Success")) {
						logInformationOnServer(userID, itemID, "Success", "WaitListItem", true, userServer);
						returnMsg = "Success:"+userID + " added to the waitlist successfully!";
					}
				} else if (itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
					returnMsg = waitListHandler(7777, userID, itemID);
				} else {
					returnMsg = waitListHandler(8888, userID, itemID);
				}
				return returnMsg;

			case "MCG":
				if (itemID.substring(0, 3).equalsIgnoreCase("MCG")) {
					returnMsg = McgillServer.waitList(userID, itemID);
					if (returnMsg.equalsIgnoreCase("Success")) {
						returnMsg = "Success:"+userID + " added to the waitlist successfully!";
						logInformationOnServer(userID, itemID, "Success", "WaitListItem", true, userServer);
					}
				} else if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
					returnMsg = waitListHandler(6666, userID, itemID);
				} else {
					returnMsg = waitListHandler(8888, userID, itemID);
				}
				return returnMsg;

			case "MON":
				if (itemID.substring(0, 3).equalsIgnoreCase("MON")) {
					returnMsg = MontrealServer.waitList(userID, itemID);
					if (returnMsg.equalsIgnoreCase("Success")) {
						returnMsg = "Success:"+userID + " added to the waitlist successfully!";
						logInformationOnServer(userID, itemID, "Success", "WaitListItem", true, userServer);
					}
				} else if (itemID.substring(0, 3).equalsIgnoreCase("CON")) {
					returnMsg = waitListHandler(6666, userID, itemID);
				} else {
					returnMsg = waitListHandler(7777, userID, itemID);
				}
				return returnMsg;
			}
		}
		return returnMsg;
	}

	private boolean validateForUserWaitList(String userID, String itemID) {
		// TODO Auto-generated method stub
		boolean isUserInWaitList = false;
		String value = waitList.get(itemID);
		if (value != null) {
			List<String> userList = Arrays.asList(value.split(","));
			if (userList.contains(userID)) {
				isUserInWaitList = true;
			}
		}
		return isUserInWaitList;
	}

	public String waitListHandler(int port, String userID, String itemID) {
		String borrowResponse = "";
		String serverName = (port == 6666 ? "Concordia" : (port == 7777 ? "Mcgill" : (port == 8888 ? "Montreal" : "")));
		String reqMsg = "WaitList" + "," + userID + "," + itemID;
		borrowResponse = sendMessage(port, reqMsg);
		if (borrowResponse.equalsIgnoreCase("Success")) {
			// addUserToWaitList(userID,itemID,serverName);
			logInformationOnServer(userID, itemID, "Success", "WaitListItem", true, serverName);
			return "Success:"+userID + " added to the waitlist successfully!";
		} else {
			logInformationOnServer(userID, itemID, "Failure", "WaitListItem", true, serverName);
			return "Fail:"+borrowResponse;
		}
	}

	public void saveUserHistoryDetails(String userID, HashMap<String, String> returnItems) {
		List<String> userHistoryList = new ArrayList<String>();
		Iterator<Entry<String, String>> returnItemsIT = returnItems.entrySet().iterator();

		while (returnItemsIT.hasNext()) {
			Entry<String, String> historyItem = returnItemsIT.next();
			userHistoryList.add(historyItem.getKey() + "," + historyItem.getValue());
		}

		if (userID.substring(0, 3).equalsIgnoreCase("CON")) {
			ConcordiaServer.userHistory.put(userID, userHistoryList);
		} else if (userID.substring(0, 3).equalsIgnoreCase("MCG")) {
			McgillServer.userHistory.put(userID, userHistoryList);
		} else if (userID.substring(0, 3).equalsIgnoreCase("MON")) {
			MontrealServer.userHistory.put(userID, userHistoryList);
		}

	}

//	public void setOrb(ORB orb) {
//		this.orb = orb;
//	}

	public String exchangeItem(String userID, String newItemID, String oldItemID) {

		userItems = new HashMap<String, String>();
		String userServer = (userID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (userID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (userID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		String message = "";
		boolean isExchangeItemAvailable = false;
		populateReturnItemsList(userID, userItems);

		if (!userItems.containsKey(oldItemID)
				|| (userItems.containsKey(oldItemID) && userItems.get(oldItemID).equalsIgnoreCase("Borrowed"))) {

			if ((userItems.containsKey(newItemID) && !userItems.get(newItemID).equalsIgnoreCase("Borrowed"))
					|| !userItems.containsKey(newItemID)) {

				isExchangeItemAvailable = findExchangeItemAvailability(userID, newItemID);

				if (isExchangeItemAvailable) {
					if (!userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))
							&& (!borrowItemFromOtherLibrary(newItemID)
									&& !(oldItemID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))))) {
						logInformationOnServer(userID, oldItemID, "Failure", "ExchangeItem", false, userServer);
						return "Fail:Cannot borrow more books from this library.";
					} else {
						message = returnItem(userID, oldItemID);
						message += "\n" + borrowItem(userID, newItemID, numberOfDays);
						message = "Success:" + message;
						logInformationOnServer(userID, oldItemID, "Success", "ExchangeItem :" + newItemID, false,
								userServer);
					}
				} else {
					logInformationOnServer(userID, oldItemID, "Failure", "ExchangeItem", false, userServer);
					message = "Fail:"+"New Item is not available in Library";
				}
			} else {
				logInformationOnServer(userID, oldItemID, "Failure", "ExchangeItem", false, userServer);
				message = "Fail:"+"New Item is already borrowed";
			}
		} else {
			logInformationOnServer(userID, oldItemID, "Failure", "ExchangeItem", false, userServer);
			message = "Fail:"+"Old Item not available with user for exchange";
		}

		return message;
	}

	private boolean findExchangeItemAvailability(String userID, String newItemID) {
		boolean itemFound = false;
		String serverName = (userID.substring(0, 3).equalsIgnoreCase("CON") ? "Concordia"
				: (userID.substring(0, 3).equalsIgnoreCase("MCG") ? "Mcgill"
						: (userID.substring(0, 3).equalsIgnoreCase("MON") ? "Montreal" : "")));
		int port = newItemID.substring(0, 3).equalsIgnoreCase("CON") ? 6666
				: (newItemID.substring(0, 3).equalsIgnoreCase("MCG") ? 7777
						: (newItemID.substring(0, 3).equalsIgnoreCase("MON") ? 8888 : 0));
		String reqMsg = "FindExchangeItem" + "," + userID + "," + newItemID;

		if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
			itemFound = findExchangeItemInUserLibrary(serverName.substring(0, 3), newItemID);
		} else {
			// Find item in other libraries via UDP.
			itemFound = Boolean.valueOf(sendMessage(port, reqMsg)).booleanValue();
		}
		return itemFound;
	}

	private boolean findExchangeItemInUserLibrary(String serverName, String newItemID) {
		int itemCount = 0;
		if (serverName.equalsIgnoreCase("CON")) {
			itemCount = Integer.parseInt(ConcordiaServer.conLibrary.get(newItemID) != null
					? ConcordiaServer.conLibrary.get(newItemID).split(",")[1]
					: "0");
		} else if (serverName.equalsIgnoreCase("MCG")) {
			itemCount = Integer.parseInt(McgillServer.mcgLibrary.get(newItemID) != null
					? McgillServer.mcgLibrary.get(newItemID).split(",")[1]
					: "0");
		} else if (serverName.equalsIgnoreCase("MON")) {
			itemCount = Integer.parseInt(MontrealServer.monLibrary.get(newItemID) != null
					? MontrealServer.monLibrary.get(newItemID).split(",")[1]
					: "0");
		}
		if (itemCount > 0) {
			return true;
		}
		return false;
	}
	
//	@Override
//	public boolean validateUser(String userID) {
//		boolean flag = false;
//		switch (userID.substring(0, 3)) {
//		case "CON":
//			if (userID.charAt(3) == 'U') {
//				if (ConcordiaServer.userlist.containsKey(userID))
//					flag = true;
//			} else {
//				if (ConcordiaServer.managerUserList.contains(userID)) {
//					flag = true;
//				}
//			}
//			break;
//		case "MON":
//			if (userID.charAt(3) == 'U') {
//				if (MontrealServer.userlist.containsKey(userID))
//					flag = true;
//			} else {
//				if (MontrealServer.managerUserList.contains(userID)) {
//					flag = true;
//				}
//			}
//			break;
//
//		case "MCG":
//			if (userID.charAt(3) == 'U') {
//				if (McgillServer.userlist.containsKey(userID))
//					flag = true;
//			} else {
//				if (McgillServer.managerUserList.contains(userID)) {
//					flag = true;
//				}
//			}
//			break;
//		}
//		return flag;
//	}

}
