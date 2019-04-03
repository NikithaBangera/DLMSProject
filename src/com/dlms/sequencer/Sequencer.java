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

		new Thread(() -> receiveRequest()).start();

	}

	static void receiveRequest() {

		aSocket = null;
		int sequenceNumber = 0;
		try {

			aSocket = new DatagramSocket(22222);
			System.out.println("Server 22222 Started............");
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
					message = sequenceNumber + "," + message;
					System.out.println(message + " : seq msg");
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
			DatagramPacket mcPacket = new DatagramPacket(msg, msg.length, mcIPAddress, 1313);
			mcPacket.setAddress(mcIPAddress);
			// mcPacket.setPort(mcPort);
			aSocket.send(mcPacket);

			System.out.println("Sent a  multicast message.");
			System.out.println("Exiting application");
			System.out.println("mcPacket" + mcPacket.getData().toString());
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}

	}
}