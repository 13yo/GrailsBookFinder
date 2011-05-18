package de.mpg.mis.neuesbibliothekssystem.stacks.factories;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT;
import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT.PreparedStatementT;
import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.DBConnectorChooserLib;

public class ShelfFactoryDatabase implements ShelfFactory {

    @Value("${DBMaster.queries.E3_BOOKS_QUERY}")
    private String E3_BOOKS_QUERY;
    @Value("${DBMaster.queries.E3_JOURNALS_QUERY}")
    private String E3_JOURNALS_QUERY;
    @Value("${DBMaster.queries.E2_JOURNALS_QUERY}")
    private String E2_JOURNALS_QUERY;

    private Map<String, Map<String, List<Integer>>> shelfMap;
    private Map<String, String> queriesMap;

    public ShelfFactoryDatabase() {
	this.shelfMap = new HashMap<String, Map<String, List<Integer>>>();
	this.shelfMap.put("E3_BOOKS", new HashMap<String, List<Integer>>());
	this.shelfMap.put("E3_JOURNALS", new HashMap<String, List<Integer>>());
	this.shelfMap.put("E2_JOURNALS", new HashMap<String, List<Integer>>());

	this.queriesMap = new HashMap<String, String>();
    }

    private ResultSet statement(String statement) throws Exception {
	DBConnectorChooserLib dbConnector = new DBConnectorChooserLib();
	ConnectionT con = new ConnectionT(dbConnector.getConnector("horizon")
		.getConnection());
	PreparedStatementT psItem = con.prepareStatement(statement);
	return psItem.executeQuery();
    }

    @Override
    public Map<String, List<Integer>> getMap(String mapName) throws Exception {
	if (!this.shelfMap.containsKey(mapName))
	    return null;

	if (this.shelfMap.get(mapName).size() == 0)
	    this.fillMapData(mapName);
	return this.shelfMap.get(mapName);
    }

    @Override
    public List<Integer> getShelfCoords(Integer level, Integer shelfNo)
	    throws Exception {
	System.out.println("Got " + level + "/" + shelfNo.toString());
	switch (level) {
	case 2:
	    if (this.shelfMap.get("E2_JOURNALS").size() == 0)
		this.fillMapData("E2_JOURNALS");
	    return this.shelfMap.get("E2_JOURNALS").get(shelfNo.toString());
	case 3:
	    if (this.shelfMap.get("E3_BOOKS").size() == 0)
		this.fillMapData("E3_BOOKS");
	    if (this.shelfMap.get("E3_BOOKS").containsKey(shelfNo.toString()))
		return this.shelfMap.get("E3_BOOKS").get(shelfNo.toString());
	    else {
		if (this.shelfMap.get("E3_JOURNALS").size() == 0)
		    this.fillMapData("E3_JOURNALS");
		return this.shelfMap.get("E3_JOURNALS").get(shelfNo.toString());
	    }
	default:
	    return null;
	}
    }

    private void fillMapData(String mapName) throws Exception {
	if (this.queriesMap.size() == 0) {
	    this.queriesMap.put("E3_BOOKS", this.E3_BOOKS_QUERY);
	    this.queriesMap.put("E3_JOURNALS", this.E3_JOURNALS_QUERY);
	    this.queriesMap.put("E2_JOURNALS", this.E2_JOURNALS_QUERY);
	}

	ResultSet rs = this.statement(this.queriesMap.get(mapName));
	Map<String, List<Integer>> m = new HashMap<String, List<Integer>>();
	while (rs.next()) {
	    List<Integer> l = new ArrayList<Integer>();
	    m.put(rs.getString(1), l);
	    l.add(rs.getInt(2));
	    l.add(rs.getInt(3));
	    l.add(rs.getInt(4));
	    l.add(rs.getInt(5));
	}
	this.shelfMap.put(mapName, m);
    }
}
