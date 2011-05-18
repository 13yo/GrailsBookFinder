package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.Serializable;
import java.util.Hashtable;

public interface StackSorterTriple
{
  public void setSortString(String sortString);
  
  public String getSortString();
  
  public void setItemSet(long itemSet);
  
  public Long getItemSet();
  
  public void setSuccessorSet(long successorSet);
  
  public Long getSuccessorSet();
  
  public void setItemInfo(Hashtable<String,Serializable> itemInfo);
  
  public Hashtable<String,Serializable> getItemInfo();
}
