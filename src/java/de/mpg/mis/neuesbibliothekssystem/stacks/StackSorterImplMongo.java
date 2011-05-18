package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.support.MessageBuilder;

import de.mpg.mis.neuesbibliothekssystem.stacks.xml.XMLParser;
import de.mpg.mis.neuesbibliothekssystem.stacks.xml.XMLParser.Element;
import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.DBAction;
import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.DBEndpoint;
import de.mpg.mis.neuesbibliothekssystem.dbendpoint.messaging.IUDType;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.DBMaster;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DTO;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.dao.DTOE;

public class StackSorterImplMongo implements StackSorter, MessageHandler {
    protected DBMaster dbmaster;
    protected DBEndpoint dbendpoint;
    protected SubscribableChannel dbmasterIUDPubsubChannel;
    protected Long sorterIdentifier;
    protected Long sorterFunction;
    protected Long successorFunction;
    protected Long itemIdentifier;
    protected Long itemInfoIdentifier;
    protected Long itemInfoFunction;
    protected Long itemNumberIdentifier;
    protected Long itemNumberFunction;
    /*
     * protected LinkedList<Triple> triples=new LinkedList<Triple>(); protected
     * LinkedList<Triple> ebabTriples=new LinkedList<Triple>();
     */
    protected TripleList triples = new TripleList();

    @Override
    public TripleList getTriples() {
	return this.triples;
    }

    protected TripleList ebabTriples = new TripleList();

    @Override
    public TripleList getEbabTriples() {
	return this.ebabTriples;
    }

    protected IUDMessageResolver iudMessageResolver = new IUDMessageResolver();

    protected Date lastInit;

    @Value("${DBMaster.lists.base}")
    protected String fileBase;

    protected File fileTriples;
    protected File fileTriplesEbab;
    protected File fileDate;

    public StackSorterImplMongo() throws Exception {
	// init();
    }

    public void subscribe() throws Exception {
	dbmasterIUDPubsubChannel.subscribe(this);
	// init();
    }

