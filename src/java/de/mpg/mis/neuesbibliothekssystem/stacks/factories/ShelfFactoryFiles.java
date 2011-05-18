package de.mpg.mis.neuesbibliothekssystem.stacks.factories;

import grailsserver.JsonService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

public class ShelfFactoryFiles implements ShelfFactory {

    @Value("${DBMaster.files.E3_BOOKS}")
    private Resource E3_BOOKS_FILE;
    @Value("${DBMaster.files.E3_JOURNALS}")
    private Resource E3_JOURNALS_FILE;
    @Value("${DBMaster.files.E2_JOURNALS}")
    private Resource E2_JOURNALS_FILE;

    private JsonService jsonService;

    @Autowired
    public void setJsonService(JsonService jsonService) {
	this.jsonService = jsonService;
    }

    private Map<String, Map<String, List<Integer>>> shelfMap;

    public ShelfFactoryFiles() throws Exception {
	this.shelfMap = new HashMap<String, Map<String, List<Integer>>>();
	this.shelfMap.put("E3_BOOKS", new HashMap<String, List<Integer>>());
	this.shelfMap.put("E3_JOURNALS", new HashMap<String, List<Integer>>());
	this.shelfMap.put("E2_JOURNALS", new HashMap<String, List<Integer>>());
    }

    @Override
    public Map<String, List<Integer>> getMap(String mapName) throws Exception {
	if (!this.shelfMap.containsKey(mapName))
	    return null;

	if (this.shelfMap.get(mapName).size() == 0)
	    this.fillMapData();
	return this.shelfMap.get(mapName);
    }

    @Override
    public List<Integer> getShelfCoords(Integer level, Integer shelfNo)
	    throws Exception {
	this.fillMapData();
	System.out.println("Got " + level + "/" + shelfNo + " ::: "
		+ this.shelfMap.get("E3_BOOKS").get(shelfNo.toString()));
	switch (level) {
	case 2:
	    return this.shelfMap.get("E2_JOURNALS").get(shelfNo.toString());
	case 3:
	    if (this.shelfMap.get("E3_BOOKS").containsKey(shelfNo.toString()))
		return this.shelfMap.get("E3_BOOKS").get(shelfNo.toString());
	    else
		return this.shelfMap.get("E3_JOURNALS").get(shelfNo.toString());
	default:
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    private void fillMapData() throws Exception {
	if (E3_BOOKS_FILE.exists()) {
	    this.shelfMap.put("E3_BOOKS",
		    (Map<String, List<Integer>>) jsonService
			    .convertToMap(E3_BOOKS_FILE.getFile()));

	}

	if (E3_JOURNALS_FILE.exists()) {
	    this.shelfMap.put("E3_JOURNALS",
		    (Map<String, List<Integer>>) jsonService
			    .convertToMap(E3_JOURNALS_FILE.getFile()));
	}

	if (E2_JOURNALS_FILE.exists()) {
	    this.shelfMap.put("E2_JOURNALS",
		    (Map<String, List<Integer>>) jsonService
			    .convertToMap(E2_JOURNALS_FILE.getFile()));
	}
    }
}
