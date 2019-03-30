package com.dlms.replicas.replica1;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

public class ReplicaManager {

	public static void main(String[] args) {
		try {
			Concordia.startConcordiaServer();
			Montreal.startMontrealServer();
			McGill.startMcGillServer();

			ActionServiceImpl action = new ActionServiceImpl();
			MulticastSocket aSocket = null;
			aSocket = new MulticastSocket(1313);

			aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));

			System.out.println("Server Started............");

			while (true) {
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				System.out.println("abcd---" + request.getData().toString());
				String stringdata = new String(request.getData());
				System.out.println(stringdata);
				String indexi[] = stringdata.split(",");
				System.out.println("index length--" + indexi.length);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
