package de.mpg.mis.neuesbibliothekssystem.stacks;

public interface StackSorterMaintain extends StackSorter
{
  public Long insertSortedItem(String sortString,String infoXML,int horizonItemNumber) throws Exception;
  
  public void removeSortedItem(long itemSet) throws Exception;
  
  public void setCheckForUpdate(boolean checkForUpdate);
  
  public boolean getCheckForUpdate();
  
}
