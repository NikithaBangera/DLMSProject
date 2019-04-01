package com.dlms.replicas.replica1;

public interface ActionService {
	public String addItem(String managerID, String itemID, String itemName, int quantity);

	public String removeItem(String managerID, String itemID, int quantity);

	public String listItemAvailability(String managerID);

	public String borrowItem(String userID, String itemID, int numberOfDays);

	public String waitList(String userID, String itemID, int numberOfDays);

	public String findItem(String userID, String itemName);

	public String returnItem(String userID, String itemID);
	
	public boolean validateUser(String userID);
	
	public String exchangeItem (String userID, String newItemID, String oldItemID);
}
