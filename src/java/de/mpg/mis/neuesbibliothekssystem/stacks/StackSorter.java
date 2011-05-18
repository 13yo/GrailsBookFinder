package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;

public interface StackSorter extends UseDBMaster {
    public void setSorterIdentifier(Long sorterIdentifier);

    public Long getSorterIdentifier();

    public void setItemIdentifier(Long itemIdentifier);

    public Long getItemIdentifier();

    public void setSorterFunction(Long sorterFunction);

    public Long getSorterFunction();

    public void setSuccessorFunction(Long successorFunction);

    public Long getSuccessorFunction();

    public void setItemInfoIdentifier(Long itemInfoIdentifier);

    public Long getItemInfoIdentifier();

    public void setItemInfoFunction(Long itemInfoFunction);

    public Long getItemInfoFunction();

    public void setItemNumberIdentifier(Long itemNumberIdentifier);

    public Long getItemNumberIdentifier();

    public void setItemNumberFunction(Long itemNumberFunction);

    public Long getItemNumberFunction();

    public Hashtable<String, Serializable> getItemInfo(long itemSet)
	    throws Exception;

    public int compareSorterStrings(String sorter1, String sorter2);

    public void init() throws Exception;

    public void exit() throws Exception;

    // Zusätzliche Getter für den Webservice
    public abstract TripleList getTriples();

    public abstract TripleList getEbabTriples();
}
