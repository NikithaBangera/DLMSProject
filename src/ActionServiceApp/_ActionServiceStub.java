package ActionServiceApp;


/**
* ActionServiceApp/_ActionServiceStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ActionService.idl
* Saturday, March 30, 2019 5:33:58 PM EDT
*/

public class _ActionServiceStub extends org.omg.CORBA.portable.ObjectImpl implements ActionServiceApp.ActionService
{

  public String addItem (String managerID, String itemID, String itemName, int quantity)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("addItem", true);
                $out.write_string (managerID);
                $out.write_string (itemID);
                $out.write_string (itemName);
                $out.write_long (quantity);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return addItem (managerID, itemID, itemName, quantity        );
            } finally {
                _releaseReply ($in);
            }
  } // addItem

  public String removeItem (String managerID, String itemID, int quantity)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("removeItem", true);
                $out.write_string (managerID);
                $out.write_string (itemID);
                $out.write_long (quantity);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return removeItem (managerID, itemID, quantity        );
            } finally {
                _releaseReply ($in);
            }
  } // removeItem

  public String listItemAvailability (String managerID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("listItemAvailability", true);
                $out.write_string (managerID);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return listItemAvailability (managerID        );
            } finally {
                _releaseReply ($in);
            }
  } // listItemAvailability

  public String borrowItem (String userID, String itemID, int numberOfDays)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("borrowItem", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $out.write_long (numberOfDays);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return borrowItem (userID, itemID, numberOfDays        );
            } finally {
                _releaseReply ($in);
            }
  } // borrowItem

  public String waitList (String userID, String itemID, int numberOfDays)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("waitList", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $out.write_long (numberOfDays);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return waitList (userID, itemID, numberOfDays        );
            } finally {
                _releaseReply ($in);
            }
  } // waitList

  public String findItem (String userID, String itemName)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("findItem", true);
                $out.write_string (userID);
                $out.write_string (itemName);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return findItem (userID, itemName        );
            } finally {
                _releaseReply ($in);
            }
  } // findItem

  public String returnItem (String userID, String itemID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("returnItem", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return returnItem (userID, itemID        );
            } finally {
                _releaseReply ($in);
            }
  } // returnItem

  public String exchangeItem (String userID, String newItemID, String oldItemID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("exchangeItem", true);
                $out.write_string (userID);
                $out.write_string (newItemID);
                $out.write_string (oldItemID);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return exchangeItem (userID, newItemID, oldItemID        );
            } finally {
                _releaseReply ($in);
            }
  } // exchangeItem

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:ActionServiceApp/ActionService:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _ActionServiceStub
