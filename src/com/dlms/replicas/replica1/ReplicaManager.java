package com.dlms.replicas.replica1;

import java.util.HashMap;

public class ReplicaManager {

	public static void main(String[] args) {
		try {
			Concordia.startConcordiaServer();
			Montreal.startMontrealServer();
			McGill.startMcGillServer();
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
