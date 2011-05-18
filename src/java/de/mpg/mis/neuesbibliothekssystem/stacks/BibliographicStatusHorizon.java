package de.mpg.mis.neuesbibliothekssystem.stacks;

import de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver.ConnectionT.PreparedStatementT;

public interface BibliographicStatusHorizon extends BibliographicStatus {
    public void setItem(int item);

    public void setBibNr(int bibNr);

    public void setBarcode(String barcode);

    public void setStatements(PreparedStatementT[] stats);
}
