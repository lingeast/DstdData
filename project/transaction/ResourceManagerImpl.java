package transaction;

import lockmgr.*;

import java.rmi.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
/** 
 * Resource Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the RM, for initial testing
 */

public class ResourceManagerImpl 
    extends java.rmi.server.UnicastRemoteObject
    implements ResourceManager {
	//reservation 
    public static final int rFlight = 1;
    public static final int rHotel = 2;
    public static final int rCar = 3;
    
	LockManager lm = new LockManager();
    
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
    
    // custName as primary key, combined with customer table
    HashMap <String, ArrayList<Reservation>> reservations = new HashMap <String, ArrayList<Reservation>>();
    
    protected int xidCounter;
    
    private boolean acqCurEntry(TransRes tr, String tableName, String Primarykey, boolean flag_wr){
    	//check if it already be in table
    	switch(tableName){
	    	case "Cars":
	    		if(tr.cars.containsKey(Primarykey))
	    			return true;
	    		else
	    			break;
			case "Hotels":
	    		if(tr.hotels.containsKey(Primarykey))
	    			return true;
	    		else
	    			break;
		    case "Flights":
		    	if(tr.flights.containsKey(Primarykey))
		    		return true;
	    		else
	    			break;
		    case "Customers":
		    	if(tr.customers.containsKey(Primarykey))
		    		return true;
		    	else
		    		break;
	    	case "Reservations":
		    	if(tr.reservations.containsKey(Primarykey))
					return true;
		    	else
		    		break;
			default: System.err.println("Unidentified " + tableName);
				return false;
    	}
    	//locking flag = true means write
    	if(flag_wr){
	    	try {
				lm.lock(tr.xid, tableName+Primarykey, LockManager.WRITE);
	    	} catch(DeadlockException dle) {	// handle deadlock
				System.err.println(dle.getMessage());
				//abort(xid);
				return false;
			}
    	}
    	else{
        	try {
    			lm.lock(tr.xid, tableName+Primarykey, LockManager.READ);
        	} catch(DeadlockException dle) {	// handle deadlock
    			System.err.println(dle.getMessage());
    			//abort(xid);
    			return false;
    		}
    		
    	}

    	if(flag_wr){
        	//create new entry shadowing/logging for write
		    switch(tableName){
		    	case "Cars":
		    		if(cars.containsKey(Primarykey))
		    				tr.cars.put(Primarykey,new Car(cars.get(Primarykey)));
		    		else
		    				tr.cars.put(Primarykey,new Car(Primarykey));
			    	return true;
			    	
				case "Hotels":
		    		if(hotels.containsKey(Primarykey))
		    				tr.hotels.put(Primarykey,new Hotel(hotels.get(Primarykey)));
		    		else
		    				tr.hotels.put(Primarykey,new Hotel(Primarykey));
			    	return true;
			
			    case "Flights":
			    	if(flights.containsKey(Primarykey))
		    				tr.flights.put(Primarykey,new Flight(flights.get(Primarykey)));
		    		else
		    				tr.flights.put(Primarykey,new Flight(Primarykey));
			    	return true;
			    	
			    case "Customers":
			    	if(customers.containsKey(Primarykey))
							tr.customers.put(Primarykey,new Customer(customers.get(Primarykey)));
			    	else
							tr.customers.put(Primarykey,new Customer(Primarykey));
			    	return true;
			
		    	case "Reservations":
			    	if(reservations.containsKey(Primarykey))
							tr.reservations.put(Primarykey,new ArrayList<Reservation>(reservations.get(Primarykey)));
			    	else
							tr.reservations.put(Primarykey,new ArrayList<Reservation>());
			    	return true;
		
				default: System.err.println("Unidentified " + tableName);
	
			}
	    }else{
		    switch(tableName){
	    	case "Cars":
	    		if(cars.containsKey(Primarykey))
	    				tr.cars.put(Primarykey,cars.get(Primarykey));
		    	return true;
		    	
			case "Hotels":
	    		if(hotels.containsKey(Primarykey))
	    				tr.hotels.put(Primarykey,hotels.get(Primarykey));
		    	return true;
		
		    case "Flights":
		    	if(flights.containsKey(Primarykey))
	    				tr.flights.put(Primarykey,flights.get(Primarykey));
		    	return true;
		    	
		    case "Customers":
		    	if(customers.containsKey(Primarykey))
						tr.customers.put(Primarykey,customers.get(Primarykey));
		    	return true;
		
	    	case "Reservations":
		    	if(reservations.containsKey(Primarykey))
						tr.reservations.put(Primarykey,reservations.get(Primarykey));
		    	return true;
	
			default: System.err.println("Unidentified " + tableName);

		}
	    }
		return false;
	}
    // help transaction to acquire a current page on table
    // implicit X lock on page
    // return false if failed (deadlock happens)
   /* private boolean acqCurPage(TransRes tr, String tableName) {
    	
    	
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
	    	}
	    	return true;
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
	    	}
	    	return true;
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
	    	}
	    	return true;
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
	    	}
	    	return true;
    	} else {
    		System.err.println("Unidentified " + tableName);
    	}
    	return false;
    	

    }*/
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
    	
    	if (finished.cars != null) {
    		HashMap <String, Car> cars_shadowing = new HashMap <String, Car>(cars);
    		for (String key : finished.cars.keySet()) {
    			if(finished.cars.get(key).location!=null)
    				cars_shadowing.put(key, finished.cars.get(key));
    			else
    				cars_shadowing.remove(key);
    		}
    		cars = cars_shadowing;
    	}
    	
    	if (finished.hotels != null){
    		HashMap <String, Hotel> hotels_shadowing = new HashMap <String, Hotel>(hotels);
    		for (String key : finished.hotels.keySet()) {
    			if(finished.hotels.get(key).location!=null)
    				hotels_shadowing.put(key, finished.hotels.get(key));
    			else
    				hotels_shadowing.remove(key);
    		}
    		hotels = hotels_shadowing;
    	}
    	
    	if (finished.flights != null) {
    		HashMap <String, Flight> flights_shadowing = new HashMap <String, Flight>(flights);
    		for (String key : finished.flights.keySet()) {
    			if(finished.flights.get(key).flightNum!=null)
    				flights_shadowing.put(key, finished.flights.get(key));
    			else
    				flights_shadowing.remove(key);
    		}
    		flights = flights_shadowing;
    	}
    	
    	if (finished.customers != null){
    		HashMap <String, Customer> customers_shadowing = new HashMap <String, Customer>(customers);
    		for (String key : finished.customers.keySet()) {
    			if(finished.customers.get(key).custName!=null)
    				customers_shadowing.put(key, finished.customers.get(key));
    			else
    				customers_shadowing.remove(key);
    		}
    		customers = customers_shadowing;
    	}
 
    	if (finished.reservations != null) {
    		HashMap <String, ArrayList<Reservation>> reservations_shadowing = new HashMap <String, ArrayList<Reservation>>(reservations);
    		for (String key : finished.customers.keySet()) {
    				reservations_shadowing.put(key, finished.reservations.get(key));
    		}
    		reservations = reservations_shadowing;
    	}
    	// releases its locks
    	lm.unlockAll(xid);
    	
    	return true; //page shadowing implies page level locking, always return true
    }

    public void abort(int xid)
	throws RemoteException, 
               InvalidTransactionException {
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"aborting");
    	trans.remove(xid);
    	// releases its locks
    	lm.unlockAll(xid);
    	return;
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"adding flight="+flightNum);
        TransRes tr = trans.get(xid);
        
        Flight curFlight = null;
        if (acqCurEntry(tr,"Flights",flightNum,true)) {
        	curFlight = tr.flights.get(flightNum);
        } else {
        	abort(xid);
        }
        
        if(price>0)
        	curFlight.price = price;
        curFlight.numSeats+=numSeats;
        curFlight.numAvail+=numSeats;
        //tr.flights.put(flightNum,curFlight);   //no need

    	return true;
    }

    public boolean deleteFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"deleting flight="+flightNum);
        TransRes tr = trans.get(xid);
        
        Flight curFlight = null;
        if (acqCurEntry(tr,"Flights",flightNum,true)) {
        	curFlight = tr.flights.get(flightNum);
        } else {
        	abort(xid);
        }
        curFlight.flightNum = null;
    	return true;
    } 
		
    public boolean addRooms(int xid, String location, int numRooms, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"adding Rooms="+location);
        TransRes tr = trans.get(xid);
        
        Hotel curHotel = null;
        if (acqCurEntry(tr,"Hotels",location,true)) {
        	curHotel = tr.hotels.get(location);
        } else {
        	abort(xid);
        }
    	
        //hotel.price=hotel.price<price?price:hotel.price;
        if(price>0)
        	curHotel.price = price;	// directly overwrite
        curHotel.numRooms += numRooms;
        curHotel.numAvail += numRooms;

        return true;
    }

    public boolean deleteRooms(int xid, String location, int numRooms) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"deleting Rooms="+location);
        TransRes tr = trans.get(xid);
        
        Hotel curHotel = null;
        if (acqCurEntry(tr,"Hotels",location,true)) {
        	curHotel = tr.hotels.get(location);
        } else {
        	abort(xid);
        }
        
        if(curHotel.numAvail<numRooms||curHotel.numRooms<numRooms)
        	return false;  
        
        curHotel.numRooms -= numRooms;
        curHotel.numAvail -= numRooms;

        if(curHotel.numRooms==0) 
        	curHotel.location=null;
      
        return true;
    }

    public boolean addCars(int xid, String location, int numCars, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"adding Cars="+location);
        TransRes tr = trans.get(xid);
        
        Car curCar = null;
        if (acqCurEntry(tr,"Cars",location,true)) {
        	curCar = tr.cars.get(location);
        } else {
        	abort(xid);
        }
        
        if(price>0)
        	curCar.price = price; // should directly overwrite price
        curCar.numCars += numCars;
        curCar.numAvail += numCars;
        
        return true;
    }

    public boolean deleteCars(int xid, String location, int numCars) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"deleting Cars="+location);
        TransRes tr = trans.get(xid);
        
        Car curCar = null;
        if (acqCurEntry(tr,"Cars",location,true)) {
        	curCar = tr.cars.get(location);
        } else {
        	abort(xid);
        }
        
        if(curCar.numAvail<numCars||curCar.numCars<numCars)
        	return false;
        
        curCar.numCars -= numCars;
        curCar.numAvail -= numCars;
        
        if(curCar.numCars==0) 
        	curCar.location=null;
        return true;
    }

    public boolean newCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"adding new customer="+custName);
        TransRes tr = trans.get(xid);
        
        Customer curCustomer = null;
        if (acqCurEntry(tr,"Customers",custName,true)) {
        	curCustomer = tr.customers.get(custName);
        } else {
        	abort(xid);
        }
        return true;
    }

    public boolean deleteCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"deleting new customer="+custName);
        TransRes tr = trans.get(xid);
        
        Customer curCustomer = null;
        if (acqCurEntry(tr,"Customers",custName,true)) {
        	curCustomer = tr.customers.get(custName);
        } else {
        	abort(xid);
        }
        curCustomer.custName = null;
        return true;
        
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying Flight="+flightNum);
        TransRes tr = trans.get(xid);
        
        Flight curFlight = null;
        if (acqCurEntry(tr,"Flights",flightNum,false)) {
        	curFlight = tr.flights.get(flightNum);
        } else {
        	abort(xid);
        }
        
    	if(curFlight!=null)
			return curFlight.numAvail;
    	else
    		return -1;
    }	

    public int queryFlightPrice(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying Flight price="+flightNum);
        TransRes tr = trans.get(xid);
        
        Flight curFlight = null;
        if (acqCurEntry(tr,"Flights",flightNum,false)) {
        	curFlight = tr.flights.get(flightNum);
        } else {
        	abort(xid);
        }
        
    	if(curFlight!=null)
			return curFlight.price;
    	else
    		return -1;
    }

    public int queryRooms(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying room="+location);
        TransRes tr = trans.get(xid);
        
        Hotel curHotel = null;
        if (acqCurEntry(tr,"Hotels",location,false)) {
        	curHotel = tr.hotels.get(location);
        } else {
        	abort(xid);
        }
    	
    	if(curHotel!=null)
    		return curHotel.numAvail;
    	else
    		return -1;
    }

    public int queryRoomsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying room price="+location);
        TransRes tr = trans.get(xid);
        
        Hotel curHotel = null;
        if (acqCurEntry(tr,"Hotels",location,false)) {
        	curHotel = tr.hotels.get(location);
        } else {
        	abort(xid);
        }
    	
    	if(curHotel!=null)
    		return curHotel.price;
    	else
    		return -1;
    }

    public int queryCars(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying cars="+location);
        TransRes tr = trans.get(xid);
        
        Car curCar = null;
        if (acqCurEntry(tr,"Cars",location,false)) {
        	curCar = tr.cars.get(location);
        } else {
        	abort(xid);
        }
        
    	if(curCar!=null)
			return curCar.numAvail;
    	else
    		return -1;
    }

    public int queryCarsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying cars price="+location);
        TransRes tr = trans.get(xid);
        
        Car curCar = null;
        if (acqCurEntry(tr,"Cars",location,false)) {
        	curCar = tr.cars.get(location);
        } else {
        	abort(xid);
        }
        
    	if(curCar!=null)
			return curCar.price;
    	else
    		return -1;
    }

    public int queryCustomerBill(int xid, String custName)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	int total=0;
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"querying billing of"+custName);
        TransRes tr = trans.get(xid);
        
    	ArrayList<Reservation> curRevlist = null;
         if (acqCurEntry(tr,"Reservations",custName,false)) {
        	 curRevlist = tr.reservations.get(custName);
         } else {
         	abort(xid);
         }
         
    	if(curRevlist==null)
    		return 0;
    	for(Reservation r:curRevlist) {
    		switch(r.resvType){
    			case rFlight:
    				if (acqCurEntry(tr,"Flights",r.resvKey,false)) 
    					total +=  tr.flights.get(r.resvKey).price;
    				else
    					abort(xid);
    				break;
    			case rHotel:
    				if (acqCurEntry(tr,"Hotels",r.resvKey,false)) 
    					total +=  tr.hotels.get(r.resvKey).price;
    				else
    					abort(xid);
    				break;
    			case rCar:
    				if (acqCurEntry(tr,"Cars",r.resvKey,false)) 
    					total +=  tr.cars.get(r.resvKey).price;
    				else
    					abort(xid);
    				break;
    			default: System.err.println("Unidentified reservation");
					return 0;
    		}
    		
    	}
    	return total;
    }


    // Reservation INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"reserving flight="+custName+"+"+flightNum);
        TransRes tr = trans.get(xid);
        
        Flight curFlight = null;
        if (acqCurEntry(tr,"Flights",flightNum,true)) {
        	curFlight = tr.flights.get(flightNum);
        } else {
        	abort(xid);
        }
        
    	ArrayList<Reservation> curRevlist = null;
        if (acqCurEntry(tr,"Reservations",custName,true)) {
       	 curRevlist = tr.reservations.get(custName);
        } else {
        	abort(xid);
        }
        
    	if(curFlight != null&&curFlight.numAvail>0){
    		//price = flight.price;
    		--curFlight.numAvail;
    		++curFlight.numSeats;
    	} else 
    		return false;
    	
    	Reservation rev = new Reservation(custName, 1, flightNum); // 1 for a flight
        
    	curRevlist.add(rev);

    	return true;
    }
 
    public boolean reserveCar(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"reserving cars="+custName+"+"+location);
        TransRes tr = trans.get(xid);
        
        Car curCar = null;
        if (acqCurEntry(tr,"Cars",location,true)) {
        	curCar = tr.cars.get(location);
        } else {
        	abort(xid);
        }
        
    	ArrayList<Reservation> curRevlist = null;
        if (acqCurEntry(tr,"Reservations",custName,true)) {
       	 curRevlist = tr.reservations.get(custName);
        } else {
        	abort(xid);
        }
        
    	if(curCar != null&&curCar.numAvail>0){
    		//price = flight.price;
    		--curCar.numAvail;
    		++curCar.numCars;
    	} else 
    		return false;
    	
    	Reservation rev = new Reservation(custName, 3, location); // 3 for a car
        
    	curRevlist.add(rev);

    	return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	
    	// get transaction
    	if(!trans.containsKey(xid)) 
    		throw new InvalidTransactionException(xid,"reserving room="+custName+"+"+location);
        TransRes tr = trans.get(xid);
        
        Hotel curHotel = null;
        if (acqCurEntry(tr,"Hotels",location,true)) {
        	curHotel = tr.hotels.get(location);
        } else {
        	abort(xid);
        }
        
    	ArrayList<Reservation> curRevlist = null;
        if (acqCurEntry(tr,"Reservations",custName,true)) {
       	 curRevlist = tr.reservations.get(custName);
        } else {
        	abort(xid);
        }
        
    	if(curHotel != null&&curHotel.numAvail>0){
    		//price = flight.price;
    		--curHotel.numAvail;
    		++curHotel.numRooms;
    	} else 
    		return false;
    	
    	Reservation rev = new Reservation(custName, 2, location); // 2 for a room
    	curRevlist.add(rev);

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

	public Flight(Flight flight) {
		flightNum=flight.flightNum;
		price=flight.price;
		numSeats=flight.numSeats;
		numAvail=flight.numAvail;
		// TODO Auto-generated constructor stub
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
	public Car(Car car) {
		// TODO Auto-generated constructor stub
		location=car.location;
		price=car.price;
		numCars=car.numCars;
		numAvail=car.numAvail;
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

	public Hotel(Hotel hotel) {
		// TODO Auto-generated constructor stub
		location=hotel.location;
		price=hotel.price;
		numRooms=hotel.numRooms;
		numAvail=hotel.numAvail;
	}
}

 class Customer{
	String custName;
	//int total;
	Customer(String name){
		custName=name;
		//total=0;
	}
	public Customer(Customer customer) {
		// TODO Auto-generated constructor stub
		custName=customer.custName;
	}
}

 class Reservation{
	String custName;
	int resvType;
	String resvKey;
	//int price; //for possible calculate
	Reservation(String name,int resvT,String resvK){//int pric
		custName=name;
		resvType=resvT;
		resvKey=resvK;
		//price=pric;
		
	}
}

 class TransRes {
	 public final int xid;
	// Transaction's private current page for shadow paging
    public Map <String, Flight> flights;
    
    // location as primary key
    public Map <String, Car> cars;
    
    // location as as primary key
    public Map <String, Hotel> hotels;
    
    // custName as primary key
    public Map <String, Customer> customers ;
    
    // resvKey or custName? as primary key, combined with customer table
    public Map <String, ArrayList<Reservation>> reservations;
    
    public TransRes (int xid) {
    	this.xid = xid;
    	flights = new HashMap <String, Flight>();
    	cars = new HashMap <String, Car>();
    	hotels = new HashMap <String, Hotel>();
    	customers = new HashMap <String, Customer>();
    	reservations = new HashMap <String, ArrayList<Reservation>>();
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

 