package quickfix.examples.executor;

public interface Observer {
	
	public void update(String ticker, String sedol, String name,
			String ric, String cusip, String price);
	
}