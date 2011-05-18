package de.mpg.mis.neuesbibliothekssystem.stacks;

import org.springframework.integration.core.SubscribableChannel;

import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.DBEndpoint;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.DBMaster;

public interface UseDBMaster
{
  public void setDBMaster(DBMaster dbmaster);
  
  public DBMaster getDBMaster();
  
  public void setDBEndpoint(DBEndpoint dbendpoint);
  
  public DBEndpoint getDBEndpoint();
  
  public void setDBMasterIUDPubsubChannel(SubscribableChannel channel);
  
  public SubscribableChannel getDBMasterIUDPubsubChannel();
}
