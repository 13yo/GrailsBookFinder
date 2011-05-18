package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT;
import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT.PreparedStatementT;

public class SpineGetterImpl implements SpineGetter {
    protected ConnectionT connection;
    protected int floorFrom;
    protected int shelfFrom;
    protected int boardFrom;
    protected int floorTo;
    protected int shelfTo;
    protected int boardTo;
    protected PreparedStatementT[] spineStats = new PreparedStatementT[BibliographicStatusImpl.psTs.length];
    protected PreparedStatementT ps0;
    protected PreparedStatementT ps1;
    protected PreparedStatementT ps2;
    private PreparedStatementT ps3;

    public SpineGetterImpl(ConnectionT connection) {
	setConnection(connection);
    }

    protected void start() {
	try {
	    for (int a = 0; a < spineStats.length; a++) {
		spineStats[a] = connection
			.prepareStatement(BibliographicStatusImpl.psTs[a]);
	    }
	    ps0 = connection
		    .prepareStatement("select a.item#,a.ibarcode,a.bib#,z.processed from horizon..item a,horizon..call z where a.item#=z.item# and a.item# in (select b.item# from horizon..call b where processed between (select c.processed from horizon..call c,horizon..item d,regal_plan_pos1 e where c.item#=d.item# and d.ibarcode=e.ibarcode and e.ebene=? and e.regal=? and e.brett=?) and (select f.processed from horizon..call f,horizon..item g,regal_plan_pos1 h where f.item#=g.item# and g.ibarcode=h.ibarcode and h.ebene=? and h.regal=? and h.brett=?)) and a.collection=? order by z.processed,a.item#");
	    ps1 = connection
		    .prepareStatement("select a.tag,b.processed from horizon..bib a,horizon..author b where a.auth#=b.auth# and a.tag in (100,700) and a.tagord=0 and a.bib#=? order by a.tag");
	    ps2 = connection
		    .prepareStatement("select processed from horizon..title where bib#=?");
	    ps3 = connection
		    .prepareStatement("select a.ibarcode,a.ebene,a.regal,a.brett,b.laenge_cm from regal_plan_pos1 a,regal_laenge b where (?<? and a.ebene=? and (a.regal=? and a.brett>?-1 or a.regal>?)) or (a.ebene<? and a.ebene>?) or (?<? and a.ebene=? and (a.regal<? or a.regal=? and a.brett<?+1)) or (?=? and (a.regal>? and a.regal<? or a.regal=? and a.brett>?-1 and a.regal=? and a.brett<?+1)) and a.ebene=b.ebene and a.regal=b.regal# and b.date=12263 order by a.ebene,a.regal,a.brett");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void setLocationBorders(int floorFrom, int shelfFrom, int boardFrom,
	    int floorTo, int shelfTo, int boardTo) throws Exception {
	if ((floorFrom > floorTo)) {
	    throw new Exception("floor mismatch");
	}
	if ((floorFrom == floorTo) && (shelfFrom > shelfTo)) {
	    throw new Exception("shelf mismatch");
	}
	if ((shelfFrom == shelfTo) && (boardFrom > boardTo)) {
	    throw new Exception("board mismatch");
	}
	this.floorFrom = floorFrom;
	this.shelfFrom = shelfFrom;
	this.boardFrom = boardFrom;
	this.floorTo = floorTo;
	this.shelfTo = shelfTo;
	this.boardTo = boardTo;
    }

    @Override
    public LinkedList<BoardInformation> getBoardInformation() {
	LinkedList<BoardInformation> aus = new LinkedList<BoardInformation>();
	try {
	    ps3.setInt(1, floorFrom);
	    ps3.setInt(2, floorTo);
	    ps3.setInt(3, floorFrom);
	    ps3.setInt(4, shelfFrom);
	    ps3.setInt(5, boardFrom);
	    ps3.setInt(6, shelfFrom);
	    ps3.setInt(7, floorTo);
	    ps3.setInt(8, floorFrom);
	    ps3.setInt(9, floorFrom);
	    ps3.setInt(10, floorTo);
	    ps3.setInt(11, floorTo);
	    ps3.setInt(12, shelfTo);
	    ps3.setInt(13, shelfTo);
	    ps3.setInt(14, boardTo);
	    ps3.setInt(15, floorFrom);
	    ps3.setInt(16, floorTo);
	    ps3.setInt(17, shelfFrom);
	    ps3.setInt(18, shelfTo);
	    ps3.setInt(19, shelfFrom);
	    ps3.setInt(20, boardFrom);
	    ps3.setInt(21, shelfFrom);
	    ps3.setInt(22, boardTo);
	    ResultSet rs = ps3.executeQuery();
	    while (rs.next()) {
		String barcode = rs.getString(1);
		String name = rs.getString(2) + ":" + rs.getString(3) + ","
			+ rs.getString(4);
		aus.add(new BoardInformationImpl(barcode, name,
			rs.getInt(5) * 10, 300));
	    }
	    rs.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return aus;
    }

    @Override
    public LinkedList<SpineRepresentation> getSpines() {
	LinkedList<SpineRepresentation> aus = new LinkedList<SpineRepresentation>();
	try {
	    ps0.setInt(1, floorFrom);
	    ps0.setInt(2, shelfFrom);
	    ps0.setInt(3, boardFrom);
	    ps0.setInt(4, floorTo);
	    ps0.setInt(5, shelfTo);
	    ps0.setInt(6, boardTo);
	    ps0.setString(7, "mo");
	    ResultSet rs = ps0.executeQuery();
	    String callAlt = null;
	    SpineRepresentation spine = null;
	    LinkedList<SpineRepresentation> zwi = new LinkedList<SpineRepresentation>();
	    while (rs.next()) {
		spine = new SpineRepresentationImpl(
			new BibliographicStatusImpl(rs.getInt(1),
				rs.getString(2), rs.getInt(3), spineStats),
			false, false);
		String call = rs.getString(4);
		if ((callAlt != null) && (!callAlt.equals(call))) {
		    aus.addAll(sortAuthor(zwi, callAlt));
		    zwi = new LinkedList<SpineRepresentation>();
		}
		zwi.add(spine);
		callAlt = call;
	    }
	    aus.addAll(sortAuthor(zwi, callAlt));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return aus;
    }

    protected class SortElement implements Comparable<SortElement> {
	public Comparable criterion;
	public Object object;

	public SortElement(Comparable criterion, Object object) {
	    this.criterion = criterion;
	    this.object = object;
	}

	public int compareTo(SortElement element) {
	    return criterion.compareTo(element.criterion);
	}
    }

    protected LinkedList<SpineRepresentation> sortAuthor(
	    LinkedList<SpineRepresentation> list, String call) {
	LinkedList<SpineRepresentation> aus = new LinkedList<SpineRepresentation>();
	try {
	    ListIterator<SpineRepresentation> li = list.listIterator();
	    SortElement[] sorteds = new SortElement[list.size()];
	    int z = 0;
	    while (li.hasNext()) {
		SpineRepresentation spine = li.next();
		int bibno = spine.getBibliographicStatus().getBibno();
		ps2.setInt(1, bibno);
		ResultSet rs = ps2.executeQuery();
		String title = "";
		if (rs.next()) {
		    title = rs.getString(1);
		}
		rs.close();
		String sortString = "";
		if (title.indexOf(call.replaceFirst(".+ ", "")) == 0) {
		    sortString = title;
		} else {
		    ps1.setInt(1, bibno);
		    rs = ps1.executeQuery();
		    String author = "";
		    if (rs.next()) {
			author = rs.getString(2);
		    }
		    rs.close();
		    sortString = author + " " + title;
		}
		sorteds[z++] = new SortElement(sortString, spine);
	    }
	    Arrays.sort(sorteds);
	    for (int a = 0; a < sorteds.length; a++) {
		aus.add((SpineRepresentation) sorteds[a].object);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	    aus = list;
	}
	return aus;
    }

    @Override
    public void setConnection(ConnectionT connection) {
	this.connection = connection;
	start();
    }

    @Override
    public void init() {
	// TODO Auto-generated method stub

    }

}
