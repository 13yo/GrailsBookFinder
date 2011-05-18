package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.util.LinkedList;

public interface StackSorterUse extends StackSorter
{
  public TripleList getBetweens(long itemSetFirst,long itemSetLast) throws Exception; 
  
  public Object[] getShowInfo(int horizonItem) throws Exception;
  
  public Long getFirstRegalItemSet(int ebene,int regal) throws Exception;
}
