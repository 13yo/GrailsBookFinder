package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT;

public class SpineGetterSorterImpl implements SpineGetter {
    protected StackSorterUse stackSorterUse;
    protected LinkedList<SpineRepresentation> spines;
    protected LinkedList<BoardInformation> boards;
    protected Integer item;
    public long[] lastPosition;

    public SpineGetterSorterImpl(StackSorterUse stackSorterUse)
	    throws Exception {
	this.stackSorterUse = stackSorterUse;
	// this.stackSorterUse.init();
    }

    public StackSorterUse getStackSorter() {
	return stackSorterUse;
    }

    @Override
    public void setConnection(ConnectionT connection) {
    }

    @Override
    public void setLocationBorders(int floorFrom, int shelfFrom, int boardFrom,
	    int floorTo, int shelfTo, int boardTo) throws Exception {
	if (shelfFrom == 0) {
	    item = floorFrom;
	    setLocationBordersByItem(item);
	    /*
	     * System.out.println("item:"+item); Object[]
	     * obs=stackSorterUse.getShowInfo(item); System.out.println("ok.");
	     * // Object[] obs=(Object[])ob; long[] position=(long[])obs[0];
	     * LinkedList<Triple> ebabTriples=(LinkedList<Triple>)obs[1];
	     * LinkedList<StackSorterTriple>
	     * triples=(LinkedList<StackSorterTriple>)obs[2];
	     * makeSpines(triples); makeBoards(ebabTriples);
	     */
	} else {
	    lastPosition = null;
	    if (boardFrom == 0) {
		Long itemSet = stackSorterUse.getFirstRegalItemSet(floorFrom,
			shelfFrom);
		if (itemSet != null) {
		    Hashtable<String, Serializable> itemInfo = stackSorterUse
			    .getItemInfo(itemSet);
		    if (itemInfo != null) {
			String itemString = (String) itemInfo.get("item");
			if (itemString != null) {
			    setLocationBordersByItem(Integer
				    .parseInt(itemString));
			}
		    }
		}
	    } else {
		// noch machen.
	    }
	}
    }

    protected void setLocationBordersByItem(int item) throws Exception {
	System.out.println("item:" + item);
	Object[] obs = stackSorterUse.getShowInfo(item);
	System.out.println("ok.");
	// Object[] obs=(Object[])ob;
	lastPosition = (long[]) obs[0];
	LinkedList<Triple> ebabTriples = (LinkedList<Triple>) obs[1];
	LinkedList<StackSorterTriple> triples = (LinkedList<StackSorterTriple>) obs[2];
	makeSpines(triples);
	makeBoards(ebabTriples);
    }

    protected void makeSpines(LinkedList<StackSorterTriple> triples) {
	System.out.println("makeSpines");
	spines = new LinkedList<SpineRepresentation>();
	ListIterator<StackSorterTriple> li = triples.listIterator();
	while (li.hasNext()) {
	    StackSorterTriple triple = li.next();
	    Hashtable<String, Serializable> hash = triple.getItemInfo();
	    if (hash != null) {
		String itemS = (String) hash.get("item");
		if (itemS != null) {
		    Integer itemAkt = Integer.parseInt(itemS);
		    boolean isSpecial = itemAkt.equals(item);
		    System.out.println(itemAkt);
		    // SpineRepresentationImpl spine=new
		    // SpineRepresentationImpl(new
		    // BibliographicStatusSorterImpl(itemAkt,(String)hash.get("ibarcode"),(String)hash.get("status"),(String)hash.get("collection")),isSpecial,false);
		    SpineRepresentationImpl spine = new SpineRepresentationImpl(
			    new BibliographicStatusSorterImpl(
				    Integer.parseInt((String) hash.get("bib")),
				    (String) hash.get("ibarcode"),
				    (String) hash.get("status"),
				    (String) hash.get("collection")),
			    isSpecial, false);
		    spines.add(spine);
		} else {
		    System.out
			    .println("SpineGetterSorterImp.makeSpines: item null.");
		}
	    } else {
		System.out
			.println("SpineGetterSorterImp.makeSpines: hash null.");
	    }
	}
    }

    protected void makeBoards(LinkedList<Triple> triples) {
	boards = new LinkedList<BoardInformation>();
	ListIterator<Triple> li = triples.listIterator();
	while (li.hasNext()) {
	    StackSorterTriple triple = li.next();
	    Hashtable<String, Serializable> hash = triple.getItemInfo();
	    String name = hash.get("ebene") + ":" + hash.get("regal") + ","
		    + hash.get("brett");
	    Triple trip = (Triple) hash.get("triple");
	    Hashtable<String, Serializable> tripHash = trip.getItemInfo();
	    System.out.println("makeBoards:" + name + "/"
		    + tripHash.get("ibarcode") + "/" + hash.get("laengeCm"));
	    BoardInformationImpl board = new BoardInformationImpl(
		    (String) tripHash.get("ibarcode"), name,
		    (Integer) hash.get("laengeCm") * 10, 300);
	    boards.add(board);
	}
    }

    @Override
    public LinkedList<SpineRepresentation> getSpines() {
	return spines;
    }

    @Override
    public LinkedList<BoardInformation> getBoardInformation() {
	return boards;
    }

    @Override
    public void init() {
	// TODO Auto-generated method stub

    }
}
