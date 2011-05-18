package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;


import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DBsDTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DBsEDTO;

public class StackSorterMaintainImpl extends StackSorterImpl implements StackSorterMaintain,MessageHandler
{
  protected boolean checkForUpdate=true;
  
  public StackSorterMaintainImpl() throws Exception
  {
//    init();
  }
  
  public void setCheckForUpdate(boolean checkForUpdate)
  {
    this.checkForUpdate=checkForUpdate;
  }
  
  public boolean getCheckForUpdate()
  {
    return checkForUpdate;
  }
  
  protected Long checkUpdate(int horizonItemNumber) throws Exception
  {
    Long itemSet=getItemSet(horizonItemNumber);
    System.out.println("checkUpdate:"+horizonItemNumber+"/"+itemSet);
    if(itemSet!=null)
    {
      removeSortedItem(itemSet);
    }
    return itemSet;
  }
  
  @Override
  public Long insertSortedItem(String sortString,String infoXML,int horizonItemNumber) throws Exception
  {
    if(sortString==null)
    {
      Long itemSet=getItemSet(horizonItemNumber);
      String hql="select a.dbseg.s from DBs a where a.dbsed.i.id="+itemIdentifier+" and a.dbsed.s="+itemSet+" and a.dbseg.i.id="+itemInfoIdentifier+" and a.dbif.f="+itemInfoFunction;
      List list=dbmaster.parseHQLStatement(hql).get("results");
      if((list!=null)&&(list.size()>0))
      {
        Long infoSet=(Long)list.get(0);
        dbmaster.setSet(itemIdentifier,itemSet,itemInfoIdentifier,infoSet,itemInfoFunction,infoXML);
      }
      return itemSet;
    }
    Long delItem=0l;
    if(checkForUpdate)
    {
      delItem=checkUpdate(horizonItemNumber);
    }
    if(delItem==null)
    {
      delItem=0l;
    }
    ListIterator<Triple> li=triples.listIterator();
    int greater=1;
    Triple alt=null;
    Triple triple=null;
    while(li.hasNext()&&(greater>-1))
    {
      triple=li.next();
      greater=sortString.compareTo(triple.getSortString());
      System.out.println(greater+":\n"+sortString+"\n"+triple.getSortString()+"\n:"+greater);
      if(greater>-1)
      {
        alt=triple;        
      }
    }
//    DBsDTO neu=dbmaster.setSet(itemIdentifier,0l,sorterIdentifier,0l,sorterFunction,sortString);
    DBsDTO neu=dbmaster.setSet(itemIdentifier,delItem,sorterIdentifier,0l,sorterFunction,sortString);
    DBsEDTO neuDbsed=neu.getDbsed();
    Long itemSet=neuDbsed.getS();
    dbmaster.setSet(itemIdentifier,itemSet,itemInfoIdentifier,0l,itemInfoFunction,infoXML);
    dbmaster.setSet(itemIdentifier,itemSet,itemNumberIdentifier,0l,itemNumberFunction,(new Integer(horizonItemNumber)).toString());
    if(alt!=null)
    {
      Long altSuccessorSet=alt.getSuccessorSet();
      Long altItemSet=alt.getItemSet();
      try
      {
//        System.out.println("deleteSet:"+altItemSet+"/"+altSuccessorSet);
        dbmaster.deleteSet(itemIdentifier,altItemSet,itemIdentifier,altSuccessorSet,successorFunction,true);
       }
       catch(Exception ex)
       {
         System.out.println(ex.toString());
         ex.printStackTrace();
      }
      try
      {
//        System.out.println("setSet0:"+altItemSet+"/"+itemSet);
        dbmaster.setSet(itemIdentifier,altItemSet,itemIdentifier,itemSet,successorFunction,null);
        alt.setSuccessorSet(itemSet);
       }
       catch(Exception ex)
       {
         System.out.println(ex.toString());
         ex.printStackTrace();
      }
      if(altSuccessorSet>0l)
      {
        try
        {
//          System.out.println("setSet1:"+itemSet+"/"+altSuccessorSet);
          dbmaster.setSet(itemIdentifier,itemSet,itemIdentifier,altSuccessorSet,successorFunction,null);
         }
         catch(Exception ex)
         {
           System.out.println(ex.toString());
           ex.printStackTrace();
        }
      }
//      int wo=triples.indexOf(alt.getItemSet())+1;
      int wo=triples.indexOf(alt)+1;
//      System.out.println("triples.add: "+wo);
      triples.add(wo,new Triple(sortString,itemSet,altSuccessorSet,null));
//      triples.add(wo,new Triple(sortString,altSuccessorSet,itemSet,null));
     }
     else
     {
       if(triples.size()>0)
       {
         alt=triples.getFirst();
         try
         {
//           dbmaster.setSet(itemIdentifier,alt.getItemSet(),itemIdentifier,itemSet,successorFunction,null);
           dbmaster.setSet(itemIdentifier,itemSet,itemIdentifier,alt.getItemSet(),successorFunction,null);
           triples.addFirst(new Triple(sortString,itemSet,alt.getItemSet(),null));
//           triples.addFirst(new Triple(sortString,alt.getItemSet(),itemSet,null));
          }
          catch(Exception ex)
          {
            System.out.println(ex.toString());
            ex.printStackTrace();
         }
         try
         {
//           dbmaster.setSet(itemIdentifier,itemSet,itemIdentifier,alt.getSuccessorSet(),successorFunction,null);
          }
          catch(Exception ex)
          {
            System.out.println(ex.toString());
            ex.printStackTrace();
         }
        }
        else
        {
          triples.addFirst(new Triple(sortString,itemSet,0l,null));
//          triples.addFirst(new Triple(sortString,0l,itemSet,null));
//          System.out.println("triples.add: "+0);
       }
    }
//    System.out.println("triples.size(): "+triples.size());
    /*
    ListIterator<Triple> taus=triples.listIterator();
    while(taus.hasNext())
    {
      Triple trip=taus.next();
      System.out.println(trip.getSortString()+"\t"+trip.getItemSet()+"\t"+trip.getSuccessorSet());
    }
    */
    return itemSet;
  }

