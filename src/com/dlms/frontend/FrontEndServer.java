package com.dlms.frontend;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;

import ActionServiceApp.ActionService;
import ActionServiceApp.ActionServiceHelper;

public class FrontEndServer {

	public static void main(String[] args) throws Exception {

		try {

			System.out.println("Front-End Server has been started successfully");

			ORB orb = ORB.init(args, null);

			POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");

			rootpoa.the_POAManager().activate();

			
			FrontEndImplementation libraryStub = new FrontEndImplementation("frontEndOne");

			libraryStub.setORB(orb);

			
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(libraryStub);
			ActionService href = ActionServiceHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name("libraryStub");
			ncRef.rebind(path, href);

			System.out.println("Front-End Server ready and waiting ...");
			for (;;) {
			//	new Thread(() -> receiverequest(libraryStub)).start();
				orb.run();

			}

		}
		catch (Exception e) {

			e.printStackTrace();

		}
	
	
	
	
	
}
}