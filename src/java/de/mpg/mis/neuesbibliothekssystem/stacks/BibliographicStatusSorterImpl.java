package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.util.Hashtable;

import org.springframework.core.io.Resource;

public class BibliographicStatusSorterImpl implements BibliographicStatus
{
  protected int bibno;
  protected String ibarcode;
  protected String status;
  protected String collection;
  
  public BibliographicStatusSorterImpl(int bibno,String ibarcode,String status,String collection)
  {
    this.bibno=bibno;
    this.ibarcode=ibarcode;
    this.status=status;
    this.collection=collection;
  }
  @Override
  public int getBibno()
  {
    // TODO Auto-generated method stub
    return bibno;
  }

  @Override
  public String getBarcode()
  {
    // TODO Auto-generated method stub
    return ibarcode;
  }

  @Override
  public boolean isBorrowed()
  {
    // TODO Auto-generated method stub
    return status.equals("o");
  }

  @Override
  public boolean isMissing()
  {
    // TODO Auto-generated method stub
    return status.equals("m");
  }

  @Override
  public ElectronicRight isElectronic()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isPrint()
  {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public void refresh()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public String getCollection()
  {
    // TODO Auto-generated method stub
    return collection;
  }

  @Override
  public Hashtable<String, Resource> getElectronic()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Hashtable<String, Resource> getBibliographicInformation()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void init()
  {
    // TODO Auto-generated method stub

  }

}
