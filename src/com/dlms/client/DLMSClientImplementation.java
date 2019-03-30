package com.dlms.client;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import ActionServiceApp.ActionService;
import ActionServiceApp.ActionServiceHelper;

public class DLMSClientImplementation {

	String userID;
	String managerID;
	ActionService conStub;
	ActionService mcstub;
	ActionService montStub;
	
	String result;

	String initialID;
	String id;
	String choice;
	String role;
	Logger logger = Logger.getLogger(DLMSClientImplementation.class.getName());

	public DLMSClientImplementation(String args[]) {

		try {
			
			ORB orb = ORB.init(args, null);
			// -ORBInitialPort 1050 -ORBInitialHost localhost
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			conStub = (ActionService) ActionServiceHelper.narrow(ncRef.resolve_str("ActionService"));
			mcstub = (ActionService) ActionServiceHelper.narrow(ncRef.resolve_str("ActionService"));
			montStub = (ActionService) ActionServiceHelper.narrow(ncRef.resolve_str("ActionService"));
			

		}

		catch (Exception e) {
			e.printStackTrace();

		}

	}

	public enum LibraryName {
		CON, MCG, MON;

	}

	Scanner sc = new Scanner(System.in);
	

	public void createLoggingFile(String id) {
		File file = null;
		FileHandler handler = null;
		LogManager.getLogManager().reset();

		if (id.charAt(3) == 'U') {
			file = new File("C:\\Users\\Arpit\\Desktop\\Distributed system\\User\\logs");
		} else {
			file = new File("C:\\Users\\Arpit\\Desktop\\Distributed system\\Manager\\logs");
		}

		if (!file.exists()) {
			file.mkdirs();
		}

		// File f=new File(file, id + ".txt");
		File f = new File(file.getAbsolutePath().toString() + "\\" + id + ".txt");
		try {
			if (!f.exists()) {
				f.createNewFile();
			}

			handler = new FileHandler(f.getAbsolutePath(), true);

			handler.setFormatter(new SimpleFormatter());
			handler.setLevel(Level.INFO);
			logger.addHandler(handler);
		} catch (SecurityException e) {

			e.printStackTrace();
		}
		catch (IOException i)
		{
			i.printStackTrace();
		}

	}

	public void libraryHome() throws IOException {

		System.out.print("Welcome to Library\n");
		System.out.println("Enter your ID\n");

		id = sc.nextLine();
		id = id.toUpperCase();
		role = verifyID(id);

		if (role.equalsIgnoreCase("manager")) {
			initialID = id.substring(0, 3);
			createLoggingFile(id);
			managerOperations(id.toUpperCase());
		} else if (role.equalsIgnoreCase("user")) {
			initialID = id.substring(0, 3);
			createLoggingFile(id);
			userOperations(id.toUpperCase());
		} else {

			System.out.println("Enter a valid ID");
			id = null;
			System.out.println(
					"Note: ID should me made of your library prefix i.e CON, MCG or MON followed by U or M for user or Manager respectively followed by four digit unique number.");
			libraryHome();
		}

	}

	// verifies ID entered ....To be done using regex

	public String verifyID(String id) throws IOException {
		if (id == null) {
			System.out.println(
					"ID cannot be null. It should me made of your library prefix i.e CON, MCG or MON followed by U or M for user or Manager respectively followed by four digit unique number.");
			libraryHome();
		}

		choice = id;
		choice = choice.toUpperCase();
		Pattern regex = Pattern.compile("(CON|MCG|MON)([UM])[0-9]{4}");
		Matcher match = regex.matcher(choice);

		if (match.matches()) {
			char ch = id.charAt(3);

			if (ch == 'U') {
				result = "user";
			} else {
				result = "manager";
			}

		} else {
			result = "invalid user";
		}
		return result;
	}

	// All manager operations are handled in this method

