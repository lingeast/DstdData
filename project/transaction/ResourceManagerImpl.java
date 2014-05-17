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
	
	LockManager lm = new LockManager();
    
    // in this toy, we don't care about location or flight number
    protected int flightcounter, 
    flightprice, 
    carscounter, 
    carsprice, 
    roomscounter, 
    roomsprice;
    
    // Mapping xid to transaction private resources
    HashMap <Integer, TransRes> trans = new HashMap<Integer, TransRes>();

    // Use Hash Map to represent tables
    // flightNum as primary key
    HashMap <String, Flight> flights = new HashMap <String, Flight>();
    
    // location as primary key
    HashMap <String, Car> cars = new HashMap <String, Car>();
    
    // location as as primary key
    HashMap <String, Hotel> hotels = new HashMap <String, Hotel>();
    
    // custName as primary key
    HashMap <String, Customer> customers = new HashMap <String, Customer>();
    
    // resvKey or custName? as primary key, combined with customer table
    HashMap <String, ArrayList<Reservation>> reservations = new HashMap <String, ArrayList<Reservation>>();
    
    protected int xidCounter;
    
    // help transaction to acquire a current page on table
    // implicit X lock on page
    // return false if failed (deadlock happens)
    private boolean acqCurPage(TransRes tr, String tableName) {
    	
    	
    	if (tableName.equals("Cars")){
	    	if (tr.cars == null) {
	    		try {
	    			lm.lock(tr.xid, tableName, LockManager.WRITE);
	    			tr.cars = new HashMap <String, Car>(cars);
	    		} catch(DeadlockException dle) {	// handle deadlock
	    			System.err.println(dle.getMessage());
	    			//abort(xid);
	    			return false;
	    		}
	    	}
	    	return true;
    	} else if (tableName.equals("Hotels")){
	    	if (tr.hotels == null) {
	    		try {
	    			lm.lock(tr.xid, tableName, LockManager.WRITE);
	    			tr.hotels = new HashMap <String, Hotel> (hotels);
	    		} catch(DeadlockException dle) {	// handle deadlock
	    			System.err.println(dle.getMessage());
	    			//abort(xid);
	    			return false;
	    		}
	    		return true;
	    	}
    	} else if (tableName.equals("Flights")){
	    	if (tr.flights == null) {
	    		try {
	    			lm.lock(tr.xid, tableName, LockManager.WRITE);
	    			tr.flights = new HashMap <String, Flight>(flights);

	    		} catch(DeadlockException dle) {	// handle deadlock
	    			System.err.println(dle.getMessage());
	    			//abort(xid);
	    			return false;
	    		}
    			return true;
	    	}
    	} else if (tableName.equals("Customers")){
	    	if (tr.customers == null) {
	    		try {
	    			lm.lock(tr.xid, tableName, LockManager.WRITE);
	    			tr.customers = new HashMap<String, Customer>(customers);

	    		} catch(DeadlockException dle) {	// handle deadlock
	    			System.err.println(dle.getMessage());
	    			//abort(xid);
	    			return false;
	    		}
    			return true;
	    	}
    	} else if (tableName.equals("Reservations")){
	    	if (tr.reservations == null) {
	    		try {
	    			lm.lock(tr.xid, tableName, LockManager.WRITE);
	    			tr.reservations = new HashMap <String, ArrayList<Reservation>> (reservations);

	    		} catch(DeadlockException dle) {	// handle deadlock
	    			System.err.println(dle.getMessage());
	    			//abort(xid);
	    			return false;
	    		}
    			return true;
	    	}
    	} else {
    		System.err.println("Unidentified " + tableName);
    	}
    	return false;
    	

    }
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
    	xidCounter = 0;
    }


    // TRANSACTION INTERFACE
    public int start()
    		throws RemoteException {
    	++xidCounter;
    	trans.put(xidCounter, new TransRes(xidCounter));
    	return (xidCounter);
    	
    }

    public boolean commit(int xid)
	throws RemoteException, 
	       TransactionAbortedException, 
	       InvalidTransactionException {
    	System.out.println("Committing");
    	
    	TransRes finished = trans.remove(xid);
    	if (finished == null) 
    		assert(false);
    	// update current to be shadow
    	if (finished.cars != null) cars = finished.cars;
    	if (finished.hotels != null) hotels = finished.hotels;
    	if (finished.flights != null) flights = finished.flights;
    	if (finished.customers != null) customers = finished.customers;
    	if (finished.reservations != null) reservations = finished.reservations;
    	
    	// releases its locks
    	lm.unlockAll(xid);
    	
    	return true; //page shadowing implies page level locking, always return true
    }

    public void abort(int xid)
	throws RemoteException, 
               InvalidTransactionException {
    	trans.remove(xid);
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
        flight.price = flight.price < price ? price : flight.price;
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
        //hotel.price=hotel.price<price?price:hotel.price;
        hotel.price = price;	// directly overwrite
        hotel.numRooms += numRooms;
        hotel.numAvail += numRooms;
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
    	
    	// get right page
        TransRes tr = trans.get(xid);
        if (tr == null){
        	assert(false);
        	return false;
        }
        HashMap <String, Car> curCars = null;
        if (acqCurPage(tr,"Cars")) {
        	curCars = tr.cars;
        } else {
        	abort(xid);
        }
        
        Car car = null;
        if(curCars.containsKey(location))
        	car = curCars.get(location);
        else
        	car = new Car(location,0,0,0);
        //car.price = car.price < price ? price : car.price;
        
        car.price = price; // should directly overwrite price
        car.numCars += numCars;
        car.numAvail += numCars;
        curCars.put(location,car);
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
    	Customer cust = null;

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
    	ArrayList<Reservation> revlist;
    	int total=0;
    	if(reservations.containsKey(custName))
    		revlist=reservations.get(custName);
    	else
    		return -1;
    	for(Reservation r:revlist) total+=r.price;
    	return total;
    }


    // Reservation INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	int price=0;
    	if(flights.containsKey(flightNum)){
    		price=flights.get(flightNum).price;
    	}else return false;
    	Reservation rev = new Reservation(custName, 1, flightNum, price); // 1 for a flight
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
    	// 3 for a car
    	int price=0;
    	if(cars.containsKey(location)){
    		price = cars.get(location).price;
    	} else return false;
    	//TODO: change cars's available number
    	Reservation rev = new Reservation(custName, 3, location,price);
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
    	int price=0;
    	if(hotels.containsKey(location)){
    		price=hotels.get(location).price;
    	}else return false;
    	// 2 for a hotel room
    	Reservation rev = new Reservation(custName,2,location,price);
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
	//int total;
	Customer(String name){
		custName=name;
		//total=0;
	}
}

 class Reservation{
	String custName;
	int resvType;
	String resvKey;
	int price; //for possible calculate
	Reservation(String name,int resvT,String resvK, int pric){
		custName=name;
		resvType=resvT;
		resvKey=resvK;
		price=pric;
		
	}
}

 class TransRes {
	 public final int xid;
	// Transaction's private current page for shadow paging
    public HashMap <String, Flight> flights;
    
    // location as primary key
    public HashMap <String, Car> cars;
    
    // location as as primary key
    public HashMap <String, Hotel> hotels;
    
    // custName as primary key
    public HashMap <String, Customer> customers ;
    
    // resvKey or custName? as primary key, combined with customer table
    public HashMap <String, ArrayList<Reservation>> reservations;
    
    public TransRes (int xid) {
    	this.xid = xid;
    	flights = null;
    	cars = null;
    	hotels = null;
    	customers = null;
    	reservations = null;
    }
    
    /*
    public boolean hasFlights() {
    	return flights;
    }
    
    public boolean hasCars() {
    	return cars != null;
    }
    
    public boolean hasCustomers() {
    	return customers != null;
    }
    
    public boolean hasHotels() {
    	return hotels != null;
    }
    
    public boolean hasReservations() {
    	return reservations != null;
    }
    */
}