  @Override
  public void removeSortedItem(long itemSet) throws Exception
  {
    Object[] obsVor=getNext(itemSet,false);
    Object[] obsNach=getNext(itemSet,true);
//    System.out.println("deleteNach:"+obsVor.length+"/"+obsNach.length);
    if(obsNach!=null)
    {
//      System.out.println("deleteSet:"+itemIdentifier+"/"+itemSet+"/"+obsNach[0]+"/"+successorFunction);
      dbmaster.deleteSet(itemIdentifier,itemSet,itemIdentifier,(Long)obsNach[0],successorFunction,true);
    }
    System.out.println("deleteVor");
    if(obsVor!=null)
    {
//      System.out.println("deleteSet:"+itemIdentifier+"/"+obsVor[0]+"/"+itemSet+"/"+"/"+successorFunction);
      dbmaster.deleteSet(itemIdentifier,(Long)obsVor[0],itemIdentifier,itemSet,successorFunction,true);
      if(obsNach!=null)
      {
//        System.out.println("setSet:"+itemIdentifier+"/"+obsVor[0]+"/"+obsNach[0]+"/"+successorFunction);
        dbmaster.setSet(itemIdentifier,(Long)obsVor[0],itemIdentifier,(Long)obsNach[0],successorFunction,null);
      }
    }
    System.out.println("successorDeletes ok.");
    List sorterSets=dbmaster.parseHQLStatement("select a.dbseg.s from DBs a where a.dbsed.i.id="+itemIdentifier+" and a.dbsed.s="+itemSet+" and a.dbseg.i.id="+itemInfoIdentifier+" and a.dbif.f="+itemInfoFunction).get("results");
    Iterator it=sorterSets.iterator();
    while(it.hasNext())
    {
      Long sorterSet=(Long)it.next();
      dbmaster.deleteSet(itemIdentifier,itemSet,sorterIdentifier,sorterSet,sorterFunction,true);
    }
    deleteSets(itemSet,sorterIdentifier,sorterFunction);
//    System.out.println(triples);
    triples.remove(itemSet);
//    System.out.println(triples);
    deleteSets(itemSet,itemInfoIdentifier,itemInfoFunction);
    deleteSets(itemSet,itemNumberIdentifier,itemNumberFunction);
  }
  
  protected void deleteSets(long itemSet,long identifierG,long function) throws Exception
  {
    List sets=dbmaster.parseHQLStatement("select a.dbseg.s from DBs a where a.dbsed.i.id="+itemIdentifier+" and a.dbsed.s="+itemSet+" and a.dbseg.i.id="+identifierG+" and a.dbif.f="+function).get("results");
    Iterator it=sets.iterator();
    while(it.hasNext())
    {
      Long set=(Long)it.next();
      dbmaster.deleteSet(itemIdentifier,itemSet,identifierG,set,function,true);
    }
  }
  
  public void handleMessage(Message<?> message) throws MessagingException
  {
    
  }
  
  protected Triple createAndConnectNewTriple(String string,Long sd,Long sdAlt) throws Exception
  {
    Hashtable<String,Serializable> itemInfo=getItemInfo(sd);
    Triple triple=new Triple(string,sd,sdAlt,itemInfo);
    return triple;
  }
}