	public void managerOperations(String id) {
		String itemID;
		System.out.print("Welcome to Manager Portal\n");

		logger.info("Manager ID: " + id + " Manager is allowed to perform operations in this library");

		System.out.println("Enter your Preferred choice\n");
		System.out.println("1. Add a new Item in the Library" + "\n" + "2. Remove an Item in the Library" + "\n"
				+ "3. List of all available Items in the Library" + "\n");

		int preferredChoice = sc.nextInt();
		sc.nextLine();
		switch (preferredChoice) {

		case 1:
			System.out.println("Enter Item ID:  ");
			logger.info("Manager ID: " + id + " Manager has chosen to add a new item in the library");
			itemID = validateItemid(id);
			logger.info("Manager ID: " + id + " Manager has entered item ID: " + itemID);
			System.out.println("Enter number of Items:  ");
			int noOfItems = validateNumber();
			logger.info("Manager ID: " + id + " Manager has entered quantity of item: " + noOfItems);

			sc.nextLine();
			// System.out.println("Enter Item Name: ");
			String name = validateItemName();
			logger.info("Manager ID: " + id + " Manager has entered item Name: " + name);

			addnewItem(id, itemID.toUpperCase(), name.toUpperCase(), noOfItems);
			break;

		case 2:
			System.out.println("Enter Item ID you wish to be removed\n");
			logger.info("Manager ID: " + id + " Manager has chosen to remove an item from the library.");
			itemID = validateItemid(id);
			logger.info("Manager ID: " + id + " Manager has entered item ID: " + itemID);
			System.out.println(
					"Enter quantities of item to be removed. \nNote: If you wish to completely remove the Item, Enter 0 otherwise enter any number\n");
			noOfItems = validateNumber();
			logger.info("Manager ID: " + id + " Manager has entered quantity of item to remove: " + noOfItems);
			removeAnyItem(id, itemID.toUpperCase(), noOfItems);
			break;

		case 3:
			logger.info("Manager ID: " + id + " Manager has chosen to list all the available items in the library.");
			listAnyItemAvailability(id);
			break;

		default:
			logger.info("Manager ID: " + id + " Manager has not chosen a valid option./nNeed to try again.");
			System.out.println("Invalid Entry\n");
			managerOperations(id);
			break;

		}

	}

