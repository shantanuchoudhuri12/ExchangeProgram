/*
 * File     : Instrument.java
 *
 * Author   : Zoltan Feledy
 * 
 * Contents : This class is a basic Instrument object that is used to 
 *            create and store instrument details.
 * 
 */

package quickfix.examples;

import java.util.ArrayList;

import quickfix.examples.executor.Observer;
import quickfix.examples.executor.Subject;

public class Instrument implements Subject{
	
	private ArrayList<Observer> observers;
	
	private String ticker;
	private String cusip;
	private String sedol;
	private String name;
	private String ric;
	private String price;
	
	public Instrument( String ticker, String sedol, String name,
			String ric, String cusip, String price ) {
		this.ticker = ticker;
		this.cusip = cusip;
		this.sedol = sedol;
		this.name = name;
		this.ric = ric;
		this.price = price;
		
		observers = new ArrayList<Observer>();
		
		notifyObserver();
	}
	
	
public void register(Observer newObserver) {
		
		// Adds a new observer to the ArrayList
		
		observers.add(newObserver);
		
	}

	public void unregister(Observer deleteObserver) {
		
		// Get the index of the observer to delete
		
		int observerIndex = observers.indexOf(deleteObserver);
		
		// Print out message (Have to increment index to match)
		
		System.out.println("Observer " + (observerIndex+1) + " deleted");
		
		// Removes observer from the ArrayList
		
		observers.remove(observerIndex);
		
	}

	public void notifyObserver() {
		
		// Cycle through all observers and notifies them of
		// price changes
		
		for(Observer observer : observers){
			
			observer.update(ticker, sedol, name,
					ric, cusip, price);
			
		}
	}
	
	public String getTicker() {
		return ticker;
	}
	
	public String getCusip() {
		return cusip;
	}
	
	public String getSedol() {
		return sedol;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRIC() {
		return ric;
	}
	
	public String getPrice() {
		return price;
	}
}