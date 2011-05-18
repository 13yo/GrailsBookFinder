package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.util.Map;

import org.springframework.integration.Message;

import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.DBAction;
import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.IUDType;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.DBMaster;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DBiDTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DBiFDTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DBsDTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DBsEDTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DTOE;

public class IUDMessageResolver
{
  public IUDMessageResolver()
  {
  }
  
  public Object[] resolveIUDMessage(Message<?> message,DBMaster dbmaster) throws Exception
  {
    Object[] aus=null;
    Map map=(Map)message.getPayload();
//    IUDType payload=(IUDType)((Map)message.getPayload()).get("dbObject");
//    DBAction dbAction=payload.getAction();
    DBAction dbAction=(DBAction)map.get("action");
//    DTO dto=(DTO)payload.getDbObject();
    DTO dto=(DTO)map.get("dbObject");
    DTOE dtoType=dto.getDtoType();
    System.out.println("resolveIUDMessage:"+map+"/"+dtoType);
    if(dtoType.equals(DTOE.DBs))
    {
      aus=new Object[8];
      aus[0]=dbAction;
      aus[1]=dtoType;
      DBsDTO dbsDTO=(DBsDTO)dto;
      DBsEDTO dbsed=dbsDTO.getDbsed();
      DBsEDTO dbseg=dbsDTO.getDbseg();
      DBiFDTO dbif=dbsDTO.getDbif();
      if((dbsed==null)||(dbseg==null)||(dbif==null))
      {
        //ist die Verwendung von dbmaster hier gut? Kostet das nicht immer wieder Netzverkehr?
        dbsDTO.setDTOSubStructure(dbmaster);
        dbsed=dbsDTO.getDbsed();
        dbseg=dbsDTO.getDbseg();
        dbif=dbsDTO.getDbif();
      }
      if(dbsed!=null)
      {
        DBiDTO dbidto=dbsed.getI();
        if(dbidto==null)
        {
          //dbmaster-Netzverkehr?
          dbsed.setDTOSubStructure(dbmaster);
          dbidto=dbsed.getI();
        }
        if(dbidto!=null)
        {
          Long id=dbidto.getId();
          Long sd=dbsed.getS();
          aus[2]=id;
          aus[3]=sd;
          DBiDTO dbidtog=dbseg.getI();
          if(dbidtog==null)
          {
            //dbmaster-Netzverkehr?
            dbseg.setDTOSubStructure(dbmaster);
            dbidtog=dbseg.getI();
          }
          if(dbidtog!=null)
          {
            Long ig=dbidtog.getId();
            Long sg=dbseg.getS();
            String xmlString=dbsDTO.getXmlString();
            aus[4]=ig;
            aus[5]=sg;
            aus[6]=dbif.getF();
            aus[7]=xmlString;
          }
        }
      }
     }
     else
     {
       if(dtoType.equals(DTOE.DBi))
       {
         //noch zu machen
        }
        else
        {
          if(dtoType.equals(DTOE.DBiF))
          {
            //noch zu machen
           }
           else
           {
             //weitere noch zu machen
          }
       }
    }
    return aus;
  }
}
