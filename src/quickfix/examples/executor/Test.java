package quickfix.examples.executor;

import java.io.File;

import quickfix.examples.InstrumentSet;

public class Test {

	public static void main(String[] args) {
		InstrumentSet instruments = new InstrumentSet(new File("config/instruments.xml"));
		
		System.out.println("Test");
	}

}
