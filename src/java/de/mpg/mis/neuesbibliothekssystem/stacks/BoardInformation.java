package de.mpg.mis.neuesbibliothekssystem.stacks;

public interface BoardInformation
{
  public void setFirstBarcode(String barcode);
  
  public String getFirstBarcode();
  
  public void setBoardWidthMm(int boardWidthMm);
  
  public int getBoardWidthMm();
  
  public void setBoardHeightMm(int boardHeightMm);
  
  public int getBoardHeightMm();
  
  public void setBoardName(String name);
  
  public String getBoardName();
}
