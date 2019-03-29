package com.dlms.sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class Sequencer {

	public static Set<String> historyBuffer = new HashSet<String>();
	public static int sizebefore = 0;
	public static int sizeafter = 0;
	public static DatagramSocket aSocket;

	public static void main(String[] args) throws Exception {

		System.out.println("Sequencer has been started successfully");

		new Thread(() -> receiverequest()).start();

	}

	static void receiverequest() {

		aSocket = null;
		int sequenceNumber = 0;
		try {

			aSocket = new DatagramSocket(3333);
			System.out.println("Server 3333 Started............");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);

				String message = new String(request.getData());
				/*
				 * Checking for a duplicate message
				 */
				boolean isDuplicate = checkDuplicateMessage(message);
				if (isDuplicate) {
					continue;
				} else {
					/*
					 * Attaching a unique sequencer number and multi-casting message to all replicas
					 */
					sequenceNumber++;
					message = message + "," + sequenceNumber;
					multicastMessage(message);
					DatagramPacket reply = null;
					
				}
			}

		}

		catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public static boolean checkDuplicateMessage(String message) {

		sizebefore = historyBuffer.size();
		historyBuffer.add(message);
		sizeafter = historyBuffer.size();

		if (sizebefore != sizeafter)
			return false;
		else
			return true;

	}

	public static void multicastMessage(String message) {

		try {

			String mcIPStr = "234.1.1.1";

			InetAddress mcIPAddress = InetAddress.getByName(mcIPStr);
			byte[] msg = message.getBytes();
			DatagramPacket mcPacket = new DatagramPacket(msg, msg.length);
			mcPacket.setAddress(mcIPAddress);
			// mcPacket.setPort(mcPort);
			aSocket.send(mcPacket);

			System.out.println("Sent a  multicast message.");
			System.out.println("Exiting application");
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}

	}
}