	public void listAnyItemAvailability(String id) {

		try {
			if (initialID.equalsIgnoreCase("CON")) {

				result = conStub.listItemAvailability(id);
				System.out.println(result);
				logger.info("Available items in Concordia Library:" + "\n" + result);
			} else if (initialID.equalsIgnoreCase("MCG")) {

				result = mcstub.listItemAvailability(id);
				System.out.println(result);
				logger.info("Available items in McGill Library:" + "\n" + result);

			}

			else {

				result = montStub.listItemAvailability(id);
				System.out.println(result);
				logger.info("Available items in Montreal Library:" + "\n" + result);
			}

			String s = validateChoice();
			logger.info("Manager ID: " + id + " Manager has been asked whether to continue operations or exit.");
			if (s.equalsIgnoreCase("Y")) {
				logger.info("Manager ID: " + id + " Manager entered Y and chose to continue");
				managerOperations(id);
			} else {
				logger.info("Manager ID: " + id + " Manager entered N and chose to exit.");

				sc.nextLine();
				libraryHome();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void removeAnyItem(String id, String itemID, int noOfItems) {
		try {
			if (initialID.equalsIgnoreCase("CON")) {
				result = conStub.removeItem(id, itemID, noOfItems);
				System.out.println(result + "\n");
				logger.info("Result: " + result);

			} else if (initialID.equalsIgnoreCase("MCG")) {

				result = mcstub.removeItem(id, itemID, noOfItems);
				System.out.println(result + "\n");
				logger.info("Result: " + result);
			}

			else {

				result = montStub.removeItem(id, itemID, noOfItems);
				System.out.println(result + "\n");
				logger.info("Result: " + result);
			}
			String s = validateChoice();
			logger.info("Manager ID: " + id + " Manager has been asked whether to continue operations or exit.");
			if (s.equalsIgnoreCase("Y")) {
				logger.info("Manager ID: " + id + " Manager entered Y and chose to continue");
				managerOperations(id);

			} else {
				logger.info("Manager ID: " + id + " Manager entered N and chose to exit Library");
				sc.nextLine();
				libraryHome();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addnewItem(String id, String itemID, String name, int noOfItems) {

		try {
			if (initialID.equalsIgnoreCase("CON")) {
				result = conStub.addItem(id, itemID, name, noOfItems);
				logger.info("Result of adding a new item in concordia library: " + result);
				System.out.println(result + "\n");
			} else if (initialID.equalsIgnoreCase("MCG")) {

				result = mcstub.addItem(id, itemID, name, noOfItems);
				System.out.println(result + "\n");
				logger.info("Result of adding a new item in McGill Library: " + result);
			}

			else {

				result = montStub.addItem(id, itemID, name, noOfItems);
				System.out.println(result + "\n");
				logger.info("Result of adding a new item in Montreal Library: " + result);
			}

			String s = validateChoice();
			logger.info("Manager ID: " + id + " Manager has been asked whether to continue operations or exit.");
			if (s.equalsIgnoreCase("Y")) {
				logger.info("Manager ID: " + id + " Manager entered Y and chose to continue");

				managerOperations(id);
			} else {
				logger.info("Manager ID: " + id + " Manager entered N and chose to exit");

				sc.nextLine();
				libraryHome();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// All user operations are handled in this method

	public void userOperations(String id) throws IOException {

		System.out.println("Welcome to User Portal\n");
		
		logger.info("User ID: " + id + " User is allowed to perform operations in this library");
		System.out.println("Enter your Preferred choice\n");
		System.out.println(
				"1. Borrow a new Item" + "\n" + "2. Find any Item" + "\n" + "3. Return a borrowed Item" + "\n" + "4. Exchange any Item");

		int preferredChoice = sc.nextInt();
		sc.nextLine();

		String itemName;
		String itemID;
		String oldItemID;
		String newItemID;

		switch (preferredChoice) {
		case 1:
			logger.info("User ID: " + id + " User has chosen to borrow a new item in this library");
			System.out.println("Enter Item ID:  ");
			itemID = validateItemid();
			System.out.println("Enter no of days:  ");
			int days = validateNumber();
			sc.nextLine();
			borrowNewItem(id, itemID.toUpperCase(), days);
			break;

		case 3:
			logger.info("User ID: " + id + " User has chosen to find an item in this library");

			System.out.println("Enter Item ID\n");
			itemID = validateItemid();
			returnAnyItem(id, itemID.toUpperCase());
			break;

		case 2: // System.out.println("Enter Item Name");
			
			logger.info("User ID: " + id + " User has chosen to return previously borrowed item from this library");

			itemName = validateItemName();

			findAnyItem(id, itemName.toUpperCase());
			break;
			
		case 4:
			logger.info("User ID: " + id + " User has chosen to exchange previously borrowed item from this library");
			System.out.println("Enter old Item ID:  ");
			oldItemID = validateItemid();
			System.out.println("Enter new Item ID:  ");
			newItemID = validateItemid();
			
			exchangeItem(id, newItemID, oldItemID);
			
			

		default:
			System.out.println("Invalid Entry");
			logger.info("User ID: " + id + " User has not chosen a valid option./nNeed to try again.");
			userOperations(id);
			break;

		}
	}

	public void exchangeItem(String userID, String newItemID, String oldItemID) {
		
		try {
			if (initialID.equalsIgnoreCase("CON")) {
				logger.info("Sending request to user library: Concordia Library ");
				result = conStub.exchangeItem(userID, newItemID, oldItemID);
				
				logger.info("Reply recieved from Concordia Library");
				
			} else if (initialID.equalsIgnoreCase("MCG")) {
				
				logger.info("Sending request to user library: Mcgill Library ");
				result = mcstub.exchangeItem(userID, newItemID, oldItemID);
				logger.info("Reply recieved from Mcgill Library");

			}

			else {
				
				logger.info("Sending request to user library: Montreal Library ");
				result = montStub.exchangeItem(userID, newItemID, oldItemID);
				logger.info("Reply recieved from Montreal Library");

			}
			System.out.println(result);
			
			String s = validateChoice();
			if (s.equalsIgnoreCase("Y")) {
				logger.info("User ID: " + userID + " User entered Y and chose to continue");
				userOperations(userID);
			} else {
				sc.nextLine();
				logger.info("User ID: " + userID + " User entered N and chose to exit");
				libraryHome();
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
				
		
	}

	// This method calls library server implementation to borrow a new item id

	public void borrowNewItem(String userId, String itemID, int days) {

		String response;

		try {
			if (initialID.equalsIgnoreCase("CON")) {
				logger.info("Sending request to user library: Concordia Library ");
				result = conStub.borrowItem(id, itemID, days);
				
				logger.info("Reply recieved from Concordia Library");
				
			} else if (initialID.equalsIgnoreCase("MCG")) {
				
				logger.info("Sending request to user library: Mcgill Library ");
				result = mcstub.borrowItem(id, itemID, days);
				logger.info("Reply recieved from Mcgill Library");

			}

			else {
				
				logger.info("Sending request to user library: Montreal Library ");
				result = montStub.borrowItem(id, itemID, days);
				logger.info("Reply recieved from Montreal Library");

			}
			logger.info("Result is: " + result);

			if (result.equalsIgnoreCase("waitingqueue"))

			{
				logger.info("Asking user If he wants to be added in the waiting queue. ");
				String initialItemID = itemID.substring(0, 3);

				String s = validateWaitChoice();
				if (s.equalsIgnoreCase("Y") & initialItemID.equalsIgnoreCase("CON")) {
					
					logger.info("User has chosen to be added in the waiting queue. ");
					result = conStub.waitList(userId, itemID, 0);
					logger.info("User added in the waiting queue. ");

				} else if (s.equalsIgnoreCase("Y") & initialItemID.equalsIgnoreCase("MCG")) {
					
					logger.info("User has chosen to be added in the waiting queue. ");
					result = mcstub.waitList(userId, itemID, 0);
					logger.info("User added in the waiting queue. ");

				} else if (s.equalsIgnoreCase("Y") & initialItemID.equalsIgnoreCase("MON")) {
					
					logger.info("User has chosen to be added in the waiting queue. ");
					result = montStub.waitList(userId, itemID,0);
					logger.info("User added in the waiting queue. ");
					
				} else if (s.equalsIgnoreCase("N")) {
					
					logger.info("User has chosen not to be added in the waiting queue. ");
					result = "Thanks for your time. ";
				}

			}

			if (result != "waitingqueue") {
				System.out.println(result);
			}
			
			logger.info("User ID: " + userID + " User has been asked whether to continue operations or exit.");
			String s = validateChoice();
			if (s.equalsIgnoreCase("Y")) {
				logger.info("User ID: " + userID + " User entered Y and chose to continue");
				userOperations(userId);
			} else {
				sc.nextLine();
				logger.info("User ID: " + id + " User entered N and chose to exit");
				libraryHome();
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	// This method calls library server implementation to find any item using its
	// name

	public void findAnyItem(String userID, String itemName) {

		try {

			if (initialID.equalsIgnoreCase("CON")) {
				logger.info("Sending request to user library: Concordia Library ");
				result = conStub.findItem(userID, itemName);
				logger.info("Reply recieved from Concordia Library");
				
				System.out.println("The Result is: " + result + "\n");
			} else if (initialID.equalsIgnoreCase("MCG")) {
				
				logger.info("Sending request to user library: Mcgill Library ");
				result = mcstub.findItem(userID, itemName);
				logger.info("Reply recieved from Mcgill Library");
				
				System.out.println("The result is: " + result + "\n");

			}

			else {
				
				logger.info("Sending request to user library: Montreal Library ");
				result = montStub.findItem(userID, itemName);
				logger.info("Reply recieved from Montreal Library");
				
				System.out.println("The result is: " + result + "\n");
			}
			logger.info("The result is: " + result);
			
			logger.info("User ID: " + userID + " User has been asked whether to continue operations or exit.");

			String s = validateChoice();
			if (s.equalsIgnoreCase("Y")) {
				
				logger.info("User ID: " + userID + " User entered Y and chose to continue");
				userOperations(userID);
			} else {
				sc.nextLine();
				logger.info("User ID: " + userID + " User entered N and chose to exit");
				
				libraryHome();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// This method calls library server implementation to return any item using an
	// Item ID
	public void returnAnyItem(String userID, String itemID) {

		try {
			if (initialID.equalsIgnoreCase("CON")) {
				
				logger.info("Sending request to user library: Concordia Library ");
				result = conStub.returnItem(userID, itemID);
				logger.info("Reply recieved from Mcgill Library");
				
				System.out.println(result + "\n");
			} else if (initialID.equalsIgnoreCase("MCG")) {
				
				logger.info("Sending request to user library: Mcgill Library ");
				result = mcstub.returnItem(userID, itemID);
				logger.info("Reply recieved from Mcgill Library");
				
				System.out.println(result + "\n");

			}

			else {
				
				logger.info("Sending request to user library: Montreal Library ");
				result = montStub.returnItem(userID, itemID);
				logger.info("Reply recieved from Mcgill Library");
				
				System.out.println(result + "\n");
			}
			
			logger.info("Result is: " + result);
			
			logger.info("User ID: " + userID + " User has been asked whether to continue operations or exit.");

			
			String s = validateChoice();
			if (s.equalsIgnoreCase("Y")) {
				logger.info("User ID: " + userID + " User entered Y and chose to continue");
				userOperations(userID);
			} else {
				sc.nextLine();
				logger.info("User ID: " + userID + " User entered N and chose to exit");
				libraryHome();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String validateItemid(String id) {
		String choice = sc.nextLine().toUpperCase();
		id = id.toUpperCase();
		Pattern r;
		String s;
		if (id.substring(0, 3).equalsIgnoreCase("CON")) {
			r = Pattern.compile("(CON)[0-9]{4}");
		} else if (id.substring(0, 3).equalsIgnoreCase("MCG")) {
			r = Pattern.compile("(MCG)[0-9]{4}");
		} else {
			r = Pattern.compile("(MON)[0-9]{4}");
		}
		Matcher M = r.matcher(choice);

		if (M.matches()) {
			return choice;
		} else {
			System.out.println(
					"Enter Valid ID. Item ID must consist of initial three letters of your user ID(CON/MCG/MON)  followed by unique four digits(0-9).");
			System.out.println("Enter it again");
			s = validateItemid(id);
		}
		return s;

	}

	public String validateItemid() {
		String choice = sc.nextLine().toUpperCase();

		Pattern r;
		String s;

		r = Pattern.compile("(MON|CON|MCG)[0-9]{4}");

		Matcher M = r.matcher(choice);

		if (M.matches()) {
			return choice;
		} else {
			System.out.println(
					"Enter Valid ID. Item ID must consist of initial three letters of (CON/MCG/MON) any library followed by unique four digits(0-9).");
			System.out.println("Enter it again");
			s = validateItemid();
		}
		return s;

	}

	public String validateChoice() {
		System.out.println("Do you want to Continue. Press Y/N to continue or end");
		String choice = sc.next().toUpperCase();
		Pattern regex = Pattern.compile("(Y|N)");
		Matcher match = regex.matcher(choice);
		String s;

		if (match.matches()) {
			return choice;
		} else {
			System.out.println("Enter valid choice");
			s = validateChoice();
		}
		return s;
	}

	public String validateWaitChoice() {
		System.out.println("Item not available. Do you want to be added in waiting queue. Press Y/N");
		String choice = sc.next().toUpperCase();
		Pattern regex = Pattern.compile("(Y|N)");
		Matcher match = regex.matcher(choice);
		String a;

		if (match.matches()) {
			return choice;
		} else {
			System.out.println("Enter valid choice");
			a = validateChoice();
		}
		return a;
	}

	public String validateItemName() {
		System.out.println("Enter Item Name");
		String s;
		String name = sc.nextLine();
		if (name.isEmpty()) {
			System.out.println("Name cannot be empty");
			s = validateItemName();

		} else {
			name = name.replaceAll(",", " ");
			return name;
		}
		s = s.replaceAll(",", " ");
		return s;

	}

	public int validateNumber() {

		int number;
		do {
			System.out.println("Please enter any positive no.");
			while (!sc.hasNextInt()) {                  
				String input = sc.next();
				System.out.println(input + " is not a valid number. No.should be positive\n");
			}
			number = sc.nextInt();
		} while (number < 0);

		return number;
	}

}
