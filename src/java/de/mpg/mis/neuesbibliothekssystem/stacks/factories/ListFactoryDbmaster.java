package de.mpg.mis.neuesbibliothekssystem.stacks.factories;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import de.mpg.mis.neuesbibliothekssystem.stacks.DoMaintain;
import de.mpg.mis.neuesbibliothekssystem.stacks.SpineGetter;
import de.mpg.mis.neuesbibliothekssystem.stacks.SpineGetterSorterImpl;
import de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterMaintain;
import de.mpg.mis.neuesbibliothekssystem.stacks.StackSorterUse;
import de.mpg.mis.neuesbibliothekssystem.stacks.TripleList;

public class ListFactoryDbmaster implements ListFactory {

    private DoMaintain doMaintain;

    @Autowired
    public void setDoMaintain(DoMaintain doMaintain) {
	this.doMaintain = doMaintain;
    }

    private SpineGetterSorterImpl spineGetter;

    @Autowired
    public void setSpineGetterSorter(SpineGetterSorterImpl sg) {
	this.spineGetter = sg;
    }

    private StackSorterUse stackSorter;
    private StackSorterMaintain stackMaintainer;

    @Override
    public TripleList getUseList() throws Exception {
	if (stackSorter == null)
	    initSorter();
	return stackSorter.getTriples();
    }

    @Override
    public TripleList getUseEbabList() throws Exception {
	if (stackSorter == null)
	    initSorter();
	return stackSorter.getEbabTriples();
    }

    @Override
    public TripleList getMaintainList() throws Exception {
	if (stackMaintainer == null)
	    initMaintainer();
	return stackMaintainer.getTriples();
    }

    @Override
    public TripleList getMaintainEbabList() throws Exception {
	if (stackMaintainer == null)
	    initMaintainer();
	return stackMaintainer.getEbabTriples();
    }

    public ListFactoryDbmaster() {
	this.stackMaintainer = null;
	this.stackSorter = null;
    }

    public void initMaintainer() throws Exception {
	this.doMaintain.init();
	this.stackMaintainer = this.doMaintain.getMaintainer();
	this.stackMaintainer.setCheckForUpdate(true);
	this.doMaintain.start(false);
    }

    public void initSorter() throws Exception {
	// initMaintainer();
	this.stackSorter = this.spineGetter.getStackSorter();
	this.stackSorter.init();
    }
}
