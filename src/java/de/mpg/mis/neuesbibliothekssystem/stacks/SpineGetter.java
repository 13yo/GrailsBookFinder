package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.util.LinkedList;

import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT;

;

public interface SpineGetter {
    public void setConnection(ConnectionT connection);

    public void setLocationBorders(int floorFrom, int shelfFrom, int boardFrom,
	    int floorTo, int shelfTo, int boardTo) throws Exception;

    public LinkedList<SpineRepresentation> getSpines();

    public LinkedList<BoardInformation> getBoardInformation();

    public void init();
}