    public void init() throws Exception {
	Long dbids = dbmaster.getDBiNumber();
	System.out.println("DBis:" + dbids);
	lastInit = new Date();
	String name = fileBase + getClass().getName();
	fileDate = new File(name + "Date.ser");
	if (fileDate.exists()) {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    fileDate));
	    lastInit = (Date) ois.readObject();
	    ois.close();
	}
	// lastInit=new Date(0);
	fileTriples = new File(name + "Triples.ser");
	fileTriplesEbab = new File(name + "TriplesEbab.ser");
	if (fileTriples.exists() && fileTriplesEbab.exists() && (1 == 1)) {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    fileTriples));
	    triples = (TripleList) ois.readObject();
	    ois.close();
	    // System.out.println("StackSorterImpl.init.triples:"+triples);
	    ois = new ObjectInputStream(new FileInputStream(fileTriplesEbab));
	    ebabTriples = (TripleList) ois.readObject();
	    ois.close();
	    furtherInit();
	    // doSince geht leider im Moment noch nicht
	    // doSince(lastInit);
	} else {
	    firstTriplesFilling();
	    // eigentlich beim Zumachen erst serialisieren
	    // onExit();
	}
    }

    protected void furtherInit() throws Exception {

    }

    protected void doSince(Date since) throws Exception {
	System.out.println(getClass() + ":doSince gestartet:" + since + "/"
		+ since.getTime());
	if ((1 == 1) && (dbendpoint != null)) {
	    // System.out.println(dbendpoint.getDBids());
	    // System.out.println(dbendpoint.getDBids());

	    Map<String, List> mapp = dbendpoint.doSince(since, null);
	    System.out.println(mapp);
	    List<String> actions = mapp.get("actions");
	    List<DTO> dbObjects = mapp.get("dtos");
	    System.out.println("doSince:list.size:" + actions.size());
	    Iterator<String> liAction = actions.iterator();
	    Iterator<DTO> liDbObject = dbObjects.iterator();
	    while (liAction.hasNext()) {
		DBAction dbaction = DBAction.valueOf((String) liAction.next());
		DTO dbObject = liDbObject.next();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("action", dbaction);
		map.put("dbObject", dbObject);
		System.out.println("Map:" + map);
		Message message = MessageBuilder.withPayload(map).build();
		System.out.println("Message:" + message);
		handleMessage(message);
	    }
	} else {
	    System.out.println("Achtung: dbendpoint==null.");
	}
	System.out.println("doSince fertig.");
	// System.exit(0);
    }

    public void exit() throws Exception {
	onExit();
	// System.exit(0);
    }

    protected void onExit() throws Exception {
	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
		fileTriples));
	// System.out.println("serialisiere triples:\n"+triples);
	oos.writeObject(triples);
	oos.close();
	System.out.println("serialisiere ebabTriples");
	oos = new ObjectOutputStream(new FileOutputStream(fileTriplesEbab));
	oos.writeObject(ebabTriples);
	oos.close();
	System.out.println("serialisiere Date");
	lastInit = new Date();
	oos = new ObjectOutputStream(new FileOutputStream(fileDate));
	oos.writeObject(lastInit);
	oos.close();
    }

    // public class TripleList extends LinkedList<Triple>
    // {
    // public TripleList()
    // {
    // super();
    // }
    //
    // public int indexOf(Long was)
    // {
    // return super.indexOf(new Triple(was));
    // }
    // }

    // public class Triple implements StackSorterTriple,Serializable
    // {
    // protected String sortString;
    // protected Long itemSet;
    // protected Long successorSet;
    // protected Hashtable<String,Serializable> itemInfo;
    //
    // public Triple(String sortString,Long itemSet,Long
    // successorSet,Hashtable<String,Serializable> itemInfo)
    // {
    // this.sortString=sortString;
    // this.itemSet=itemSet;
    // this.successorSet=successorSet;
    // this.itemInfo=itemInfo;
    // }
    //
    // public Triple(Long itemSet)
    // {
    // this.itemSet=itemSet;
    // }
    //
    // public boolean equals(Object triple)
    // {
    // // System.out.println("Triple.equals:"+itemSet+"/"+triple);
    // if(!(triple instanceof Triple))
    // {
    // if(triple instanceof Long)
    // {
    // return (itemSet.equals((Long)triple));
    // }
    // return false;
    // }
    // return itemSet.equals(((Triple)triple).getItemSet());
    // }
    //
    // public int hashCode()
    // {
    // return itemSet.hashCode();
    // }
    //
    // @Override
    // public void setSortString(String sortString)
    // {
    // this.sortString=sortString;
    // }
    //
    // @Override
    // public String getSortString()
    // {
    // return sortString;
    // }
    //
    // @Override
    // public void setItemSet(long itemSet)
    // {
    // this.itemSet=itemSet;
    // }
    //
    // @Override
    // public Long getItemSet()
    // {
    // return itemSet;
    // }
    //
    // @Override
    // public void setItemInfo(Hashtable<String,Serializable> itemInfo)
    // {
    // this.itemInfo=itemInfo;
    // }
    //
    // @Override
    // public Hashtable<String,Serializable> getItemInfo()
    // {
    // return itemInfo;
    // }
    //
    // @Override
    // public void setSuccessorSet(long successorSet)
    // {
    // this.successorSet=successorSet;
    // }
    //
    // @Override
    // public Long getSuccessorSet()
    // {
    // return successorSet;
    // }
    // }

    @Override
    public void setDBMaster(DBMaster dbmaster) {
	this.dbmaster = dbmaster;
    }

    @Override
    public DBMaster getDBMaster() {
	return dbmaster;
    }

    @Override
    public void setDBMasterIUDPubsubChannel(SubscribableChannel channel) {
	this.dbmasterIUDPubsubChannel = channel;
    }

    @Override
    public SubscribableChannel getDBMasterIUDPubsubChannel() {
	return dbmasterIUDPubsubChannel;
    }

    @Override
    public void setSorterIdentifier(Long sorterIdentifier) {
	this.sorterIdentifier = sorterIdentifier;
    }

    @Override
    public Long getSorterIdentifier() {
	return sorterIdentifier;
    }

    @Override
    public void setSuccessorFunction(Long successorFunction) {
	this.successorFunction = successorFunction;
    }

    @Override
    public Long getSuccessorFunction() {
	return successorFunction;
    }

    @Override
    public void setItemIdentifier(Long itemIdentifier) {
	this.itemIdentifier = itemIdentifier;
    }

    @Override
    public Long getItemIdentifier() {
	return itemIdentifier;
    }

    protected void firstTriplesFilling() throws Exception {
	String hql = "select min(a.dbsed.s) from DBs a where dbsed.i.id="
		+ itemIdentifier + " and dbseg.i.id=" + itemIdentifier
		+ " and dbif.f=" + successorFunction;
	System.out.println(hql);
	Map<String, List> map = dbmaster.parseHQLStatement(hql);
	List ergs = map.get("results");
	System.out.println(ergs.size());
	Long erst = null;
	if ((ergs != null) && (ergs.size() > 0)) {
	    System.out.println(ergs);
	    erst = (Long) ergs.get(0);
	}
	// Long merk=null;
	Long merk = erst;
	System.out.println("erst:" + erst);
	while (erst != null) {
	    // Object[] obs=getNext(erst,false);
	    Object[] obs = getNext(erst, true);
	    if (obs != null) {
		erst = (Long) obs[0];
		/*
		 * if(merk==null) { merk=erst; }
		 */
		triples.add(createAndConnectNewTriple((String) obs[1],
			(Long) obs[2], (Long) obs[3]));
	    } else {
		hql = "select xmlString from DBs a where a.dbsed.i.id="
			+ itemIdentifier + " and a.dbseg.i.id="
			+ sorterIdentifier + " and a.dbif.f=" + sorterFunction
			+ " and a.dbsed.s=" + erst;
		map = dbmaster.parseHQLStatement(hql);
		ergs = map.get("results");
		if ((ergs != null) && (ergs.size() > 0)) {
		    String sort = (String) ergs.get(0);
		    triples.add(createAndConnectNewTriple(sort, erst, 0l));
		}
		erst = null;
	    }
	}
	erst = merk;
	// merk=null;
	while (erst != null) {
	    // Object[] obs=getNext(erst,true);
	    Object[] obs = getNext(erst, false);
	    if (obs != null) {
		if (merk != erst) {
		    /*
		     * if(merk==null) { merk=erst; }
		     */
		    Triple triple = createAndConnectNewTriple((String) obs[1],
			    (Long) obs[2], (Long) obs[3]);
		    triples.addFirst(triple);
		}
		erst = (Long) obs[0];
	    } else {
		hql = "select a.dbsed.s,b.xmlString,a.dbseg.s from DBs a,DBs b where a.dbsed.i.id="
			+ itemIdentifier
			+ " and a.dbseg.i.id=a.dbsed.i.id and a.dbif.f="
			+ successorFunction
			+ " and a.dbsed.s="
			+ erst
			+ " and b.dbsed=a.dbsed and b.dbseg.i.id="
			+ sorterIdentifier + " and b.dbif.f=" + sorterFunction;
		System.out.println("FirstFilling:" + hql);
		map = dbmaster.parseHQLStatement(hql);
		ergs = map.get("results");
		if ((ergs != null) && (ergs.size() > 0)) {
		    obs = (Object[]) ergs.get(0);
		    String sort = (String) obs[1];
		    Long sd = (Long) obs[0];
		    Long sg = (Long) obs[2];
		    System.out.println("FirstFilling2:" + sort + "/" + sd + "/"
			    + sg);
		    triples.add(0, createAndConnectNewTriple(sort, sd, sg));
		}
		erst = null;
	    }
	}
    }

    protected Triple createAndConnectNewTriple(String string, Long sd,
	    Long sdAlt) throws Exception {
	Hashtable<String, Serializable> itemInfo = getItemInfo(sd);
	Triple triple = new Triple(string, sd, sdAlt, itemInfo);
	// System.out.println("triple created:"+triple.getSortString()+"/"+triple.getItemSet()+"/"+triple.getSuccessorSet());
	connectEbab(triple);
	return triple;
    }

    protected void connectEbab(Triple triple) throws Exception {
	Long sd = triple.getItemSet();
	Hashtable<String, Serializable> itemInfo = triple.getItemInfo();
	Hashtable<String, Serializable> ebab = (Hashtable<String, Serializable>) itemInfo
		.get("erstesBuchAufBrett");
	if (ebab != null) {
	    String ebene = intToSortString(Integer.parseInt((String) ebab
		    .get("ebene")));
	    String regal = intToSortString(Integer.parseInt((String) ebab
		    .get("regal")));
	    String brett = intToSortString(Integer.parseInt((String) ebab
		    .get("brett")));
	    String laenge = intToSortString(Integer.parseInt((String) ebab
		    .get("laengeCm")));
	    String sortString = ebene + (char) 31 + regal + (char) 31 + brett;
	    Hashtable<String, Serializable> itemInfoEbab = new Hashtable<String, Serializable>();
	    itemInfoEbab.put("triple", triple);
	    /*
	     * itemInfoEbab.put("ebene",Integer.parseInt(ebene));
	     * itemInfoEbab.put("regal",Integer.parseInt(regal));
	     * itemInfoEbab.put("brett",Integer.parseInt(brett));
	     * itemInfoEbab.put("laengeCm",Integer.parseInt(laenge));
	     */
	    itemInfoEbab.put("ebene",
		    Integer.parseInt((String) ebab.get("ebene")));
	    itemInfoEbab.put("regal",
		    Integer.parseInt((String) ebab.get("regal")));
	    itemInfoEbab.put("brett",
		    Integer.parseInt((String) ebab.get("brett")));
	    itemInfoEbab.put("laengeCm",
		    Integer.parseInt((String) ebab.get("laengeCm")));

	    Triple tripleEbab = sortIn(ebabTriples, sortString, sd,
		    itemInfoEbab);
	    itemInfo.put("triple", tripleEbab);
	}
    }

    public Hashtable<String, Serializable> getItemInfo(long itemSet)
	    throws Exception {
	Hashtable<String, Serializable> aus = null;
	String hql = "select a.xmlString from DBs a where a.dbsed.i.id="
		+ itemIdentifier + " and a.dbsed.s=" + itemSet
		+ " and a.dbseg.i.id=" + itemInfoIdentifier + " and a.dbif.f="
		+ itemInfoFunction;
	System.out.println(hql);
	String xmlString = null;
	List list = dbmaster.parseHQLStatement(hql).get("results");
	if ((list != null) && (list.size() > 0)) {
	    xmlString = (String) list.get(0);
	}
	if (xmlString != null) {
	    aus = getItemInfo(xmlString);
	}
	return aus;
    }

    protected String intToSortString(Integer i) {
	String is = i.toString();
	is = is.length() + is;
	return is;
    }

    public Hashtable<String, Serializable> getItemInfo(String xmlString)
	    throws Exception {
	Hashtable<String, Serializable> aus = new Hashtable<String, Serializable>();
	XMLParser parser = new XMLParser(xmlString, null);
	Element oberst = parser.getOberst();
	String[] tags = { "item", "ibarcode", "bib", "erstesBuchAufBrett",
		"ruecken", "status" };
	String[][] subtags = { null, null, null,
		new String[] { "ebene", "regal", "brett", "laengeCm" },
		new String[] { "breiteMm", "hoeheMm" }, null };
	String[] attrs = { null, null, null, null, null, "wert" };
	for (int a = 0; a < tags.length; a++) {
	    String tag = tags[a];
	    String[] subtag = subtags[a];
	    String attr = attrs[a];
	    Element such = parser.searchElement(oberst, tag, attr, null, null,
		    false);
	    if (such != null) {
		if ((subtag == null) && (attr == null)) {
		    aus.put(tag, such.getTextContent());
		} else {
		    if (subtag == null) {
			aus.put(tag, such.getAttribute().get(attr));
		    } else {
			Hashtable<String, Object> sub = new Hashtable<String, Object>();
			aus.put(tag, sub);
			for (int b = 0; b < subtag.length; b++) {
			    String subt = subtag[b];
			    Element suchSub = parser.searchElement(such, subt,
				    null, null, null, false);
			    if (suchSub != null) {
				sub.put(subt, suchSub.getTextContent());
			    }
			}
		    }
		}
	    }
	}
	System.out.println(aus);
	return aus;
    }

    protected Object[] getNext(Long alt, boolean successor) throws Exception {
	String hql = null;
	if (successor) {
	    // hql="select a.dbseg.s,b.xmlString,a.dbsed.s,a.dbseg.s from DBs a,DBs b where a.dbsed.i.id="+itemIdentifier+" and a.dbseg.i.id=a.dbsed.i.id and a.dbif.f="+successorFunction+" and a.dbsed.s="+alt+" and b.dbsed=a.dbsed and b.dbseg.i.id="+sorterIdentifier+" and b.dbif.f="+sorterFunction;
	    hql = "select a.dbseg.s,b.xmlString,a.dbsed.s,a.dbseg.s from DBs a,DBs b where a.dbsed.i.id="
		    + itemIdentifier
		    + " and a.dbseg.i.id=a.dbsed.i.id and a.dbif.f="
		    + successorFunction
		    + " and a.dbsed.s="
		    + alt
		    + " and b.dbsed=a.dbsed and b.dbseg.i.id="
		    + sorterIdentifier + " and b.dbif.f=" + sorterFunction;
	} else {
	    // hql="select a.dbsed.s,b.xmlString,a.dbsed.s,a.dbseg.s from DBs a,DBs b where a.dbsed.i.id="+itemIdentifier+" and a.dbseg.i.id=a.dbsed.i.id and a.dbif.f="+successorFunction+" and a.dbseg.s="+alt+" and b.dbsed=a.dbsed and b.dbseg.i.id="+sorterIdentifier+" and b.dbif.f="+sorterFunction;
	    // beachten: am Ende könnte wegen fehlenden cs gar nichts kommen:
	    // outer join versuchen oder für diesen einen Fall Extra-Anfrage
	    hql = "select a.dbsed.s,b.xmlString,a.dbseg.s,c.dbseg.s from DBs a,DBs b,DBs c where a.dbsed.i.id="
		    + itemIdentifier
		    + " and a.dbseg.i.id=a.dbsed.i.id and a.dbif.f="
		    + successorFunction
		    + " and a.dbseg.s="
		    + alt
		    + " and b.dbsed=a.dbseg and b.dbseg.i.id="
		    + sorterIdentifier
		    + " and b.dbif.f="
		    + sorterFunction
		    + " and c.dbsed=a.dbseg and c.dbseg.i.id=c.dbsed.i.id and c.dbif.f=a.dbif.f";
	}
	System.out.println(hql);
	List list = dbmaster.parseHQLStatement(hql).get("results");
	Object[] aus = null;
	if ((list != null) && (list.size() > 0)) {
	    aus = (Object[]) list.get(0);
	}
	for (int a = 0; (aus != null) && a < aus.length; a++) {
	    System.out.println(a + ":" + aus[a].toString());
	}
	return aus;
    }

    @Override
    public void setSorterFunction(Long sorterFunction) {
	this.sorterFunction = sorterFunction;
    }

    @Override
    public Long getSorterFunction() {
	return sorterFunction;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
	try {
	    Object[] obs = iudMessageResolver.resolveIUDMessage(message,
		    dbmaster);
	    if (obs != null) {
		Long id = (Long) obs[2];
		Long sd = (Long) obs[3];
		Long ig = (Long) obs[4];
		Long sg = (Long) obs[5];
		Long f = (Long) obs[6];
		String xmlString = (String) obs[7];
		DBAction dbAction = (DBAction) obs[0];
		DTOE dtoe = (DTOE) obs[1];
		System.out.println("StackSorterImpl.handleMessage2:" + this
			+ "/" + dbAction + "/" + dtoe + "/" + id + "("
			+ itemIdentifier + ")/" + sd + "/" + ig + "("
			+ sorterIdentifier + "/" + itemInfoIdentifier + ")"
			+ sg + "/" + f + "(" + sorterFunction + "/"
			+ itemInfoFunction + ")/" + xmlString);
		System.out
			.println("StackSorterImpl.handleMessage2a:"
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
		if ((dtoe.equals(DTOE.DBs) && (id.equals(itemIdentifier)) && (((ig
			.equals(sorterIdentifier)) && (f.equals(sorterFunction))) || ((ig
			.equals(itemInfoIdentifier)) && (f
			.equals(itemInfoFunction)))))) {
		    int index = triples.indexOf(sd);
		    System.out.println("StackSorterImpl.handleMessage3:"
			    + index);
		    if (ig.equals(sorterIdentifier)) {
			if (dbAction.equals(DBAction.DELETE)) {
			    if (index > -1) {
				Triple triple = triples.get(index);
				System.out.println("Remove sort:"
					+ triples.remove(triple));
				removeEbab(triple);
			    }
			} else {
			    System.out.println("Sort in:" + xmlString + "/"
				    + sd);
			    sortIn(triples, xmlString, sd, null);
			}
		    } else {
			if (dbAction.equals(DBAction.STATUSQUO)) {
			    if (index > -1) {
				Triple triple = triples.get(index);
				Hashtable<String, Serializable> itemInfo = getItemInfo(xmlString);
				System.out.println("update:" + itemInfo);
				triple.setItemInfo(itemInfo);
				connectEbab(triple);
			    }
			} else {
			    if (dbAction.equals(DBAction.DELETE)
				    && (index > -1)) {
				Triple triple = triples.get(index);
				// System.out.println("remove ebab:"+triple);
				removeEbab(triple);
				triple.setItemInfo(new Hashtable<String, Serializable>());
			    }
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new MessagingException(message);
	}
    }

    protected void removeEbab(Triple triple) {
	Hashtable<String, Serializable> itemInfo = triple.getItemInfo();
	if (itemInfo != null) {
	    Triple tripleEbab = (Triple) itemInfo.get("triple");
	    if (tripleEbab != null) {
		ebabTriples.remove(tripleEbab);
		itemInfo.remove("triple");
	    }
	}
    }

    public Triple sortIn(TripleList list, String string, Long set,
	    Hashtable<String, Serializable> info) {
	ListIterator<Triple> li = list.listIterator();
	int greater = 1;
	Triple alt = null;
	Triple triple = null;
	while (li.hasNext() && (greater > -1)) {
	    triple = li.next();
	    System.out.println("StackSorterImpl.sortIn:" + string + " ... "
		    + triple.getSortString());
	    greater = string.compareTo(triple.getSortString());
	    if (greater > -1) {
		alt = triple;
	    }
	}
	Triple newTriple = null;
	if (alt != null) {
	    System.out.println("StackSorterImpl.alt:" + alt.getSortString()
		    + "/" + alt.getItemSet() + "/" + alt.getSuccessorSet());
	    System.out.println("StackSorterImpl.newTriple:" + string + "/"
		    + set + "/" + alt.getSuccessorSet() + "/" + info);
	    newTriple = new Triple(string, set, alt.getSuccessorSet(), info);
	    list.add(list.indexOf(alt.getItemSet()) + 1, newTriple);
	} else {
	    newTriple = new Triple(string, set, 0l, info);
	    list.add(newTriple);
	}
	// System.out.println(list);
	return newTriple;
    }

    @Override
    public void setItemInfoIdentifier(Long itemInfoIdentifier) {
	this.itemInfoIdentifier = itemInfoIdentifier;
    }

    @Override
    public Long getItemInfoIdentifier() {
	return itemInfoIdentifier;
    }

    @Override
    public void setItemInfoFunction(Long itemInfoFunction) {
	this.itemInfoFunction = itemInfoFunction;
    }

    @Override
    public Long getItemInfoFunction() {
	return itemInfoFunction;
    }

    @Override
    public int compareSorterStrings(String sorter1, String sorter2) {
	return sorter1.compareTo(sorter2);
    }

    @Override
    public void setItemNumberIdentifier(Long itemNumberIdentifier) {
	this.itemNumberIdentifier = itemNumberIdentifier;
    }

    @Override
    public Long getItemNumberIdentifier() {
	return itemNumberIdentifier;
    }

    @Override
    public void setItemNumberFunction(Long itemNumberFunction) {
	this.itemNumberFunction = itemNumberFunction;
    }

    @Override
    public Long getItemNumberFunction() {
	return itemNumberFunction;
    }

    public Long getItemSet(int horizonItemNumber) throws Exception {
	String hql = "select a.dbsed.s from DBs a where dbsed.i.id="
		+ itemIdentifier + " and dbseg.i.id=" + itemNumberIdentifier
		+ " and dbif.f=" + itemNumberFunction + " and xmlString='"
		+ horizonItemNumber + "'";
	System.out.println(hql);
	Long sd = null;
	List list = dbmaster.parseHQLStatement(hql).get("results");
	if ((list != null) && (list.size() > 0)) {
	    sd = (Long) list.get(0);
	}
	System.out.println(sd);
	return sd;
    }

    @Override
    public void setDBEndpoint(DBEndpoint dbendpoint) {
	this.dbendpoint = dbendpoint;
    }

    @Override
    public DBEndpoint getDBEndpoint() {
	return dbendpoint;
    }

}
