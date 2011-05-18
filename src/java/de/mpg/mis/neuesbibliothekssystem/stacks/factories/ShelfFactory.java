package de.mpg.mis.neuesbibliothekssystem.stacks.factories;

import java.util.List;
import java.util.Map;

public interface ShelfFactory {

    public abstract Map<String, List<Integer>> getMap(String mapName)
	    throws Exception;

    public abstract List<Integer> getShelfCoords(Integer level, Integer shelfNo)
	    throws Exception;

}