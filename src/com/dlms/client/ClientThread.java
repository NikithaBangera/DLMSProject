package com.dlms.client;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import ActionServiceApp.ActionServiceHelper;



public class ClientThread {

	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
		// TODO Auto-generated method stub
		LibraryTestClient testObject = new LibraryTestClient(args);
		testObject.testmultithreading();
	}

}

class LibraryTestClient {
	ORB orb;
	org.omg.CORBA.Object objRef;
	static NamingContextExt ncRef;

	static ActionServiceApp.ActionService serverRef;

	public LibraryTestClient(String[] args) {
		try {
			orb = ORB.init(args, null);
			objRef = orb.resolve_initial_references("NameService");
			ncRef = NamingContextExtHelper.narrow(objRef);
			serverRef = (ActionServiceApp.ActionService) ActionServiceHelper.narrow(ncRef.resolve_str("libraryStub"));
		} catch (Exception e) {
			System.out.println("Client exception: " + e);
			e.printStackTrace();
		}
	}

	public void testmultithreading() throws InterruptedException, BrokenBarrierException {

		Runnable zero = () -> {

			//Return Item
			Thread.currentThread().setName("Thread 0:");
			System.out.println(Thread.currentThread().getName() + "     " + "Concordia:  "
					+ serverRef.returnItem("CONU4444", "MCG3333"));

		};

		//Exchange Item
		Runnable first = () -> {
			Thread.currentThread().setName("Thread 1:");

			System.out.println(Thread.currentThread().getName() + "     " + "Concordia:  "
					+ serverRef.exchangeItem("CONU1111", "MCG3333", "MCG1111"));

		};
		
		//Exchange Item
		Runnable second = () -> {
			Thread.currentThread().setName("Thread 2:");
			System.out.println(Thread.currentThread().getName() + "     " + "Mcgill:  "
					+ serverRef.exchangeItem("MCGU1111", "MCG3333", "MCG1111"));

		};
		
		//Exchange Item
		Runnable third = () -> {
			Thread.currentThread().setName("Thread 3:");
			System.out.println(Thread.currentThread().getName() + "     " + "Montreal:  "
					+ serverRef.exchangeItem("MONU1111", "MCG3333", "MCG1111"));

		};
		

		// Borrow Item
		Runnable fourth = () -> {
			Thread.currentThread().setName("Thread 4:");
			System.out.println(Thread.currentThread().getName() + "     " + "Concordia:  "
					+ serverRef.borrowItem("CONU2222", "MCG3333", 5));

		};
		
		// Borrow Item
		Runnable fifth = () -> {
			Thread.currentThread().setName("Thread 5:");
			System.out.println(Thread.currentThread().getName() + "     " + "Mcgill:  "
					+ serverRef.borrowItem("MCGU2222", "MCG3333", 5));

		};
		
		// Borrow Item
		Runnable sixth = () -> {
			Thread.currentThread().setName("Thread 6:");
			System.out.println(Thread.currentThread().getName() + "     " + "Montreal:  "
					+ serverRef.borrowItem("MONU2222", "MCG3333", 5));

		};

//		// Find Item
		Runnable seventh = () -> {
			Thread.currentThread().setName("Thread 7:");
			System.out.println(Thread.currentThread().getName() + "     " + "Montreal:  "
					+ serverRef.findItem("CONU111", "ADVANCED PROGRAMMING PRACTICES"));

		};
		
		

		Thread thread0 = new Thread(zero);
		Thread thread1 = new Thread(first);
		Thread thread2 = new Thread(second);
		Thread thread3 = new Thread(third);
		Thread thread4 = new Thread(fourth);
		Thread thread5 = new Thread(fifth);
		Thread thread6 = new Thread(sixth);
		Thread thread7 = new Thread(seventh);
		thread1.start();
		// Thread.sleep(3000);
		thread3.start();

		thread2.start();
		thread0.start();
		thread7.start();
		thread4.start();
		Thread.sleep(3000);
		thread6.start();
		thread5.start();


	}

}
