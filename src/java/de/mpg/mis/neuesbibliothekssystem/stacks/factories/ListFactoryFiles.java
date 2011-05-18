package de.mpg.mis.neuesbibliothekssystem.stacks.factories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import de.mpg.mis.neuesbibliothekssystem.stacks.TripleList;

public class ListFactoryFiles implements ListFactory {

    /**
     * @param useList
     *            the useList to set
     */
    @Value("${DBMaster.lists.sorterUse}")
    public void setUseList(Resource useList) {
	this.useList = useList;
    }

    /**
     * @param useEbabList
     *            the useEbabList to set
     */
    @Value("${DBMaster.lists.sorterUseEbab}")
    public void setUseEbabList(Resource useEbabList) {
	this.useEbabList = useEbabList;
    }

    /**
     * @param maintainList
     *            the maintainList to set
     */
    @Value("${DBMaster.lists.sorterMaintain}")
    public void setMaintainList(Resource maintainList) {
	this.maintainList = maintainList;
    }

    /**
     * @param maintainList
     *            the maintainList to set
     */
    @Value("${DBMaster.lists.sorterMaintain}")
    public void setMaintainEbabList(Resource maintainEbabList) {
	this.maintainEbabList = maintainEbabList;
    }

    private Resource useList;
    private Resource useEbabList;
    private Resource maintainList;
    private Resource maintainEbabList;

    private TripleList useTriples;
    private TripleList useEbabTriples;
    private TripleList maintainTriples;
    private TripleList maintainEbabTriples;

    public ListFactoryFiles() {
	this.useTriples = null;
	this.useEbabTriples = null;
	this.maintainTriples = null;
	this.maintainEbabTriples = null;
    }

    @Override
    public TripleList getUseList() throws FileNotFoundException, IOException,
	    ClassNotFoundException {
	if (this.useTriples != null)
	    return this.useTriples;
	if (this.useList.exists()) {
	    System.out.println("da!");
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    this.useList.getFile()));
	    this.useTriples = (TripleList) ois.readObject();
	    ois.close();
	    return this.useTriples;
	} else
	    return null;
    }

    @Override
    public TripleList getUseEbabList() throws FileNotFoundException,
	    IOException, ClassNotFoundException {
	if (this.useEbabTriples != null)
	    return this.useEbabTriples;
	if (this.useEbabList.exists()) {
	    System.out.println("da!");
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    this.useEbabList.getFile()));
	    this.useEbabTriples = (TripleList) ois.readObject();
	    ois.close();
	    return this.useEbabTriples;
	} else
	    return null;
    }

    @Override
    public TripleList getMaintainList() throws FileNotFoundException,
	    IOException, ClassNotFoundException {
	System.out.println(this.maintainList.getFilename());
	if (this.maintainTriples != null)
	    return this.maintainTriples;
	if (this.maintainList.exists()) {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    this.maintainList.getFile()));
	    this.maintainTriples = (TripleList) ois.readObject();
	    ois.close();
	    return maintainTriples;
	} else
	    return null;
    }

    @Override
    public TripleList getMaintainEbabList() throws FileNotFoundException,
	    IOException, ClassNotFoundException {
	System.out.println(this.maintainEbabList.getFilename());
	if (this.maintainEbabTriples != null)
	    return this.maintainEbabTriples;
	if (this.maintainEbabList.exists()) {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
		    this.maintainEbabList.getFile()));
	    this.maintainEbabTriples = (TripleList) ois.readObject();
	    ois.close();
	    return maintainEbabTriples;
	} else
	    return null;
    }

}
