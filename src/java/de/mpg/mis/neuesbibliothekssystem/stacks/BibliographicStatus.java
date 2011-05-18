package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;

import org.springframework.core.io.Resource;

public interface BibliographicStatus
{
  public int getBibno();
  
  public String getBarcode();
  
  public boolean isBorrowed();
  
  public boolean isMissing();
  
  public ElectronicRight isElectronic();
  
  public boolean isPrint();
  
  public void refresh();
  
  public String getCollection();
  
  public Hashtable<String,Resource> getElectronic();
  
  public Hashtable<String,Resource> getBibliographicInformation();
  
  public void init();
}
