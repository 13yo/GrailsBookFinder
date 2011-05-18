package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT;
import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT.PreparedStatementT;
import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.DBConnectorChooserLib;
import de.mpg.mis.neuesbibliothekssystem.stacks.services.ImageService;
import de.mpg.mis.neuesbibliothekssystem.dbmaster.remote.DBMaster;

public class DoMaintain {
    /** Logger available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${DBMaster.files.imagesDirectory}")
    protected Resource imagesDirectory;
    protected PreparedStatementT psItem;
    protected PreparedStatementT psItemEreignis;
    protected PreparedStatementT psBrett;
    protected PreparedStatementT psCall;
    protected PreparedStatementT psAuth;
    protected PreparedStatementT psTitle;
    protected PreparedStatementT psYear;
    protected PreparedStatementT psItemPlan;
    protected PreparedStatementT psEreignisPuffer;
    protected PreparedStatementT psEreignisPufferPlan;
    protected char trenn = 31;
    protected String subfieldDelimiter = new String(new char[] { trenn });
    protected double spineFactor = 0.254629;
    protected StackSorterMaintain maintainer;
    protected File lastUsedDateFile = null;
    protected Date lastUsedDate;

    protected static boolean weiter = true;
    protected static JButton knopf = new JButton("stop");

    private boolean doStart = false;

    // zentrale Anpassung des Select-Bereiches
    private String dbSelectRange = "between 'A' and 'ZZ'";

    // für Spring-Config eingebunden
    @Value("${DBMaster.lastMaintained}")
    protected Resource lastUsedDateResource;

    @Scheduled(fixedDelay = 2000)
    void doPoll() throws Exception {
	if (this.doStart) {
	    this.doStart = false;
	    if (logger.isInfoEnabled())
		logger.info("start Poll at: " + new Date());
	    this.update();
	    if (logger.isInfoEnabled())
		logger.info("end Poll at: " + new Date());
	    this.doStart = true;
	}
    }

    public static void main(String[] args) throws Exception {
	Date d = new Date();
	System.out.println(d.getTime() / 1000 / 60 / 60 / 24);
	// ApplicationContext applicationContext = new
	// ClassPathXmlApplicationContext(
	// "classpath*:META-INF/aspects.xml");
	//
	// DoMaintain doMaintain = (DoMaintain) applicationContext
	// .getBean("doMaintain");
	// doMaintain.init();
	// // doMaintain.start();
	// doMaintain.getMaintainer().setCheckForUpdate(true);
    }

    public static class Lauscher implements ActionListener {
	public StackSorterMaintain stackSorter;
	public boolean isIn = true;
	public int item = 28805;
	// public String
	// sort="moHG11 JohnsonJOHNSON NEIL F41961FINANCIAL MARKET COMPLEXITY /4200300374842";
	public String sort = null;
	public String infoIn = "<item>28805</item><ibarcode>00374842</ibarcode><bib>22817</bib><status wert='i'></status><ruecken><breiteMm>17.0</breiteMm><hoeheMm>247.0</hoeheMm></ruecken>";
	public String infoOut = "<item>28805</item><ibarcode>00374842</ibarcode><bib>22817</bib><status wert='o'></status><ruecken><breiteMm>17.0</breiteMm><hoeheMm>247.0</hoeheMm></ruecken>";

	public Lauscher(StackSorterMaintain stackSorter) {
	    this.stackSorter = stackSorter;
	}

	public void actionPerformed(ActionEvent ae) {
	    String command = ae.getActionCommand();
	    if (command.equals("exit DoMaintain")) {
		try {
		    stackSorter.exit();
		    // System.exit(0);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    } else {
		isIn = !isIn;
		String info = infoIn;
		if (!isIn) {
		    info = infoOut;
		}
		try {
		    stackSorter.insertSortedItem(sort, info, item);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    public void setMaintainer(StackSorterMaintain maintainer) {
	this.maintainer = maintainer;
	try {
	    // this.maintainer.init();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public StackSorterMaintain getMaintainer() {
	return maintainer;
    }

    public void init() throws Exception {
	// kommt vom SetMaintainer
	this.maintainer.init();

	DBConnectorChooserLib dbConnector = new DBConnectorChooserLib();
	ConnectionT con = new ConnectionT(dbConnector.getConnector("horizon")
		.getConnection());
	con.exec = true;
	// psItem = con
	// .prepareStatement("select a.item#,a.ibarcode,a.bib#,a.item_status from horizon..item a,horizon..call b where (a.creation_date>? or a.last_update_date>? or a.last_status_update_date>? or a.last_inventory_date>?) and a.item#=b.item# and b.processed between 'GF' and 'HB' and collection in ('mo') order by b.processed desc,a.ibarcode");

	// To be preserved

	// psItem = con
	// .prepareStatement("select a.item#,a.ibarcode,a.bib#,a.item_status from horizon..item a,horizon..call b where (a.creation_date>? or a.last_update_date>? or a.last_status_update_date>? or a.last_inventory_date>?) and a.item#=b.item# and b.processed between 'A' and 'ZZ' and collection in ('mo') order by b.processed desc,a.ibarcode");
	// psBrett = con
	// .prepareStatement("select a.ebene,a.regal,a.brett,b.laenge_cm from training..regal_plan_pos1 a,training..regal_laenge b where a.ebene*=b.ebene and a.regal*=b.regal# and a.ibarcode=? order by b.date desc");
	// psCall = con
	// .prepareStatement("select b.collection,a.processed from horizon..call a,horizon..item b where a.item#=b.item# and b.item#=?");
	// psAuth = con
	// .prepareStatement("select a.text,a.tag,b.tag from horizon..auth a,horizon..bib b where a.auth#=b.auth# and b.tag in (100,700) and a.tag=100 and a.tagord=0 and b.tagord=0 and b.bib#=? order by b.tag");
	// psTitle = con
	// .prepareStatement("select a.text,a.longtext,a.indicators from horizon..fullbib a where bib#=? and a.tag=245");
	// psYear = con
	// .prepareStatement("select a.text from horizon..bib a where a.bib#=? and a.tag=260");
	// psItemPlan = con
	// .prepareStatement("select c.item#,a.ibarcode,c.bib#,c.item_status,d.item#,b.ibarcode,d.bib#,d.item_status from training..regal_plan_pos1 a,horizon..item c,training..regal_plan_pos1_alt b,horizon..item d where a.ebene=b.ebene and a.regal=b.regal and a.brett=b.brett and not a.ibarcode=b.ibarcode and b.date>? and c.ibarcode=a.ibarcode and d.ibarcode=b.ibarcode");

	psItem = con
		.prepareStatement("select a.item#,a.ibarcode,a.bib#,a.item_status from horizon..item a,horizon..call b where (a.creation_date>? or a.last_update_date>? or a.last_status_update_date>? or a.last_inventory_date>? ) and a.item#=b.item# and b.processed "
			+ dbSelectRange
			+ " and collection in ('mo') order by b.processed desc,a.ibarcode");
	psItemEreignis = con
		.prepareStatement("select a.item#,a.ibarcode,a.bib#,a.item_status,c.timestamp from horizon..item a,horizon..call b,training..ereignisPuffer c where a.item#=c.item and a.item#=b.item# and b.processed "
			+ dbSelectRange
			+ " and collection in ('mo') order by c.timestamp,b.processed desc,a.ibarcode");
	// psItem=con.prepareStatement("select a.item#,a.ibarcode,a.bib#,a.item_status from horizon..item a,horizon..call b where (a.creation_date>? or a.last_update_date>? or a.last_status_update_date>? or a.last_inventory_date>?) and a.item#=b.item# and b.processed between 'H' and 'HA' and collection in ('mo')");
	// psBrett=con.prepareStatement("select a.ebene,a.regal,a.brett from training..regal_plan_pos1 a where ibarcode=?");
	psBrett = con
		.prepareStatement("select a.ebene,a.regal,a.brett,b.laenge_cm from training..regal_plan_pos1 a,training..regal_laenge b where a.ebene*=b.ebene and a.regal*=b.regal# and a.ibarcode=? order by b.date desc");
	psCall = con
		.prepareStatement("select b.collection,a.processed from horizon..call a,horizon..item b where a.item#=b.item# and b.item#=?");
	// psAuth=con.prepareStatement("select a.text,a.tag,b.tag from horizon..auth a,horizon..bib b where a.auth#=b.auth# and b.tag in (100,700,110,710) and a.tag between 100 and 199 and a.tagord=0 and b.tagord=0 and b.bib#=? order by b.tag");
	psAuth = con
		.prepareStatement("select a.text,a.tag,b.tag from horizon..auth a,horizon..bib b where a.auth#=b.auth# and b.tag in (100,700) and a.tag=100 and a.tagord=0 and b.tagord=0 and b.bib#=? order by b.tag");
	psTitle = con
		.prepareStatement("select a.text,a.longtext,a.indicators from horizon..fullbib a where bib#=? and a.tag=245");
	psYear = con
		.prepareStatement("select a.text from horizon..bib a where a.bib#=? and a.tag=260");
	// psItemPlan=con.prepareStatement("select c.item#,a.ibarcode,c.bib#,c.item_status,d.item#,b.ibarcode,d.bib#,d.item_status from training..regal_plan_pos1 a,horizon..item c,training..regal_plan_pos1_alt b,horizon..item d where a.ebene=b.ebene and a.regal=b.regal and a.brett=b.brett and not a.ibarcode=b.ibarcode and b.date>? and c.ibarcode=a.ibarcode and d.ibarcode=b.ibarcode");
	psItemPlan = con
		.prepareStatement("select c.item#,a.ibarcode,c.bib#,c.item_status,d.item#,b.ibarcode,d.bib#,d.item_status from training..regal_plan_pos1 a,horizon..item c,training..regal_plan_pos1_alt b,horizon..item d where a.ebene=b.ebene and a.regal=b.regal and a.brett=b.brett and not a.ibarcode=b.ibarcode and (b.date>? or c.item# in (select distinct item from training..ereignisPuffer)) and c.ibarcode=a.ibarcode and d.ibarcode=b.ibarcode and 1=0");
	psEreignisPuffer = con
		.prepareStatement("delete from training..ereignisPuffer where timestamp<=? and itemOderPlan=1");

	psEreignisPufferPlan = con
		.prepareStatement("delete from training..ereignisPuffer where itemOderPlan=0");

	lastUsedDateFile = lastUsedDateResource.getFile();

	try {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    lastUsedDateFile));
	    lastUsedDate = (Date) ois.readObject();
	    ois.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    // lastUsedDate = new Date(15090 * 24 * 3600 * 1000);
	    lastUsedDate = new Date(0);
	}
	// lastUsedDate=new Date(0);
	// deleteAllSets();

	if (this.lastUsedDate.compareTo(new Date(0)) == 0)
	    this.start(false);
	else
	    this.start(true);

	this.doStart = true;
    }

    protected void deleteAllSets() throws Exception {
	DBMaster dbmaster = maintainer.getDBMaster();
	Long itemIdentifier = maintainer.getItemIdentifier();
	Long[][] was = {
		new Long[] { itemIdentifier,
			maintainer.getItemNumberIdentifier(),
			maintainer.getItemNumberFunction() },
		new Long[] { itemIdentifier,
			maintainer.getItemInfoIdentifier(),
			maintainer.getItemInfoFunction() },
		new Long[] { itemIdentifier, itemIdentifier,
			maintainer.getSuccessorFunction() },
		new Long[] { itemIdentifier, maintainer.getSorterIdentifier(),
			maintainer.getSorterFunction() } };
	for (int a = 0; a < was.length; a++) {
	    Long[] akt = was[a];
	    try {
		deleteSets(dbmaster, akt[0], akt[1], akt[2]);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    protected void deleteSets(DBMaster dbmaster, Long id, Long ig, Long f)
	    throws Exception {
	List list = dbmaster.parseHQLStatement(
		"select a.dbsed.s,a.dbseg.s from DBs a where a.dbsed.i.id="
			+ id + " and a.dbseg.i.id=" + ig + " and a.dbif=" + f)
		.get("results");
	Iterator it = list.iterator();
	while (it.hasNext()) {
	    Long[] ss = (Long[]) it.next();
	    dbmaster.deleteSet(id, ss[0], ig, ss[1], f, true);
	}
    }

    /**
     * füllen der Maintain-Liste + Aktualisierung der Elemente
     * 
     * @param checkForUpdate
     *            soll der Maintainer schon sofort einsortieren?
     * @throws Exception
     */
    public void start(boolean checkForUpdate) throws Exception {

	getMaintainer().setCheckForUpdate(checkForUpdate);
	int datum = (int) (lastUsedDate.getTime() / 1000 / 60 / 60 / 24);
	// int datum = 15080;
	System.out.println(datum + ": " + lastUsedDate.toString());
	for (int a = 1; a < 5; a++) {
	    psItem.setInt(a, datum);
	}

	this.dealWithItems(true);

	this.doPlan(datum);

	this.writeUpdateFile();

	getMaintainer().setCheckForUpdate(true);
    }

