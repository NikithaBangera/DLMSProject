package com.dlms.replicas.replica3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Map;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;


public class McGillLibrary {

	
	
	static ActionserviceImpl mcStub;

	public static void startMcGillLibrary() {
		try {
			mcStub = new ActionserviceImpl("McGill");

			System.out.println("Montreal Server ready and waiting ...");

			new Thread(() -> receiverequest(mcStub)).start();

		}

		catch (Exception e) {

			e.printStackTrace();

		}
	}

	static void receiverequest(ActionserviceImpl mcStub) {

		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(6666);
			System.out.println("Server 6666 Started............");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);
				String inputData = new String(request.getData()); 
				DatagramPacket reply = null;
				
				String message = "";
				

				String input[] = inputData.split(",");

				String operation = input[0];
				String itemID = input[1];
				String itemName = input[2];
				int port = Integer.parseInt(input[3]);
				String userID = input[4];
				userID = userID.trim();
				
				synchronized(mcStub) {

				if (operation.equalsIgnoreCase("removeitem"))

				{
					mcStub.LOG.info("---------Request recieved to remove an item---------");
					int flag = 0;
					for (String str : mcStub.userInfo.keySet()) {
						if (mcStub.userInfo.get(str).contains(itemID)) {

							mcStub.LOG.info("Successfully removed item with Item ID: "+ itemID + " associated with user with ID: " + userID);
							mcStub.LOG.info("----SUCCESS----");


							mcStub.userInfo.get(str).remove(itemID);

							flag = 1;

						}

					}

					if (flag == 1) {
						
						message = "success: Item ID: " + itemID + " has been removed from users present in McGill Library";
						mcStub.LOG.info("----SUCCESS----");

					} else {
						
						message = "fail: Item ID: " + itemID + " No student has been issued any item in McGill Library";
						mcStub.LOG.info("----FAILED----");

					}

				}

				else if (operation.equalsIgnoreCase("finditem")) {
					int flag = 0;

					mcStub.LOG.info("---------Request recieved to Find an item---------");
					for (String s : mcStub.libraryInfo.keySet()) {
						if (mcStub.libraryInfo.get(s).containsKey(itemName)) {
                                                         Map.Entry<String, Integer> bookName = mcStub.libraryInfo.get(s).entrySet().iterator().next();
//							message = "success:"+s + " " + mcStub.libraryInfo.get(s).get(itemName) + "\n";
                                                        message = s + "-" + bookName.getKey()+","+mcStub.libraryInfo.get(s).get(itemName) + "'";
							flag = 1;
							mcStub.LOG.info("Item present in Mcgill library.");
							mcStub.LOG.info("----SUCCESS----");
							break;
						}
					}
					if (flag == 0)

					{
						message = "fail: Item with Item name: " + itemName + " does not exist in the McGill library\n";
						mcStub.LOG.info("Item not present in the library.");
						mcStub.LOG.info("----FAILED----");
						
					}
				}

				else if (operation.equalsIgnoreCase("returnitem")) {
					String end = "successfully";
					mcStub.LOG.info("---------Request recieved to return an item---------");
					
					if (mcStub.libraryInfo.containsKey(itemID)) {
						
						if (mcStub.userInfo.containsKey(userID) && mcStub.userInfo.get(userID).contains(itemID)) {
							
							mcStub.userInfo.get(userID).remove(itemID);
							Map.Entry<String, Integer> entry = mcStub.libraryInfo.get(itemID).entrySet().iterator()
									.next();

							// entry.setValue(entry.getValue() - 1);

							mcStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue() + 1);
							mcStub.LOG.info("Checking waitlist to find any users registered for this item. ");
							String result = mcStub.waitList(null, itemID,0);
							if (result == null) {
								
								mcStub.LOG.info("No users has been registered for this item. ");
								
								mcStub.LOG.info("Returned item successfully to the library. ");
								mcStub.LOG.info("----SUCCESS----");
								
								message = "success: Item returned to the library successfully. Have a nice day!"+ end;
							} else {

								String s[] = result.split(",");
								String answer;
								if (mcStub.userInfo.containsKey(s[0])) {
									if (mcStub.userInfo.get(s[0]).isEmpty()) {
										mcStub.userInfo.get(s[0]).add(s[1]);
										entry.setValue(entry.getValue() - 1);
										mcStub.libraryInfo.get(s[1]).put(entry.getKey(), entry.getValue());
										answer = "Item ID: " + s[1]
												+ " has been successfully issued to the user with user ID: " + s[0]
												+ ";\n";
										
										mcStub.LOG.info("Item returned to the library successfully. User with user ID: " + s[0] + " has been issued this item ");
										
										mcStub.LOG.info("----SUCCESS----");
										
										
									} else {
										
										mcStub.LOG.info("-----FAILED-----");
										
										answer = "Cannot issue more than one item to the user of another library.";

									}

								} else {
									mcStub.userInfo.put(s[0], new ArrayList<String>());
									mcStub.userInfo.get(s[0]).add(s[1]);
									entry.setValue(entry.getValue() - 1);
									mcStub.libraryInfo.get(s[1]).put(entry.getKey(), entry.getValue());
									answer = "Item ID: " + s[1]
											+ " has been successfully issued to the user with user ID: " + s[0] + ";\n";
									mcStub.LOG.info("Item returned to the library successfully.");
									mcStub.LOG.info("----SUCCESS----");
									

								}

								message = "success: Item returned to the library successfully. Have a nice day!";

								message = message + "Waiting queue result: " + answer + "" + end;

							}
						} else {

							message = "fail: Item: " + itemID + " is not issued to user" + userID + "\n";
							
							mcStub.LOG.info("Item not issued to the user who is trying to return the item. ");
							
							mcStub.LOG.info("----FAILED----");
							
						}

					} else {
						
						mcStub.LOG.info("Item does not belong to the library. Cannot accept the item");
						
						message = "fail: No such Item ID Exist in the Montreal library\n";
						mcStub.LOG.info("----FAILED----");

					}

				} else if (operation.equalsIgnoreCase("borrowitem") || operation.equalsIgnoreCase("exchangeitem")) {
					
					mcStub.LOG.info("---------Request recieved to borrow an item---------");
					
					if (mcStub.libraryInfo.containsKey(itemID))

					{
						if (mcStub.userInfo.containsKey(userID) && mcStub.userInfo.get(userID).size() >= 1 && !(operation.equalsIgnoreCase("exchangeitem")) ) {
							message = "fail: User with user ID " + userID
									+ " cannot borrow more than one item from McGill library.\n";
							mcStub.LOG.info("-----FAILED-----");
							
						} else {

							Map.Entry<String, Integer> entry = mcStub.libraryInfo.get(itemID).entrySet().iterator()
									.next();
							if (entry.getValue() > 0) {
								if (mcStub.userInfo.containsKey(userID) && !mcStub.userInfo.get(userID).contains(itemID)) {
									mcStub.userInfo.get(userID).add(itemID);
									entry.setValue(entry.getValue() - 1);
									mcStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
									message = "success: Item ID: " + itemID
											+ " has been successfully issued to the user with user ID: " + userID;
									
									mcStub.LOG.info("-----SUCCESS-----");
								}

								else if (!mcStub.userInfo.containsKey(userID)) {
									mcStub.userInfo.put(userID, new ArrayList<String>());
									mcStub.userInfo.get(userID).add(itemID);
									entry.setValue(entry.getValue() - 1);
									mcStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
									message = "success: Item ID: " + itemID
											+ " has been successfully issued to the user with user ID: " + userID;
									mcStub.LOG.info("-----SUCCESS-----");
								}
								else {
									
									message = "fail: Item ID: " + itemID
											+ " is already issued to the user with user ID: " + userID;
									
									mcStub.LOG.info("-----FAILED-----");
									mcStub.LOG.info(message);
									
								}

							} else {
								message = "Unavailable";
								mcStub.LOG.info("-----FAILED-----");
								mcStub.LOG.info("Item does not exist in the library. User to be asked If he wants to be added in the waiting queue. ");

							}
						}

					} else {
						
						message = "fail: Item does not exist in the library";
						message = "Unavailable";
						mcStub.LOG.info("-----FAILED-----");
						mcStub.LOG.info("Item does not exist in the library. User to be asked If he wants to be added in the waiting queue. ");
					}

				}

				
				byte[] resultMessage = message.getBytes();
				reply = new DatagramPacket(resultMessage, message.length(), request.getAddress(), request.getPort());
				
				mcStub.LOG.info("Result is: " + message);
				System.out.println(message);
				mcStub.LOG.info("Sending reply////");
				
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
