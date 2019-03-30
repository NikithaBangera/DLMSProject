package montrealServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import concordiaServer.library.Library;
import concordiaServer.library.LibraryHelper;
import libraryImplementation.LibraryImplementation;

public class MontrealLibrary {

	public static void main(String[] args) throws Exception {

		try {

			System.out.println("Montreal Library Server has been started successfully");

			ORB orb = ORB.init(args, null);

			POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");

			rootpoa.the_POAManager().activate();

			
			LibraryImplementation monStub = new LibraryImplementation("Montreal");

			monStub.setORB(orb);

			
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(monStub);
			Library href = LibraryHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name("montrealStub");
			ncRef.rebind(path, href);

			System.out.println("Montreal Server ready and waiting ...");
			for (;;) {
				new Thread(() -> receiverequest(monStub)).start();
				orb.run();

			}

		}

		catch (Exception e) {

			e.printStackTrace();

		}
	}

	
	static void receiverequest(LibraryImplementation monStub) {

		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(7777);
			System.out.println("Server 7777 Started............");
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

				synchronized (monStub) {

					if (operation.equalsIgnoreCase("removeitem"))

					{
						monStub.LOG.info("---------Request recieved to remove an item---------");

						int flag = 0;
						for (String str : monStub.userInfo.keySet()) {
							if (monStub.userInfo.get(str).contains(itemID)) {

								monStub.LOG.info("Successfully removed item with Item ID: " + itemID
										+ " associated with user with ID: " + userID);
								monStub.LOG.info("----SUCCESS----");

								monStub.userInfo.get(str).remove(itemID);

								flag = 1;

							}

						}

						if (flag == 1) {
							message = "Item ID: " + itemID + " has been removed from users present in Montreal Library";

						} else {
							message = "Item ID: " + itemID + " No student has been issued any item in Montreal Library";

							monStub.LOG.info("----FAILED----");
						}

					}

					else if (operation.equalsIgnoreCase("finditem")) {
						int flag = 0;

						monStub.LOG.info("---------Request recieved to Find an item---------");

						for (String s : monStub.libraryInfo.keySet()) {
							if (monStub.libraryInfo.get(s).containsKey(itemName)) {

								message = s + " " + monStub.libraryInfo.get(s).get(itemName) + "\n";
								flag = 1;

								monStub.LOG.info("Item present in Montreal library.");
								monStub.LOG.info("----SUCCESS----");
								break;
							}
						}
						if (flag == 0)

						{
							message = "Item with Item name: " + itemName + " does not exist in the Montreal library\n";

							monStub.LOG.info("Item not present in the library.");
							monStub.LOG.info("----FAILED----");
						}
					}

					else if (operation.equalsIgnoreCase("returnitem")) {
						String end = "successfully";
						monStub.LOG.info("---------Request recieved to return an item---------");

						if (monStub.libraryInfo.containsKey(itemID)) {
							if (monStub.userInfo.containsKey(userID) && monStub.userInfo.get(userID).contains(itemID)) {
								monStub.userInfo.get(userID).remove(itemID);
								Map.Entry<String, Integer> entry = monStub.libraryInfo.get(itemID).entrySet().iterator()
										.next();

								// entry.setValue(entry.getValue() - 1);

								monStub.LOG.info("Checking waitlist to find any users registered for this item. ");

								monStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue() + 1);
								String result = monStub.waitingQueue(null, itemID);
								if (result == null) {

									monStub.LOG.info("No users has been registered for this item. ");

									monStub.LOG.info("Returned item successfully to the library. ");
									monStub.LOG.info("----SUCCESS----");

									message = "Item returned to the library successfully. Have a nice day!" + end;
								} else {

									String s[] = result.split(",");
									String answer;
									if (monStub.userInfo.containsKey(s[0])) {
										if (monStub.userInfo.get(s[0]).isEmpty()) {
											monStub.userInfo.get(s[0]).add(s[1]);
											entry.setValue(entry.getValue() - 1);
											monStub.libraryInfo.get(s[1]).put(entry.getKey(), entry.getValue());
											answer = "Item ID: " + s[1]
													+ " has been successfully issued to the user with user ID: " + s[0]
													+ ";\n";

											monStub.LOG.info(
													"Item returned to the library successfully. User with user ID: "
															+ s[0] + " has been issued this item ");

											monStub.LOG.info("----SUCCESS----");

										} else {

											monStub.LOG.info("-----FAILED-----");
											answer = "Cannot issue more than one item to the user of another library.";

										}

									} else {
										monStub.userInfo.put(s[0], new ArrayList<String>());
										monStub.userInfo.get(s[0]).add(s[1]);
										entry.setValue(entry.getValue() - 1);
										monStub.libraryInfo.get(s[1]).put(entry.getKey(), entry.getValue());
										answer = "Item ID: " + s[1]
												+ " has been successfully issued to the user with user ID: " + s[0]
												+ ";\n";

										monStub.LOG.info("Item returned to the library successfully.");
										monStub.LOG.info("----SUCCESS----");

									}

									message = "Item returned to the library successfully. Have a nice day!";

									message = message + "Waiting queue result: " + answer + "" + end;

								}
							} else {

								message = "Item: " + itemID + " is not issued to user" + userID + "\n";

								monStub.LOG.info("Item not issued to the user who is trying to return the item. ");

								monStub.LOG.info("----FAILED----");
							}

						} else {

							monStub.LOG.info("Item does not belong to the library. Cannot accept the item");
							message = "No such Item ID Exist in the Montreal library\n";

							monStub.LOG.info("----FAILED----");
						}

					} else if (operation.equalsIgnoreCase("borrowitem") || operation.equalsIgnoreCase("exchangeitem")) {

						System.out.println(itemID);
						System.out.println(userID);
						System.out.println(monStub.libraryInfo.get(itemID));
						monStub.LOG.info("---------Request recieved to borrow an item---------");
						if (monStub.libraryInfo.containsKey(itemID))

						{
							if (monStub.userInfo.containsKey(userID) && monStub.userInfo.get(userID).size() >= 1
									&& !(operation.equalsIgnoreCase("exchangeitem"))) {
								message = "User with user ID " + userID
										+ " cannot borrow more than one item from Montreal library.\n";

								monStub.LOG.info("-----FAILED-----");
							} else {

								Map.Entry<String, Integer> entry = monStub.libraryInfo.get(itemID).entrySet().iterator()
										.next();
								if (entry.getValue() > 0) {
									if (monStub.userInfo.containsKey(userID)
											&& !monStub.userInfo.get(userID).contains(itemID)) {
										monStub.userInfo.get(userID).add(itemID);
										entry.setValue(entry.getValue() - 1);
										monStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
										message = "Item ID: " + itemID
												+ " has been successfully issued to the user with user ID: " + userID;
										monStub.LOG.info("-----SUCCESS-----");
									}

									else if (!monStub.userInfo.containsKey(userID)) {
										monStub.userInfo.put(userID, new ArrayList<String>());
										monStub.userInfo.get(userID).add(itemID);
										entry.setValue(entry.getValue() - 1);
										monStub.libraryInfo.get(itemID).put(entry.getKey(), entry.getValue());
										message = "Item ID: " + itemID
												+ " has been successfully issued to the user with user ID: " + userID;
										monStub.LOG.info("-----SUCCESS-----");

									} else {

										message = "Item ID: " + itemID + " is already issued to the user with user ID: "
												+ userID;

										monStub.LOG.info("-----FAILED-----");
										monStub.LOG.info(message);

									}

								} else {
									message = "waitingqueue";
									monStub.LOG.info("-----FAILED-----");
									monStub.LOG.info(
											"Item does not exist in the library. User to be asked If he wants to be added in the waiting queue. ");

								}
							}

						} else {
							message = "Item does not exist in the library";
							message = "waitingqueue";
							monStub.LOG.info("-----FAILED-----");
							monStub.LOG.info(
									"Item does not exist in the library. User to be asked If he wants to be added in the waiting queue. ");

						}

					}

					byte[] resultMessage = message.getBytes();
					reply = new DatagramPacket(resultMessage, message.length(), request.getAddress(),
							request.getPort());

					monStub.LOG.info("Result is: " + message);
					System.out.println(message);
					monStub.LOG.info("Sending reply////");
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
