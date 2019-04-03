package com.dlms.replicas.replica3;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

public class ConcordiaLibrary {

	static ActionserviceImpl conStub;

	public static void startConcordiaLibrary() {
		try {
			conStub = new ActionserviceImpl("Concordia");
			

			System.out.println("Concordia Server ready and waiting ...");

			new Thread(() -> receiverequest(conStub)).start();

		}

		catch (Exception e) {

			e.printStackTrace();

		}
	}
	
	


	static void receiverequest(ActionserviceImpl conStub) {

		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(5555);
			System.out.println("Server 5555 Started............");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);
				conStub.LOG.info(Thread.currentThread().getName());
				System.out.println(Thread.currentThread().getName());
				String inputData = new String(request.getData());
				DatagramPacket reply = null;

				String message = "";

				String input[] = inputData.split(",");

				String operation = input[0];
				String itemID = input[1];
				String itemName = input[2];
//				int port = Integer.parseInt(input[3]);
				String userID = input[4];
				userID = userID.trim();
				System.out.println("Request recieved from: " + request.getPort());
				conStub.LOG.info("Request recieved from" + request.getPort());
				synchronized(conStub) {
				
				if (operation.equalsIgnoreCase("removeitem"))

				{
					
					
					
					conStub.LOG.info("---------Request recieved to remove an item---------");
					int flag = 0;
					for (String str : conStub.userInfo.keySet()) {
						if (conStub.userInfo.get(str).contains(itemID)) {

							conStub.LOG.info("Successfully removed item with Item ID: " + itemID
									+ " associated with user with ID: " + userID);
							conStub.LOG.info("----SUCCESS----");
							
							conStub.userInfo.get(str).remove(itemID);

							flag = 1;

						}

					}

					if (flag == 1) {
						message = "success: Item ID: " + itemID + " has been removed from users present in Concordia Library successfully";

					} else {
						message = "fail: Item ID: " + itemID + " No student has been issued any item in Concordia Library";
						conStub.LOG.info("----FAILED----");
					}

				}

				else if (operation.equalsIgnoreCase("finditem")) {

					conStub.LOG.info("---------Request recieved to Find an item---------");
					int flag = 0;

					for (String s : conStub.libraryInfo.keySet()) {
						if (conStub.libraryInfo.get(s).containsKey(itemName)) {
                                                        Map.Entry<String, Integer> bookName = conStub.libraryInfo.get(s).entrySet().iterator().next();
//							message = "success:"+ s + " " + conStub.libraryInfo.get(s).get(itemName) + "\n";
                                                        message = s + "-" + bookName.getKey()+","+conStub.libraryInfo.get(s).get(itemName) + "'";
							flag = 1;
							conStub.LOG.info("Item present in Concordia library.");
							conStub.LOG.info("----SUCCESS----");
							break;
						}
					}
					if (flag == 0)

					{
						message = "fail: Item with Item name: " + itemName + " does not exist in the Concordia library\n";
						conStub.LOG.info("Item not present in the library.");
						conStub.LOG.info("----FAILED----");
					}
				} else if (operation.equalsIgnoreCase("returnitem")) {
					
					conStub.LOG.info("---------Request recieved to return an item---------");
					String end = "successfully";
					if (conStub.libraryInfo.containsKey(itemID)) {
						if (conStub.userInfo.containsKey(userID) && conStub.userInfo.get(userID).contains(itemID)) {
							conStub.userInfo.get(userID).remove(itemID);
							Map.Entry<String, Integer> entry = conStub.libraryInfo.get(itemID).entrySet().iterator()
									.next();

							// entry.setValue(entry.getValue() - 1);

							conStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue() + 1);
							conStub.LOG.info("Checking waitlist to find any users registered for this item. ");
							String result = conStub.waitList(null, itemID,0);
							if (result == null) {

								conStub.LOG.info("No users has been registered for this item. ");

								conStub.LOG.info("Returned item successfully to the library. ");
								conStub.LOG.info("----SUCCESS----");

								message = "success: Item returned to the library successfully. Have a nice day!"+ end;
							} else {

								String s[] = result.split(",");
								String answer;
								if (conStub.userInfo.containsKey(s[0])) {
									if (conStub.userInfo.get(s[0]).isEmpty()) {
										conStub.userInfo.get(s[0]).add(s[1]);
										entry.setValue(entry.getValue() - 1);
										conStub.libraryInfo.get(s[1]).put(entry.getKey(), entry.getValue());
										answer = "Item ID: " + s[1]
												+ " has been successfully issued to the user with user ID: " + s[0]
												+ ";\n";

										conStub.LOG
												.info("Item returned to the library successfully. User with user ID: "
														+ s[0] + " has been issued this item ");

										conStub.LOG.info("----SUCCESS----");
									} else {

										conStub.LOG.info("-----FAILED-----");
										answer = "Cannot issue more than one item to the user of another library.";

									}

								} else {
									conStub.userInfo.put(s[0], new ArrayList<String>());
									conStub.userInfo.get(s[0]).add(s[1]);
									entry.setValue(entry.getValue() - 1);
									conStub.libraryInfo.get(s[1]).put(entry.getKey(), entry.getValue());
									answer = "Item ID: " + s[1]
											+ " has been successfully issued to the user with user ID: " + s[0];
									conStub.LOG.info("Item returned to the library successfully.");
									conStub.LOG.info("----SUCCESS----");

								}

								message = "success: Item returned to the library successfully. Have a nice day!";

								message = message + "Waiting queue result: " + answer + "" + end;

							}
						} else {

							message = "fail: Item: " + itemID + " is not issued to user" + userID + "\n";
							conStub.LOG.info("Item not issued to the user who is trying to return the item. ");

							conStub.LOG.info("----FAILED----");

						}

					} else {
						conStub.LOG.info("Item does not belong to the library. Cannot accept the item");
						message = "fail: No such Item ID Exist in the Montreal library\n";
						conStub.LOG.info("----FAILED----");
					}

				} else if (operation.equalsIgnoreCase("borrowitem") || operation.equalsIgnoreCase("exchangeitem")) {

					conStub.LOG.info("---------Request recieved to borrow an item---------");

					if (conStub.libraryInfo.containsKey(itemID))

					{

						if (conStub.userInfo.containsKey(userID) && conStub.userInfo.get(userID).size() >= 1 && !(operation.equalsIgnoreCase("exchangeitem")) ) {
							message = "fail: User with user ID " + userID
									+ " cannot borrow more than one item from Concordia library.\n";
							conStub.LOG.info("-----FAILED-----");
						} else {

							Map.Entry<String, Integer> entry = conStub.libraryInfo.get(itemID).entrySet().iterator()
									.next();
							if (entry.getValue() > 0) {
								if (conStub.userInfo.containsKey(userID)
										&& !conStub.userInfo.get(userID).contains(itemID)) {
									conStub.userInfo.get(userID).add(itemID);
									entry.setValue(entry.getValue() - 1);
									conStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
									message = "success: Item ID: " + itemID
											+ " has been successfully issued to the user with user ID: " + userID;
									conStub.LOG.info("-----SUCCESS-----");
								}

								else if (!conStub.userInfo.containsKey(userID)) {
									conStub.userInfo.put(userID, new ArrayList<String>());
									conStub.userInfo.get(userID).add(itemID);
									entry.setValue(entry.getValue() - 1);
									conStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
									message = "success: Item ID: " + itemID
											+ " has been successfully issued to the user with user ID: " + userID;
									conStub.LOG.info("-----SUCCESS-----");

								} else {
									
									message = "fail: Item ID: " + itemID
											+ " is already issued to the user with user ID: " + userID;
									
									conStub.LOG.info("-----FAILED-----");
									conStub.LOG.info(message);
									
								}

							} else {
								message = "Unavailable";
								conStub.LOG.info("-----FAILED-----");
								conStub.LOG.info(
										"Item does not exist in the library. User to be asked If he wants to be added in the waiting queue. ");

							}
						}

					} else {
						message = "fail: Item does not exist in the library";
						message = "Unavailable";
						conStub.LOG.info("-----FAILED-----");
						conStub.LOG.info(
								"Item does not exist in the library. User to be asked If he wants to be added in the waiting queue. ");

					}

				}

				byte[] resultMessage = message.getBytes();
				reply = new DatagramPacket(resultMessage, message.length(), request.getAddress(), request.getPort());
				conStub.LOG.info("Result is: " + message);
				System.out.println(message); 
				conStub.LOG.info("Sending reply////");
				aSocket.send(reply);
			}
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

	}

}
