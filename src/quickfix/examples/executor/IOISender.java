package quickfix.examples.executor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import edu.harvard.fas.zfeledy.fiximulator.core.IOI;
import edu.harvard.fas.zfeledy.fiximulator.core.Instrument;
import edu.harvard.fas.zfeledy.fiximulator.core.InstrumentSet;
import quickfix.ConfigError;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.field.Currency;
import quickfix.field.IDSource;
import quickfix.field.IOINaturalFlag;
import quickfix.field.IOIRefID;
import quickfix.field.IOIShares;
import quickfix.field.IOITransType;
import quickfix.field.IOIid;
import quickfix.field.OnBehalfOfCompID;
import quickfix.field.OnBehalfOfSubID;
import quickfix.field.Price;
import quickfix.field.SecurityDesc;
import quickfix.field.SecurityID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.ValidUntilTime;
import quickfix.fix42.Message.Header;

public class IOISender implements Runnable {
	InstrumentSet instruments;
	private Integer delay;
	private String symbolValue = "";
	private String securityIDvalue = "";
	private Random random = new Random();
	private SessionSettings settings = null;
	private SessionID sessionID = null;
	
	public IOISender ( Integer delay, String symbol, String securityID, 
			SessionSettings settings, SessionID sessionID ) {
        instruments = new InstrumentSet(new File("config/instruments.xml"));
        this.delay = delay;
        symbolValue = symbol;
        securityIDvalue = securityID;
        this.settings = settings;
        this.sessionID = sessionID;
	}
	

	public void setDelay(Integer delay){
        this.delay = delay;
	}
	
 	public void setSymbol( String identifier ) {
        symbolValue = identifier;
 	}
 	
 	public void setSecurityID ( String identifier ) {
        securityIDvalue = identifier;
 	}
 	
    public void run() {
        while ( true ) {
            sendRandomIOI();
            try {
                Thread.sleep( delay.longValue() );
            } catch ( InterruptedException e ) {}
        }
    }
	
