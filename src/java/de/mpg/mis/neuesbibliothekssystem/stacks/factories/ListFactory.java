package de.mpg.mis.neuesbibliothekssystem.stacks.factories;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import de.mpg.mis.neuesbibliothekssystem.stacks.TripleList;

public interface ListFactory {

    public abstract TripleList getUseList() throws Exception;

    public abstract TripleList getUseEbabList() throws Exception;

    public abstract TripleList getMaintainList() throws Exception;

    public abstract TripleList getMaintainEbabList() throws Exception;

}