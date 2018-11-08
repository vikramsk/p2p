package p2p;

import java.io.File;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class P2PLog{

	static Logger logger;
	
	public static Logger GetLogger(String peerID){
		try {
			String logName = "log_peer_" + peerID;
			String path = System.getProperty("user.dir") + File.separator;
			FileHandler fileHandler = new FileHandler(path + logName + ".log");
			fileHandler.setFormatter(new SimpleFormatter() {
				private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

				@Override
				public synchronized String format(LogRecord lr) {
					return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
							lr.getMessage());
				}
			});
			
			logger = Logger.getLogger(logName);
			logger.setLevel(Level.INFO);
			logger.addHandler(fileHandler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logger;
	}
}