	public void sendRandomIOI() {
        Instrument instrument = instruments.randomInstrument();
        IOI ioi = new IOI();
        ioi.setType("NEW");
        
        // Side
        ioi.setSide("BUY");
        if ( random.nextBoolean() ) ioi.setSide("SELL");
        
        // IOIShares
        Integer quantity = new Integer( random.nextInt(1000) * 100 + 100 );
        ioi.setQuantity( quantity );

        // Symbol
        String value = "";
        if (symbolValue.equals( "Ticker" ) ) value = instrument.getTicker();
        if (symbolValue.equals( "RIC" ) ) value = instrument.getRIC();
        if (symbolValue.equals( "Sedol" ) ) value = instrument.getSedol();
        if (symbolValue.equals( "Cusip" ) ) value = instrument.getCusip();
        if ( value.equals( "" ) ) value = "<MISSING>";
        ioi.setSymbol( value );
        Symbol symbol = new Symbol( ioi.getSymbol() );
		
        // *** Optional fields ***
        // SecurityID
        value = "";
        if (securityIDvalue.equals("Ticker")) 
            value = instrument.getTicker();
        if (securityIDvalue.equals("RIC")) 
            value = instrument.getRIC();
        if (securityIDvalue.equals("Sedol"))
            value = instrument.getSedol();
        if (securityIDvalue.equals("Cusip"))
            value = instrument.getCusip();
        if ( value.equals( "" ) )
            value = "<MISSING>";
        ioi.setSecurityID( value );
        
        // IDSource
        if ( securityIDvalue.equals("Ticker") ) ioi.setIDSource("TICKER");
        if ( securityIDvalue.equals("RIC") ) ioi.setIDSource("RIC");
        if ( securityIDvalue.equals("Sedol") ) ioi.setIDSource("SEDOL");
        if ( securityIDvalue.equals("Cusip") ) ioi.setIDSource("CUSIP");
        if ( ioi.getSecurityID().equals("<MISSING>") ) 
            ioi.setIDSource("UNKNOWN");

//        SessionSettings settings = null;
        // Price
        int pricePrecision = 4;
//        try {
//        	InputStream inputStream = null;
//            try {
//                inputStream = new BufferedInputStream( 
//                                new FileInputStream( 
//                                new File( "config/FIXimulator.cfg" )));
//            } catch (FileNotFoundException exception) {
//                exception.printStackTrace();
//                System.exit(0);
//            }
            try {
//                settings = new SessionSettings( inputStream );
                pricePrecision = (int)settings.getLong("FIXimulatorPricePrecision");
//            }catch (ConfigError e) {
//                e.printStackTrace();
//            }
        } catch ( Exception e ) {}
        double factor = Math.pow( 10, pricePrecision );
        double price = Math.round( 
                random.nextDouble() * 100 * factor ) / factor;
        ioi.setPrice(price);
        
        // IOINaturalFlag
        ioi.setNatural("No");
        if ( random.nextBoolean() ) ioi.setNatural("Yes");
        
        sendIOI(ioi);
    }
	
	
	 public void sendIOI( IOI ioi) {
	        // *** Required fields ***
	        // IOIid
	        IOIid ioiID = new IOIid( ioi.getID() );

	        // IOITransType
	        IOITransType ioiType = null;
	        if ( ioi.getType().equals("NEW") )
	            ioiType = new IOITransType( IOITransType.NEW );
	        if ( ioi.getType().equals("CANCEL") )
	            ioiType = new IOITransType( IOITransType.CANCEL );
	        if ( ioi.getType().equals("REPLACE") )
	            ioiType = new IOITransType( IOITransType.REPLACE );
	        
	        // Side
	        Side side = null;
	        if ( ioi.getSide().equals("BUY") ) side = new Side( Side.BUY );
	        if ( ioi.getSide().equals("SELL") ) side = new Side( Side.SELL );
	        if ( ioi.getSide().equals("UNDISCLOSED") )
	            side = new Side( Side.UNDISCLOSED );

	        // IOIShares
	        IOIShares shares = new IOIShares( ioi.getQuantity().toString() );

	        // Symbol
	        Symbol symbol = new Symbol( ioi.getSymbol() );

	        // Construct IOI from required fields
	        quickfix.fix42.IndicationofInterest fixIOI = 
	            new quickfix.fix42.IndicationofInterest(
	            ioiID, ioiType, symbol, side, shares);

	        // *** Conditionally required fields ***
	        // IOIRefID
	        IOIRefID ioiRefID = null;
	        if ( ioi.getType().equals("CANCEL") || ioi.getType().equals("REPLACE")){
	            ioiRefID = new IOIRefID( ioi.getRefID() );
	            fixIOI.set(ioiRefID);
	        }
	        
	        // *** Optional fields ***
	        // SecurityID
	        SecurityID securityID = new SecurityID( ioi.getSecurityID() );
	        fixIOI.set( securityID );

	        // IDSource
	        IDSource idSource = null;
	        if (ioi.getIDSource().equals("TICKER"))
	            idSource = new IDSource( IDSource.EXCHANGE_SYMBOL );
	        if (ioi.getIDSource().equals("RIC"))
	            idSource = new IDSource( IDSource.RIC_CODE );
	        if (ioi.getIDSource().equals("SEDOL"))
	            idSource = new IDSource( IDSource.SEDOL );
	        if (ioi.getIDSource().equals("CUSIP"))
	            idSource = new IDSource( IDSource.CUSIP );
	        if (ioi.getIDSource().equals("UNKOWN"))
	            idSource = new IDSource( "100" );
	        fixIOI.set( idSource );

	        // Price
	        Price price = new Price( ioi.getPrice() );
	        fixIOI.set( price );

	        // IOINaturalFlag
	        IOINaturalFlag ioiNaturalFlag = new IOINaturalFlag();
	        if ( ioi.getNatural().equals("YES") ) 
	            ioiNaturalFlag.setValue( true );
	        if ( ioi.getNatural().equals("NO") ) 
	            ioiNaturalFlag.setValue( false );
	        fixIOI.set( ioiNaturalFlag );
	        
	        InstrumentSet instruments = new InstrumentSet(new File("config/instruments.xml"));

	        // SecurityDesc
	        Instrument instrument = 
	        		instruments.getInstrument(ioi.getSymbol());
	        String name = "Unknown security";
	        if ( instrument != null ) name = instrument.getName();
	        SecurityDesc desc = new SecurityDesc( name );
	        fixIOI.set( desc );

	        // ValidUntilTime
	        int minutes = 30;
	        long expiry = new Date().getTime() + 1000 * 60 * minutes;
	        Date validUntil = new Date( expiry );
	        ValidUntilTime validTime = new ValidUntilTime( validUntil );
	        fixIOI.set( validTime );

	        //Currency
	        Currency currency = new Currency( "USD" );
	        fixIOI.set( currency );

	        // *** Send message ***
	        sendMessage(fixIOI);
	    }
	 
	 
	 
	// Message sending methods
	    public void sendMessage( Message message) {
	        String oboCompID = "<UNKNOWN>";
	        String oboSubID = "<UNKNOWN>";
	        boolean sendoboCompID = false;
	        boolean sendoboSubID = false;
	        
	        try {
	            oboCompID = settings.getString(sessionID, "OnBehalfOfCompID");
	            oboSubID = settings.getString(sessionID, "OnBehalfOfSubID");
	            sendoboCompID = settings.getBool("FIXimulatorSendOnBehalfOfCompID");
	            sendoboSubID = settings.getBool("FIXimulatorSendOnBehalfOfSubID");
	        } catch ( Exception e ) {}
	        
	        // Add OnBehalfOfCompID
	        if ( sendoboCompID && !oboCompID.equals("") ) {
	            OnBehalfOfCompID onBehalfOfCompID = new OnBehalfOfCompID(oboCompID);
	            Header header = (Header) message.getHeader();
	            header.set( onBehalfOfCompID );			
	        }

	        // Add OnBehalfOfSubID
	        if ( sendoboSubID && !oboSubID.equals("") ) {
	            OnBehalfOfSubID onBehalfOfSubID = new OnBehalfOfSubID(oboSubID);
	            Header header = (Header) message.getHeader();
	            header.set( onBehalfOfSubID );			
	        }
	        
	        // Send actual message
	        try {
	            Session.sendToTarget( message, sessionID );
	        } catch ( SessionNotFound e ) { e.printStackTrace(); }
	    }
	
}