/**
 * 
 */
package com.cmm.jft.engine.marketdata.recovery;

import javax.management.JMException;

import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.SessionSettings;

import com.cmm.jft.engine.EngineService;

/**
 * <p>
 * <code>SnapshotRecoveryService.java</code>
 * </p>
 * 
 * @author Cristiano M Martins
 * @version 13 de jul de 2015 00:05:40
 *
 */
public class SnapshotRecoveryService extends EngineService {
    
    private SnapshotRecoveryChannel channel;
    
    public SnapshotRecoveryService() {
	try {
	    
	    this.channel = SnapshotRecoveryChannel.getInstance();
	    
	    SessionSettings settings = new SessionSettings(Thread.currentThread().getContextClassLoader()
		    .getResourceAsStream("SnapshotRecoveryService.cfg"));
	    log = LoggerFactory.getLogger(SnapshotRecoveryService.class);
	    init(settings, channel);
	} catch (ConfigError | FieldConvertError | JMException e) {
	    log.error(e.getMessage());
	    e.printStackTrace();
	}
    }
    
    

    /* (non-Javadoc)
     * @see com.cmm.jft.engine.EngineService#start()
     */
    @Override
    public boolean start() {
	super.start();
	
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		while (started) {
		    //System.out.println("sending instruments...");
		    channel.sendSnapshot();
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    }
	}).start();
        return started;
    }
    

    public static void main(String args[]) throws Exception {
	try {
	    SnapshotRecoveryService service = new SnapshotRecoveryService();
	    service.start();

	    System.out.println("press <enter> to quit");
	    System.in.read();

	    service.stop();
	} catch (Exception e) {
	    e.printStackTrace();
	    // log.error(e.getMessage());
	}
    }

}
