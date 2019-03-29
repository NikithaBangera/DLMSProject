package com.dlms.replicas.replica1;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.rmi.action.ActionServiceObj;

import ActionServiceApp.ActionServiceHelper;

public class StartServer {
	public static void main(String[] args) {
		try {
			// create and initialize the ORB //// get reference to rootpoa &amp; activate
			// the POAManager
			ORB orb = ORB.init(args, null);
			// -ORBInitialPort 1050 -ORBInitialHost localhost
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// Registry for Concordia

			// create servant and register it with the ORB
			ActionServiceObj conObj = new ActionServiceObj();
			conObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object conRef = rootpoa.servant_to_reference(conObj);
			ActionServiceApp.ActionService ConHref = ActionServiceHelper.narrow(conRef);
			NameComponent ConPath[] = ncRef.to_name("CON");
			ncRef.rebind(ConPath, ConHref);
			System.out.println("Concordia Server ready and waiting ...");
			Concordia.startConcordiaServer();

			// Registry for Montreal

			// create servant and register it with the ORB
			ActionServiceObj monObj = new ActionServiceObj();
			monObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object monRef = rootpoa.servant_to_reference(monObj);
			ActionServiceApp.ActionService monHref = ActionServiceHelper.narrow(monRef);
			NameComponent monPath[] = ncRef.to_name("MON");
			ncRef.rebind(monPath, monHref);
			System.out.println("Montreal Server ready and waiting ...");
			Montreal.startMontrealServer();

			// Registry for McGill
			// create servant and register it with the ORB
			ActionServiceObj mcgObj = new ActionServiceObj();
			mcgObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object mcgRef = rootpoa.servant_to_reference(mcgObj);
			ActionServiceApp.ActionService mcgHref = ActionServiceHelper.narrow(mcgRef);
			NameComponent mcgPath[] = ncRef.to_name("MCG");
			ncRef.rebind(mcgPath, mcgHref);
			System.out.println("McGill Server ready and waiting ...");
			McGill.startMcGillServer();

			// wait for invocations from clients
			for (;;) {
				orb.run();
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("Server Exiting ...");
	}

}
