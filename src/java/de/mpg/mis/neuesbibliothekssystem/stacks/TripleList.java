package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.util.LinkedList;
import java.util.ListIterator;

public class TripleList extends LinkedList<Triple>
{
  public static final long serialVersionUID=3506968045565519564l;
  
  public TripleList()
  {
    super();
  }
  
  public int indexOf(Long was)
  {
    return super.indexOf(new Triple(was));
  }
  
  public boolean contains(Long was)
  {
    return super.contains(new Triple(was));
  }
  
  public void add(int index,Triple was)
  {
    super.add(index,was);
    if(index>0)
    {
      Triple vor=get(index-1);
      Long vorSuccessor=vor.getSuccessorSet();
      vor.setSuccessorSet(was.getItemSet());
//      was.setSuccessorSet(vorSuccessor);
    }
  }
  
  public boolean remove(Triple was)
  {
    int index=super.indexOf(was);
    if(index>0)
    {
      Triple weg=get(index);
      Triple vor=get(index-1);
      vor.setSuccessorSet(weg.getSuccessorSet());
    }
    return super.remove(was);    
  }
  
  public boolean remove(Long was)
  {
    return remove(new Triple(was));
  }
  
  public String toString()
  {
    ListIterator<Triple> li=listIterator();
    StringBuffer aus=new StringBuffer();
    while(li.hasNext())
    {
      Triple triple=li.next();
      aus.append(triple.getItemSet()+"/"+triple.getSortString()+"/"+triple.getSuccessorSet()+",");
    }
    return aus.toString();
  }
}
