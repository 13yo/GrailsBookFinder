package de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver;

import java.util.*;

public class DBConnectorChooserLib {
	private Hashtable hash = new Hashtable();

	public DBConnectorChooserLib() throws Exception {
		hash.put("training", new DBConnector("Sybase", "training", "sa",
				"4libri+99", "194.95.184.5", 5000));
		hash.put("horizon", new DBConnector("Sybase", "horizon", "sa",
				"4libri+99", "194.95.184.5", 5000));
		hash.put("bibliodb", new DBConnector("Oracle", "ingo", "ib0208",
				"bibliodb", "dbs01.mis.mpg.de", 1521));
		hash.put("mpimisdb", new DBConnector("Oracle", "ingo", "ingo2005",
				"mpimisdb", "dbs01.mis.mpg.de", 1521));
		hash.put("typo", new DBConnector("Mysql", "typo3_biblio",
				"ibTYPO3biblio", "typo3_biblio", "typo.mis.mpg.de", 3306));
	}

	public DBConnector getTrainingConnector() {
		return this.getConnector("training");
	}

	public DBConnector getConnector(String name) {
		return (DBConnector) hash.get(name);
	}
}
