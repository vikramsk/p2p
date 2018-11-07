package p2p;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class P2PLog{
       Logger logger;
	String peerId;
       String timeStamp;

	LogManager logManager = LogManager.getLogManager();
	String path = System.getProperty("user.dir") + File.separator;
	FileHandler fileHandler;
	public P2PLog(String peerId){
		try {
			this.peerId = peerId;
			fileHandler = new FileHandler(path + "log_peer_" + peerId + ".log");
			logger = Logger.getLogger("Log for peer " + peerId);
                     logManager.addLogger(logger);
                     logger.setLevel(Level.INFO);
                     fileHandler.setFormatter(new SimpleFormatter());
                     logger.addHandler(fileHandler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

       public void log(String string){
              logger.log(Level.INFO, string);
       }

       // public String getTime(){
       //        this.timeStamp = new SimpleDateFormat(" MM/dd/yyyy  H:m:s ").format(Calendar.getInstance().getTime());
       //        return timeStamp;
       // }

}