    /**
     * Aktualisierung der Maintainer-Liste mit Daten aus dem Ereignispuffer
     * 
     * @throws Exception
     */
    public void update() throws Exception {

	getMaintainer().setCheckForUpdate(true);
	int datum = (int) (lastUsedDate.getTime() / 1000 / 60 / 60 / 24);
	// int datum = 15080;
	System.out.println(datum + ": " + lastUsedDate.toString());

	byte[] timestamp = this.dealWithItems(false);
	this.doPlan(datum);

	if (timestamp != null) {
	    psEreignisPuffer.setBytes(1, timestamp);
	    psEreignisPuffer.execute();
	}

	this.writeUpdateFile();

	getMaintainer().setCheckForUpdate(true);
    }

    private byte[] dealWithItems(boolean init) throws Exception {
	byte[] timestamp = null;

	ResultSet rs = init ? psItem.executeQuery() : psItemEreignis
		.executeQuery();
	int z = 1;
	while (rs.next() && weiter) {
	    System.out.println("Treffer: " + z++);
	    int item = rs.getInt(1);
	    String ibarcode = rs.getString(2);
	    int bib = rs.getInt(3);
	    String status = rs.getString(4);
	    if (!status.equals("i") && !status.equals("o")) {
		status = "m";
	    }
	    if (ibarcode.indexOf("0") == 0) {
		String info = getInfo(item, ibarcode, bib, status);
		String sort = getSort(item, bib, ibarcode);
		System.out.println("insert: " + sort + "\t" + info + "\t"
			+ item);
		maintainer.insertSortedItem(sort, info, item);
	    } else {
		if (ibarcode.indexOf("D") == 0) {
		    List list = maintainer
			    .getDBMaster()
			    .parseHQLStatement(
				    "select dbsed.s from DBs where xmlString='"
					    + item + "'").get("results");
		    if ((list != null) && (list.size() > 0)) {
			Long itemSet = (Long) list.get(0);
			System.out.println("delete: " + ibarcode + "\t"
				+ itemSet);
			maintainer.removeSortedItem(itemSet);
		    }
		}
	    }
	    if (!init)
		timestamp = rs.getBytes(5);
	}
	rs.close();
	return timestamp;
    }

