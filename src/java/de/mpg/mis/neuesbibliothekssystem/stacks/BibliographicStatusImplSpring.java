package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.net.URL;
import java.sql.ResultSet;
import java.util.Hashtable;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import de.mpg.mis.neuesbibliothekssystem.stacks.CookiedHttpURLConnector;
import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT.PreparedStatementT;

public class BibliographicStatusImplSpring implements
	BibliographicStatusHorizon {
    protected int item;
    protected String ibarcode;
    protected int bibno;
    protected boolean missing;
    protected boolean borrowed;
    protected ElectronicRight rights = ElectronicRight.kein;
    protected PreparedStatementT[] preparedStatementTs;
    protected String[] gbip = { "inhaltsverzeichnisse", "indizes", "vorworte",
	    "literaturverzeichnisse", "haupttitelseiten", "symbolverzeichnisse" };
    protected String[] gbib = { "umschlaege", "vorderseiten", "ruecken" };
    protected static String[] psTs = {
	    "select collection from horizon..item where item#=?",
	    "select item_status from horizon..item where item#=?",
	    "select zugriffErlaubt from localHosting_aufsatz a,localHosting_sammlung b where a.sammlung#=b.sammlung# and b.idart='MIS-Horizon-bib#' and b.id=?",
	    "select count(*) from horizon..item where collection like 'M%' and item#=?" };

    public BibliographicStatusImplSpring(int item, String ibarcode, int bibno,
	    PreparedStatementT[] preparedStatementTs) throws Exception {
	this.item = item;
	this.ibarcode = ibarcode;
	this.bibno = bibno;
	this.preparedStatementTs = preparedStatementTs;
	prepare();
	refresh();
    }

    public BibliographicStatusImplSpring() {

    }

    public void init() {
	try {
	    prepare();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	refresh();
    }

    protected void prepare() throws Exception {
	// preparedStatementTs[0]=connectionT.prepareStatement("select collection from horizon..item where item#=?");
	preparedStatementTs[0].setInt(1, item);
	preparedStatementTs[1].setInt(1, item);
	preparedStatementTs[2].setString(1, (new Integer(bibno)).toString());
	preparedStatementTs[3].setInt(1, item);
    }

    public void refresh() {
	missing = getMissing();
	borrowed = getBorrowed();
	rights = getRights();
    }

    protected Object askDB(int was, Object fail) {
	Object aus = fail;
	try {
	    prepare();
	    ResultSet rs = preparedStatementTs[was].executeQuery();
	    if (rs.next()) {
		aus = rs.getObject(1);
	    }
	    rs.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return aus;
    }

    public int getBibno() {
	return bibno;
    }

    public String getBarcode() {
	return ibarcode;
    }

    @Override
    public Hashtable<String, Resource> getBibliographicInformation() {
	Hashtable<String, Resource> aus = new Hashtable<String, Resource>();
	for (int a = 0; a < gbip.length; a++) {
	    String key = gbip[a];
	    String content = askServer("was=" + key + "&ibarcode=" + ibarcode
		    + "&format=pdf");
	    if ((content != null) && (!content.equals(""))) {
		try {
		    aus.put(key, new UrlResource(content));
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	for (int a = 0; a < gbib.length; a++) {
	    String key = gbib[a];
	    String rot = "";
	    if (key.equals("umschlaege")) {
		rot = "&dontRotate=n";
	    }
	    String content = askServer("was=" + key + "&ibarcode=" + ibarcode
		    + "&format=bild" + rot);
	    if ((content != null) && (!content.equals(""))) {
		try {
		    String[] cont = content.split(" \\/{4} ");
		    aus.put(key, new UrlResource(cont[0]));
		    aus.put(key + "Klein", new UrlResource(cont[1]));
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	return aus;
    }

    protected String askServer(String query) {
	String content = "";
	try {
	    URL url = new URL(
		    "http://libwww.mis.mpg.de/cgi-bin/werkstatt/regal/wobuch/getBibliographicInformation.pl?"
			    + query);
	    content = ((String) url.getContent()).replaceFirst(".+?\\n\\n", "");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return content;
    }

    @Override
    public String getCollection() {
	String aus = (String) askDB(0, "");
	return aus;
    }

    @Override
    public Hashtable<String, Resource> getElectronic() {
	Hashtable<String, Resource> aus = new Hashtable<String, Resource>();
	String[] contents = askServer("format=access&bibno=" + bibno).split(
		" \\/{4} ");
	try {
	    String local = contents[0];
	    if (!local.equals("")) {
		aus.put("lokal", new UrlResource(local));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	try {
	    String fremd = contents[1];
	    if (!fremd.equals("")) {
		aus.put("fremd", new UrlResource(fremd));
		String logo = contents[2];
		if (!logo.equals("")) {
		    aus.put("logo", new UrlResource(logo));
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return aus;
    }

    public boolean isBorrowed() {
	return borrowed;
    }

    protected boolean getBorrowed() {
	String aus = (String) askDB(1, "i");
	return aus.equals("o");
    }

    @Override
    public ElectronicRight isElectronic() {
	return rights;
    }

    protected ElectronicRight getRights() {
	return ElectronicRight.kein;
	/*
	 * int zugriffErlaubt=(Integer)askDB(2,-2); if(zugriffErlaubt==2) {
	 * zugriffErlaubt=3; } ElectronicRight[] vals=ElectronicRight.values();
	 * if((zugriffErlaubt>-2)&&(zugriffErlaubt<5)) { return
	 * vals[zugriffErlaubt+1]; } return vals[vals.length-1];
	 */
    }

    @Override
    public boolean isMissing() {
	return missing;
    }

    protected boolean getMissing() {
	int anz = (Integer) askDB(3, 0);
	return !(anz > 0);
    }

    @Override
    public boolean isPrint() {
	return (item > 0);
    }

    @Override
    public void setBibNr(int bibNr) {
	this.bibno = bibNr;
    }

    @Override
    public void setItem(int item) {
	this.item = item;
    }

    @Override
    public void setBarcode(String barcode) {
	this.ibarcode = barcode;
    }

    @Override
    public void setStatements(PreparedStatementT[] stats) {
	this.preparedStatementTs = stats;
    }

}
