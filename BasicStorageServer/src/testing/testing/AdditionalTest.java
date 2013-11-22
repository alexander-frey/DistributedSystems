package testing;

import client.KVStore;
import common.messages.KVMessage;
import java.net.ProtocolException;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;

import junit.framework.TestCase;

public class AdditionalTest extends TestCase {
	
	// TODO add your test cases, at least 3
    private KVStore kvClient;
	
  
	
	public void setUp() {
            
		kvClient = new KVStore("localhost", 50000);
		try {
			kvClient.connect();
		} catch (Exception e) {
		}
	}
	@Test
	public void testGetDisconnected() {
		kvClient.disconnect();
		String key = "foo";
		
		Exception ex = null;

		try {
			kvClient.get(key);
		} catch (Exception e) {
			ex = e;
		}

		assertNotNull(ex);
	}
        
        @Test
	public void testUpdateDisconnected() {
                kvClient.disconnect();
                
		String key = "updateTestValue";
		String initialValue = "initial";
		String updatedValue = "updated";
		
		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient.put(key, initialValue);
			response = kvClient.put(key, updatedValue);
			
		} catch (Exception e) {
			ex = e;
		}
                    
                
		assertNotNull(ex);
	}
        
         @Test
	public void testDoubleConnect() {
            
              
               Exception ex = null;
		try {
			kvClient.connect();
		} catch (Exception e) {
                    ex =e;
                   
		}
                
                
                assertNotNull(ex);
	}
        
        
        
        
}
