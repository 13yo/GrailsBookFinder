package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.Serializable;
import java.util.Hashtable;

public class Triple implements StackSorterTriple,Serializable
{
  protected String sortString;
  protected Long itemSet;
  protected Long successorSet;
  protected Hashtable<String,Serializable> itemInfo;
  
  public Triple(String sortString,Long itemSet,Long successorSet,Hashtable<String,Serializable> itemInfo)
  {
    this.sortString=sortString;
    this.itemSet=itemSet;
    this.successorSet=successorSet;
    this.itemInfo=itemInfo;
  }
  
  public Triple(Long itemSet)
  {
    this.itemSet=itemSet;
  }
  
  public boolean equals(Object triple)
  {
//    System.out.println("Triple.equals:"+itemSet+"/"+triple);
    if(!(triple instanceof Triple))
    {
      if(triple instanceof Long)
      {
        return (itemSet.equals((Long)triple));
      }
      return false;
    }
    return itemSet.equals(((Triple)triple).getItemSet());
  }
  
  public int hashCode()
  {
    return itemSet.hashCode();
  }

  @Override
  public void setSortString(String sortString)
  {
    this.sortString=sortString;
  }

  @Override
  public String getSortString()
  {
    return sortString;
  }

  @Override
  public void setItemSet(long itemSet)
  {
    this.itemSet=itemSet;
  }

  @Override
  public Long getItemSet()
  {
    return itemSet;
  }

  @Override
  public void setItemInfo(Hashtable<String,Serializable> itemInfo)
  {
    this.itemInfo=itemInfo;
  }

  @Override
  public Hashtable<String,Serializable> getItemInfo()
  {
    return itemInfo;
  }

  @Override
  public void setSuccessorSet(long successorSet)
  {
    this.successorSet=successorSet;
  }

  @Override
  public Long getSuccessorSet()
  {
    return successorSet;
  }
}
