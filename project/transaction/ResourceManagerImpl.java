package transaction;

import lockmgr.*;
import java.rmi.*;
import java.util.HashMap;
/** 
 * Resource Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the RM, for initial testing
 */

public class ResourceManagerImpl 
    extends java.rmi.server.UnicastRemoteObject
    implements ResourceManager {
    
    // in this toy, we don't care about location or flight number
    protected int flightcounter, 
    flightprice, 
    carscounter, 
    carsprice, 
    roomscounter, 
    roomsprice;
    /////////////////////////////////////////////////////////////////////   
    HashMap <String, FLIGHT> flights =new HashMap <String, FLIGHT>();
    HashMap <String, CAR> cars =new HashMap <String, CAR>();
    HashMap <String, HOTEL> hotels =new HashMap <String, HOTEL>();
    HashMap <String, CUSTOMER> customers =new HashMap <String, CUSTOMER>();
    HashMap <String, RESERVATION> reservations =new HashMap <String, RESERVATION>();
    
    protected int xidCounter;
    
    public static void main(String args[]) {
    	System.setSecurityManager(new RMISecurityManager());

    	String rmiName = System.getProperty("rmiName");
    	if (rmiName == null || rmiName.equals("")) {
    		rmiName = ResourceManager.DefaultRMIName;
    	}

		String rmiRegPort = System.getProperty("rmiRegPort");
		if (rmiRegPort != null && !rmiRegPort.equals("")) {
			rmiName = "//:" + rmiRegPort + "/" + rmiName;
		}
		try {
			ResourceManagerImpl obj = new ResourceManagerImpl();
			Naming.rebind(rmiName, obj);
			System.out.println("RM bound");
		} 
		catch (Exception e) {
			System.err.println("RM not bound:" + e);
			System.exit(1);
		}
    }
    
    
    public ResourceManagerImpl() throws RemoteException {
    	flightcounter = 0;
    	flightprice = 0;
    	carscounter = 0;
    	carsprice = 0;
    	roomscounter = 0;
    	roomsprice = 0;
    	flightprice = 0;
    	xidCounter = 1;
    }


    // TRANSACTION INTERFACE
    public int start()
    		throws RemoteException {
    	return (xidCounter++);
    }

    public boolean commit(int xid)
	throws RemoteException, 
	       TransactionAbortedException, 
	       InvalidTransactionException {
    	System.out.println("Committing");
    	return true;
    }

    public void abort(int xid)
	throws RemoteException, 
               InvalidTransactionException {
    	return;
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        FLIGHT flight;
        if(flights.containsKey(flightNum))
        	flight = flights.get(flightNum);
        else
        	flight = new FLIGHT(flightNum,0,0,0);
        flight.price=flight.price<price?price:flight.price;
        flight.numSeats+=numSeats;
        flight.numAvail+=numSeats;
        flights.put(flightNum,flight);
    	++flightcounter;

    	return true;
    }

    public boolean deleteFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        if(flights.containsKey(flightNum)){
        	flights.remove(flightNum);
        	--flightcounter;
        	return true;
        }
        else
        	return false;
    }
		
    public boolean addRooms(int xid, String location, int numRooms, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        HOTEL hotel;
        if(hotels.containsKey(location))
            hotel = hotels.get(location);
        else
            hotel = new HOTEL(location,0,0,0);
        hotel.price=hotel.price<price?price:hotel.price;
        hotel.numRooms+=numRooms;
        hotel.numAvail+=numRooms;
        hotels.put(location,hotel);
        ++roomscounter;
        return true;
    }

    public boolean deleteRooms(int xid, String location, int numRooms) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        if(hotels.containsKey(location)){
        	hotels.remove(location);
        	--roomscounter;
            return true;
        }
        else
            return false;
    }

    public boolean addCars(int xid, String location, int numCars, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        CAR car;
        if(cars.containsKey(location))
        	car = cars.get(location);
        else
        	car = new CAR(location,0,0,0);
        car.price=car.price<price?price:car.price;
        car.numCars+=numCars;
        car.numAvail+=numCars;
        cars.put(location,car);
        ++carscounter;
        return true;
    }

    public boolean deleteCars(int xid, String location, int numCars) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        if(cars.containsKey(location)){
        	cars.remove(location);
        	--carscounter;
        	return true;
    	}
    	else
    		return false;
    }

    public boolean newCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	CUSTOMER cust;
    	if(customers.containsKey(custName))
    		cust = customers.get(custName);
    	else
    		cust = new CUSTOMER(custName);
    	customers.put(custName,cust);

    	return true;
    }

    public boolean deleteCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(customers.containsKey(custName)){
    		customers.remove(custName);
    		return true;
    	}
    	else
    		return false;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return flightcounter;
    }

    public int queryFlightPrice(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return flightprice;
    }

    public int queryRooms(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return roomscounter;
    }

    public int queryRoomsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return roomsprice;
    }

    public int queryCars(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return carscounter;
    }

    public int queryCarsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return carsprice;
    }

    public int queryCustomerBill(int xid, String custName)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	return 0;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	flightcounter--;
    	return true;
    }
 
    public boolean reserveCar(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	carscounter--;
    	return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	roomscounter--;
    	return true;
    }


    // TECHNICAL/TESTING INTERFACE
    public boolean shutdown()
	throws RemoteException {
    	System.exit(0);
    	return true;
    }

    public boolean dieNow() 
	throws RemoteException {
    	System.exit(1);
    	return true; // We won't ever get here since we exited above;
	             // but we still need it to please the compiler.
    }

    public boolean dieBeforePointerSwitch() 
    		throws RemoteException {
    	return true;
    }

    public boolean dieAfterPointerSwitch() 
	throws RemoteException {
    	return true;
    }

}

/////////////////////////////////////////////////////////////////////   
class FLIGHT{
	String flightNum;
	int price;
	int numSeats;
	int numAvail;
	FLIGHT(String flightN){
		flightNum=flightN;
		price=0;
		numSeats=0;
		numAvail=0;
	}
	FLIGHT(String flightN,int pri,int numS,int numA){
		flightNum=flightN;
		price=pri;
		numSeats=numS;
		numAvail=numA;
	}
}

class CAR{
	String location;
	int price;
	int numCars;
	int numAvail;
	CAR(String loc){
		location=loc;	
	}
	CAR(String loc,int pri,int numS,int numA){
		location=loc;
		price=pri;
		numCars=numS;
		numAvail=numA;
	}

}

class HOTEL{
	String location;
	int price;
	int numRooms;
	int numAvail;
	HOTEL(String loc){
		location=loc;
	}
	HOTEL(String loc,int pri,int numS,int numA){
		location=loc;
		price=pri;
		numRooms=numS;
		numAvail=numA;
	}
}

class CUSTOMER{
	String custName;
	CUSTOMER(String name){
		custName=name;
	}
}

class RESERVATION{
	String custName;
	int resvType;
	String resvKey;
	RESERVATION(String name,int resvT,String resvK){
		custName=name;
		resvType=resvT;
		resvKey=resvK;
	}
}