package transaction;

import lockmgr.*;
import java.rmi.*;
import java.util.HashMap;
import java.util.ArrayList;
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
    HashMap <String, Flight> flights =new HashMap <String, Flight>();
    HashMap <String, Car> cars =new HashMap <String, Car>();
    HashMap <String, Hotel> hotels =new HashMap <String, Hotel>();
    HashMap <String, Customer> customers =new HashMap <String, Customer>();
    
    HashMap <String, ArrayList<Reservation>> reservations =new HashMap <String, ArrayList<Reservation>>();
    
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
        Flight flight;
        if(flights.containsKey(flightNum))
        	flight = flights.get(flightNum);
        else
        	flight = new Flight(flightNum,0,0,0);
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
        Hotel hotel;
        if(hotels.containsKey(location))
            hotel = hotels.get(location);
        else
            hotel = new Hotel(location,0,0,0);
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
        Car car;
        if(cars.containsKey(location))
        	car = cars.get(location);
        else
        	car = new Car(location,0,0,0);
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
    	Customer cust;

    	if(customers.containsKey(custName))
    		//cust = customers.get(custName);
    		return false;
    	else{
    		cust = new Customer(custName);
    		customers.put(custName,cust);
			return true;
    	}
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
    	if(flights.containsKey(flightNum))
			return flights.get(flightNum).numAvail;
    	else
    		return -1;
    }	

    public int queryFlightPrice(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(flights.containsKey(flightNum))
    		return flights.get(flightNum).price;
    	else
    		return -1;
    }

    public int queryRooms(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(hotels.containsKey(location))
    		return hotels.get(location).numAvail;
    	else
    		return -1;
    }

    public int queryRoomsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(hotels.containsKey(location))
    		return hotels.get(location).price;
    	else
    		return -1;
    }

    public int queryCars(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(cars.containsKey(location))
			return cars.get(location).numAvail;
    	else
    		return -1;
    }

    public int queryCarsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(cars.containsKey(location))
    		return cars.get(location).price;
    	else
    		return -1;
    }

    public int queryCustomerBill(int xid, String custName)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	if(customers.containsKey(custName))
    		return customers.get(custName).total;
    	else
    		return -1;
    }


    // Reservation INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	Reservation rev = new Reservation(custName,1,flightNum);
    	ArrayList<Reservation> revlist;
    	if(!reservations.containsKey(custName)){
    		revlist= new ArrayList<Reservation>();
    	}else{
    		revlist=reservations.get(custName);
    	}
		revlist.add(rev);
		reservations.put(custName, revlist);
    	return true;
    }
 
    public boolean reserveCar(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	Reservation rev = new Reservation(custName,3,location);
    	ArrayList<Reservation> revlist;
    	if(!reservations.containsKey(custName)){
    		revlist= new ArrayList<Reservation>();
    	}else{
    		revlist=reservations.get(custName);
    	}
		revlist.add(rev);
		reservations.put(custName, revlist);
    	return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	Reservation rev = new Reservation(custName,2,location);
    	ArrayList<Reservation> revlist;
    	if(!reservations.containsKey(custName)){
    		revlist= new ArrayList<Reservation>();
    	}else{
    		revlist=reservations.get(custName);
    	}
		revlist.add(rev);
		reservations.put(custName, revlist);
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
class Flight{
	String flightNum;
	int price;
	int numSeats;
	int numAvail;
	
	Flight(String flightN){
		flightNum=flightN;
		price=0;
		numSeats=0;
		numAvail=0;
	}
	
	Flight(String flightN,int pri,int numS,int numA){
		flightNum=flightN;
		price=pri;
		numSeats=numS;
		numAvail=numA;
	}
}

class Car{
	String location;
	int price;
	int numCars;
	int numAvail;
	
	Car(String loc){
		location=loc;	
	}
	Car(String loc,int pri,int numS,int numA){
		location=loc;
		price=pri;
		numCars=numS;
		numAvail=numA;
	}

}

class Hotel{
	
	String location;
	int price;
	int numRooms;
	int numAvail;
	
	Hotel(String loc){
		location=loc;
	}
	
	Hotel(String loc,int pri,int numS,int numA){
		location=loc;
		price=pri;
		numRooms=numS;
		numAvail=numA;
	}
}

class Customer{
	String custName;
	int total;
	Customer(String name){
		custName=name;
		total=0;
	}
}

class Reservation{
	String custName;
	int resvType;
	String resvKey;
	//int price; //for possible calculate
	Reservation(String name,int resvT,String resvK){
		custName=name;
		resvType=resvT;
		resvKey=resvK;
		
	}
}
