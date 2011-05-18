package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.endpoint.EventDrivenConsumer;

import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.DBAction;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DTOE;

public class StackSorterUseImpl extends StackSorterImpl implements
	StackSorterUse, MessageHandler, Runnable {
    protected TripleList betweens = new TripleList();
    protected Long itemSetFirst;
    protected String sortStringFirst;
    protected Long itemSetLast;
    protected String sortStringLast;
    protected IUDMessageResolver iudMessageResolver = new IUDMessageResolver();
    private transient boolean handled = true;
    private transient Message<?> handledMessage;

    protected JButton actioneer;

    public StackSorterUseImpl() throws Exception {
	super();
	// dbmasterIUDPubsubChannel.subscribe(this);
    }

    public void subscribe() throws Exception {
	// dbmasterIUDPubsubChannel.subscribe(this);
	EventDrivenConsumer consumer = new EventDrivenConsumer(
		dbmasterIUDPubsubChannel, this);
	consumer.start();
	// init();
    }

    /*
     * protected void onExit() throws Exception { super.onExit();
     * 
     * }
     */
    protected void furtherInit() throws Exception {
	doSince(lastInit);
    }

    public void setActioneer(JButton actioneer) {
	this.actioneer = actioneer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TripleList getBetweens(long itemSetFirst, long itemSetLast)
	    throws Exception {
	if ((this.itemSetFirst == null) || (this.itemSetLast == null)
		|| (this.itemSetFirst != itemSetFirst)
		|| (this.itemSetLast != itemSetLast)) {
	    this.itemSetFirst = itemSetFirst;
	    this.itemSetLast = itemSetLast;
	    betweens = new TripleList();
	    int first = triples.indexOf(itemSetFirst);
	    sortStringFirst = triples.get(first).getSortString();
	    int last = triples.indexOf(itemSetLast);
	    sortStringLast = triples.get(last).getSortString();
	    for (int a = first; a < last + 1; a++) {
		betweens.add(triples.get(a));
		// betweens.addFirst(triples.get(a));
	    }
	}
	return (TripleList) betweens.clone();
    }

    public long[] getPosition(int item) throws Exception {
	return getPosition(getItemSet(item));
    }

    public long[] getPosition(long itemSet) throws Exception {
	String string = "erstesBuchAufBrett";
	long[] aus = new long[6];
	long position = 0l;
	long positionDa = 0l;
	ListIterator<Triple> li = triples
		.listIterator(triples.indexOf(itemSet));
	boolean weiter = true;
	Triple triple = null;
	Hashtable<String, Serializable> info = null;
	while (weiter && li.hasPrevious())
	// while(weiter&&li.hasNext())
	{
	    triple = li.previous();
	    // triple=li.next();
	    info = triple.getItemInfo();
	    weiter = (info == null) || !info.containsKey(string);
	    String status = (String) info.get("status");
	    if ((status != null) && status.equals("i")) {
		positionDa = positionDa + 1;
	    }
	    position = position + 1;
	}
	if (!weiter) {
	    aus[0] = triple.getItemSet();
	    aus[4] = positionDa;
	    aus[5] = position;
	    Hashtable<String, Serializable> ebab = (Hashtable<String, Serializable>) triple
		    .getItemInfo().get(string);
	    aus[1] = Long.parseLong((String) ebab.get("ebene"));
	    aus[2] = Long.parseLong((String) ebab.get("regal"));
	    aus[3] = Long.parseLong((String) ebab.get("brett"));
	    System.out.println("shelf " + aus[2] + " - board " + aus[3]
		    + " - no " + aus[4]);
	}
	return aus;
    }

    public TripleList getErsteBuecherAufRegalbrettern(long itemSet)
	    throws Exception {
	int index = ebabTriples.indexOf(itemSet);
	Triple ebabTriple = ebabTriples.get(index);
	Hashtable<String, Serializable> ebabInfos = ebabTriple.getItemInfo();
	int ebene = (Integer) ebabInfos.get("ebene");
	int regal = (Integer) ebabInfos.get("regal");
	int brett = (Integer) ebabInfos.get("brett");
	TripleList aus = new TripleList();
	ListIterator<Triple> li = ebabTriples.listIterator(index);
	boolean weiter = true;
	while (weiter && li.hasPrevious())
	// while(weiter&&li.hasNext())
	{
	    Triple triple = li.previous();
	    // Triple triple=li.next();
	    Hashtable<String, Serializable> tripleInfos = triple.getItemInfo();
	    int ebeneAkt = (Integer) tripleInfos.get("ebene");
	    int regalAkt = (Integer) tripleInfos.get("regal");
	    weiter = (ebeneAkt == ebene) && (regalAkt == regal);
	    // aus.addFirst(triple);
	    aus.add(triple);
	}
	if (!weiter) {
	    // aus.removeFirst();
	    aus.removeLast();
	}
	li = ebabTriples.listIterator(index);
	weiter = true;
	while (weiter && li.hasNext())
	// while(weiter&&li.hasPrevious())
	{
	    Triple triple = li.next();
	    // Triple triple=li.previous();
	    Hashtable<String, Serializable> tripleInfos = triple.getItemInfo();
	    int ebeneAkt = (Integer) tripleInfos.get("ebene");
	    int regalAkt = (Integer) tripleInfos.get("regal");
	    weiter = (ebeneAkt == ebene) && (regalAkt == regal);
	    // aus.add(triple);
	    aus.addFirst(triple);
	}
	if (!weiter) {
	    aus.removeFirst();
	}
	return aus;
    }

    @Override
    public Long getFirstRegalItemSet(int ebene, int regal) throws Exception {
	ListIterator<Triple> li = ebabTriples.listIterator();
	boolean weiter = true;
	Long aus = null;
	while (weiter && li.hasNext()) {
	    Triple akt = li.next();
	    Hashtable<String, Serializable> itemInfo = akt.getItemInfo();
	    Integer ebeneAkt = (Integer) itemInfo.get("ebene");
	    Integer regalAkt = (Integer) itemInfo.get("regal");
	    long item = akt.getItemSet();
	    if ((ebeneAkt != null) && (regalAkt != null)
		    && (ebeneAkt.intValue() == ebene)
		    && (regalAkt.intValue() == regal)) {
		weiter = false;
		aus = item;
	    }
	}
	return aus;
    }

    public Object[] getShowInfo(long itemSet) throws Exception {
	System.out.println(itemSet);
	Object[] aus = new Object[3];
	long[] position = getPosition(itemSet);
	if (position != null) {
	    aus[0] = position;
	    for (int a = 0; a < position.length; a++) {
		System.out.println(a + ":" + position[a]);
	    }
	    TripleList ebabTrips = getErsteBuecherAufRegalbrettern(position[0]);
	    aus[1] = ebabTrips;
	    /*
	     * ListIterator<Triple>
	     * li=triples.listIterator(triples.indexOf(ebabTrips
	     * .getLast().getItemSet()));
	     * aus[2]=getBetweens(ebabTrips.getFirst()
	     * .getItemSet(),li.previous().getItemSet());
	     */
	    /*
	     * ListIterator<Triple>
	     * li=triples.listIterator(triples.indexOf(ebabTrips
	     * .getFirst().getItemSet()));
	     * aus[2]=getBetweens(li.next().getItemSet
	     * (),ebabTrips.getLast().getItemSet());
	     */

	    ListIterator<Triple> ebabVors = this.ebabTriples
		    .listIterator(this.ebabTriples.indexOf(ebabTrips.getLast()));
	    Triple was = null;
	    System.out.println("ebabTest:" + ebabTrips.getLast().getItemSet());
	    // ebabVors.previous();
	    if (ebabVors.hasPrevious()) {
		Triple ebabVor = ebabVors.previous();
		System.out.println("hasNext:" + ebabVor.getItemSet());
		ListIterator<Triple> li = triples.listIterator(triples
			.indexOf(ebabVor));
		was = li.previous();
	    } else {
		System.out.println("last");
		was = triples.getLast();
	    }
	    System.out.println(ebabTrips.getFirst().getItemSet() + "/"
		    + was.getItemSet());
	    aus[2] = getBetweens(ebabTrips.getFirst().getItemSet(),
		    was.getItemSet());
	}
	// System.exit(0);
	return aus;
    }

    @Override
    public Object[] getShowInfo(int horizonItemNumber) throws Exception {
	return getShowInfo(getItemSet(horizonItemNumber));
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
	boolean handled = false;
	try {
	    Object[] obs = iudMessageResolver.resolveIUDMessage(message,
		    dbmaster);
	    // obs=null;
	    System.out.println("StackSorterUseImpl.handleMessage:" + obs);
	    if (obs != null) {
		Long id = (Long) obs[2];
		Long sd = (Long) obs[3];
		Long ig = (Long) obs[4];
		Long sg = (Long) obs[5];
		Long f = (Long) obs[6];
		String xmlString = (String) obs[7];
		DBAction dbAction = (DBAction) obs[0];
		DTOE dtoe = (DTOE) obs[1];
		System.out.println("StackSorterUseImpl.handleMessage2:"
			+ dbAction + "/" + dtoe + "/" + id + "("
			+ itemIdentifier + ")/" + sd + "/" + ig + "("
			+ sorterIdentifier + "/" + itemInfoIdentifier + ")"
			+ sg + "/" + f + "(" + sorterFunction + "/"
			+ itemInfoFunction + ")/" + xmlString);
		System.out
			.println("StackSorterUseImpl.handleMessage2a:"
				+ dtoe.equals(DTOE.DBs)
				+ "/"
				+ (id.longValue() == itemIdentifier.longValue())
				+ "/"
				+ ((ig.longValue() == sorterIdentifier
					.longValue()) && (f.longValue() == sorterFunction
					.longValue()))
				+ "/"
				+ ((ig.longValue() == itemInfoIdentifier
					.longValue()) && (f.longValue() == itemInfoFunction
					.longValue())));
		if ((dtoe.equals(DTOE.DBs)
			&& (id.longValue() == itemIdentifier.longValue()) && (((ig
			.longValue() == sorterIdentifier.longValue()) && (f
			.longValue() == sorterFunction.longValue())) || ((ig
			.longValue() == itemInfoIdentifier.longValue()) && (f
			.longValue() == itemInfoFunction.longValue()))))) {
		    System.out.println("StackSorterUseImpl.handleMessage3");
		    if (betweens.contains(sd)) {
			// delete oder update, oder einfach:
			System.out
				.println("StackSorterUseImpl.handleMessage4a: contains: refresh");
			refreshBetweens(message);
			handled = true;
		    } else {
			System.out
				.println("StackSorterUseImpl.handleMessage4b: doesn't contain");
			System.out
				.println("StackSorterUseImpl.handleMessage4c: "
					+ dbAction
					+ "/"
					+ (ig.longValue() == sorterIdentifier
						.longValue())
					+ "/"
					+ (f.longValue() == sorterFunction
						.longValue()) + "/"
					+ ig.equals(itemInfoIdentifier) + "/"
					+ f.equals(itemInfoFunction));
			if (dbAction.equals(DBAction.STATUSQUO)
				&& ((ig.longValue() == sorterIdentifier
					.longValue()) && (f.longValue() == sorterFunction
					.longValue() || (1 == 2)
					&& (ig.equals(itemInfoIdentifier) && (f
						.equals(itemInfoFunction)))))) {
			    System.out
				    .println("StackSorterUseImpl.handleMessage5: sort insert: "
					    + sortStringFirst
					    + "/"
					    + xmlString
					    + "/" + sortStringLast);
			    if (sortStringFirst != null
				    && xmlString != null
				    && (sortStringFirst.compareTo(xmlString) <= 0)
				    && (xmlString.compareTo(sortStringLast) <= 0)) {
				// insert, oder einfach:
				System.out
					.println("StackSorterUseImpl.handleMessage6: refresh");
				refreshBetweens(message);
				handled = true;
			    }
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new MessagingException(message);
	}
	System.out.println("StackSorterUseImpl.handleMessage7:" + handled);
	if (!handled) {
	    handledMessage = message;
	    if (1 == 0) {
		Thread thread = new Thread(this);
		thread.start();
	    } else {
		run();
	    }
	}
    }

    public void run() {
	super.handleMessage(handledMessage);
	handledMessage = null;
	handled = true;
    }

    protected void refreshBetweens(Message<?> message) throws Exception {
	super.handleMessage(message);
	Long itemSetFirstZwi = itemSetFirst;
	itemSetFirst = null;
	getBetweens(itemSetFirstZwi, itemSetLast);
	handled = true;
	System.out.println("betweens refreshed");
	if (actioneer != null) {
	    actioneer.doClick();
	}
    }
}