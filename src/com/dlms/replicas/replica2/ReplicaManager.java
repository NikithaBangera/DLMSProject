package com.dlms.replicas.replica2;

public class ReplicaManager {
	public static void main(String[] args) {
		try {
			ConcordiaServer.startUDP();
			McgillServer.startUDP();
			MontrealServer.startUDP();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