    private void doPlan(int datum) throws Exception {
	psItemPlan.setInt(1, datum);
	ResultSet rs = psItemPlan.executeQuery();
	int z = 1;
	while (rs.next()) {
	    System.out.println("Treffer Plan: " + z++);
	    int[] items = { rs.getInt(1), rs.getInt(5) };
	    String[] ibarcodes = { rs.getString(2), rs.getString(6) };
	    int[] bibs = { rs.getInt(3), rs.getInt(7) };
	    String[] statuses = { rs.getString(4), rs.getString(8) };
	    for (int a = 0; a < items.length; a++) {
		int item = items[a];
		String ibarcode = ibarcodes[a];
		int bib = bibs[a];
		String status = statuses[a];
		if (!status.equals("i") && !status.equals("o")) {
		    status = "m";
		}
		String info = getInfo(item, ibarcode, bib, status);
		String sort = null;
		System.out.println("insert: " + sort + "\t" + info + "\t"
			+ item);
		maintainer.insertSortedItem(sort, info, item);
	    }
	}
	rs.close();
	psEreignisPufferPlan.execute();
    }

    private void writeUpdateFile() throws FileNotFoundException, IOException {
	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
		lastUsedDateFile));
	lastUsedDate = new Date();
	oos.writeObject(lastUsedDate);
	oos.close();
    }

    protected String getInfo(int item, String ibarcode, int bib, String status) {
	String aus = "<item>" + item + "</item><ibarcode>" + ibarcode
		+ "</ibarcode><bib>" + bib + "</bib><status wert='" + status
		+ "'></status>";
	try {
	    String ruecken = getRuecken(ibarcode);
	    aus = aus + ruecken;
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	try {
	    String brett = getBrett(ibarcode);
	    aus = aus + brett;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return aus;
    }

    protected String getRuecken(String ibarcode) throws Exception {
	String aus = "";
	Dimension dimension = ImageService
		.getUprightImageDim(getRueckenFile(ibarcode));
	Dimension spineDimensionMm = new Dimension(
		(int) (dimension.width * spineFactor),
		(int) (dimension.height * spineFactor));
	aus = "<ruecken><breiteMm>" + spineDimensionMm.getWidth()
		+ "</breiteMm><hoeheMm>" + spineDimensionMm.getHeight()
		+ "</hoeheMm></ruecken>";
	return aus;
    }

    protected File getRueckenFile(String ibarcode) throws Exception {
	Resource i = imagesDirectory.createRelative(ibarcode + ".jpg");
	if (i.exists())
	    return i.getFile();
	else {
	    i = imagesDirectory.createRelative(ibarcode + ".JPG");
	    if (i.exists())
		return i.getFile();
	    else
		throw new Exception(i.getFile().getAbsolutePath()
			+ " doesn't exist");
	}
    }

    // erst, wenn ein Image gemalt worden ist, ist dessen Dimension bekannt
    // protected class Zwi extends JFrame {
    // protected Image image;
    // protected Dimension dimension;
    //
    // public void setImage(Image img) {
    // image = img;
    // }
    //
    // public void paint(Graphics g) {
    // g.drawImage(image, 0, 0, this);
    // }
    // }

    protected String getBrett(String ibarcode) throws Exception {
	String info = "";
	psBrett.setString(1, ibarcode);
	ResultSet rs = psBrett.executeQuery();
	if (rs.next()) {
	    Integer ebene = rs.getInt(1);
	    Integer regal = rs.getInt(2);
	    Integer brett = rs.getInt(3);
	    Integer laengeCm = rs.getInt(4);
	    info = "<erstesBuchAufBrett><ebene>" + ebene + "</ebene><regal>"
		    + regal + "</regal><brett>" + brett + "</brett><laengeCm>"
		    + laengeCm + "</laengeCm></erstesBuchAufBrett>";
	}
	rs.close();
	return info;
    }

    protected String getSort(int item, int bib, String ibarcode)
	    throws Exception {
	String aus = getCall(item) + trenn + getAuth(bib) + trenn
		+ getTitle(bib) + trenn + getYear(bib) + trenn + ibarcode;
	return aus;
    }

    protected String getCall(int item) throws Exception {
	psCall.setInt(1, item);
	ResultSet rs = psCall.executeQuery();
	String aus = "";
	if (rs.next()) {
	    String collection = rs.getString(1);
	    String call = rs.getString(2);
	    aus = collection + trenn + call;
	}
	rs.close();
	return aus;
    }

    public static String getSubfield(String field, String subfieldDelimiter,
	    String subfieldCode) {
	Pattern pat = Pattern.compile(subfieldDelimiter + subfieldCode + "[^"
		+ subfieldDelimiter + "]+");
	String aus = "";
	Matcher mat = pat.matcher(field);
	if (mat.find()) {
	    aus = mat.group();
	    aus = aus.substring(2, aus.length());
	}
	return aus;
    }

    public static String clearSortierlaute(String ein) {
	// beruhend auf http://lcweb2.loc.gov/diglib/codetables/45.html
	Hashtable<String, String> umlaute = new Hashtable<String, String>();
	umlaute.put("[" + ((char) 232) + ((char) 222) + ((char) 254) + "]a",
		"ae");
	umlaute.put("[" + ((char) 232) + ((char) 222) + ((char) 254) + "]o",
		"oe");
	umlaute.put("[" + ((char) 232) + ((char) 222) + ((char) 254) + "]u",
		"ue");
	umlaute.put("[" + ((char) 232) + ((char) 222) + ((char) 254) + "]A",
		"AE");
	umlaute.put("[" + ((char) 232) + ((char) 222) + ((char) 254) + "]O",
		"OE");
	umlaute.put("[" + ((char) 232) + ((char) 222) + ((char) 254) + "]U",
		"UE");
	umlaute.put("[" + ((char) 199) + ((char) 223) + "]", "ss");
	umlaute.put(((char) 161) + "", "L");
	umlaute.put("[" + ((char) 162) + ((char) 214) + "]", "OE");
	umlaute.put(((char) 163) + "", "D");
	umlaute.put(((char) 164) + "", "TH");
	umlaute.put("[" + ((char) 165) + ((char) 196) + ((char) 198) + "]",
		"AE");
	umlaute.put(((char) 166) + "", "OE");
	umlaute.put(((char) 172) + "", "O");
	umlaute.put(((char) 173) + "", "U");
	umlaute.put(((char) 177) + "", "l");
	umlaute.put(((char) 178) + "", "oe");
	umlaute.put(((char) 179) + "", "d");
	umlaute.put(((char) 180) + "", "th");
	umlaute.put(((char) 181) + "", "ae");
	umlaute.put(((char) 182) + "", "ae");
	umlaute.put(((char) 188) + "", "o");
	umlaute.put(((char) 189) + "", "u");
	umlaute.put(((char) 193) + "", "l");
	// plus Ascii-Werte, von denen das bekannt ist
	Enumeration<String> keys = umlaute.keys();
	while (keys.hasMoreElements()) {
	    String key = keys.nextElement();
	    ein = ein.replaceAll(key, umlaute.get(key));
	}
	return ein;
    }

    public static String clearCharacters(String ein) {
	StringBuffer aus = new StringBuffer();
	for (int a = 0; a < ein.length(); a++) {
	    char c = ein.charAt(a);
	    // if(c<224)
	    if (c < 127) {
		aus.append(c);
	    }
	}
	return aus.toString();
    }

    public static String sortNumbers(String ein) {
	Pattern pat = Pattern.compile("\\d+");
	StringBuffer aus = new StringBuffer();
	Matcher mat = pat.matcher(ein);
	while (mat.find()) {
	    aus.append(ein.substring(0, mat.start()));
	    String group = mat.group();
	    String test = aus.toString();
	    if ((aus.length() == 0)
		    || test.equals(test.replaceFirst("[\\.,]\\z", ""))) {
		aus.append(group.length());
	    }
	    aus.append(group);
	    ein = ein.substring(mat.end(), ein.length());
	    mat = pat.matcher(ein);
	}
	aus.append(ein);
	return aus.toString();
    }

    public static String getSortierstring(String ein) {
	ein = clearSortierlaute(ein).toUpperCase();
	ein = clearCharacters(ein);
	ein = sortNumbers(ein);
	return ein;
    }

    protected String getAuth(int bib) throws Exception {
	psAuth.setInt(1, bib);
	ResultSet rs = psAuth.executeQuery();
	String aus = "";
	if (rs.next()) {
	    String auth = rs.getString(1);
	    int authTag = rs.getInt(2);
	    int bibTag = rs.getInt(3);
	    if (authTag == 100) {
		String nameA = getSortierstring(getSubfield(auth,
			subfieldDelimiter, "a"));
		String nameQ = getSortierstring(getSubfield(auth,
			subfieldDelimiter, "q"));
		String nameD = getSortierstring(getSubfield(auth,
			subfieldDelimiter, "d"));
		String nachname = nameA.replaceFirst(",.+", "");
		String vorname = nameA.replaceFirst(".+?,", "")
			.replaceAll("[^a-zA-Z\\- ]", " ")
			.replaceAll("  +", " ").replaceFirst(" +\\z", "");
		if (!nameQ.equals("")) {
		    vorname = nameQ.replaceAll("[^a-zA-Z\\- ]", " ")
			    .replaceAll("  +", " ").replaceFirst(" +\\z", "");
		}
		Pattern pat = Pattern.compile("4\\d{4}");
		Matcher mat = pat.matcher(nameD);
		String[] vonBis = { "", "" };
		int z = 0;
		while (mat.find()) {
		    vonBis[z++] = mat.group();
		}
		aus = nachname + trenn + vorname + trenn + vonBis[0] + trenn
			+ vonBis[1];
	    }
	}
	rs.close();
	return aus;
    }

    protected String getTitle(int bib) throws Exception {
	psTitle.setInt(1, bib);
	ResultSet rs = psTitle.executeQuery();
	String aus = "";
	if (rs.next()) {
	    String text = rs.getString(1);
	    String longtext = rs.getString(2);
	    String indicators = rs.getString(3);
	    aus = text;
	    if ((text == null) || (text.equals(""))) {
		aus = longtext;
	    }
	    aus = getSortierstring(getSubfield(aus, subfieldDelimiter, "a"));
	    String bandN = getSortierstring(getSubfield(aus, subfieldDelimiter,
		    "n"));
	    String bandP = getSortierstring(getSubfield(aus, subfieldDelimiter,
		    "p"));
	    if ((indicators != null) && !indicators.equals("")
		    && (indicators.length() == 2)) {
		try {
		    int von = Integer.parseInt(indicators.substring(1, 1));
		    aus = aus.substring(von, aus.length());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	    aus = aus + trenn + bandN + trenn + bandP;
	}
	rs.close();
	return aus;
    }

    protected String getYear(int bib) throws Exception {
	psYear.setInt(1, bib);
	ResultSet rs = psYear.executeQuery();
	String aus = "";
	if (rs.next()) {
	    String year = rs.getString(1);
	    year = getSortierstring(getSubfield(year, subfieldDelimiter, "c"));
	    Pattern pat = Pattern.compile("4\\d{4}");
	    Matcher mat = pat.matcher(year);
	    if (mat.find()) {
		aus = mat.group();
	    }
	}
	rs.close();
	return aus;
    }

    protected static class StopLauscher implements ActionListener {
	public StopLauscher() {

	}

	public void actionPerformed(ActionEvent ae) {
	    weiter = false;
	    knopf.setText("angehalten");
	}
    }
}
