package de.mpg.mis.neuesbibliothekssystem.stacks;

public class BoardInformationImpl implements BoardInformation
{
  protected int boardHeightMm;
  protected int boardWidthMm;
  protected String firstBarcode;
  protected String name;
  
  public BoardInformationImpl(String firstBarcode,String name,int boardWidthMm,int boardHeightMm)
  {
    setFirstBarcode(firstBarcode);
    setBoardName(name);
    setBoardWidthMm(boardWidthMm);
    setBoardHeightMm(boardHeightMm);
  }
  
  public BoardInformationImpl()
  {
    
  }
  
  @Override
  public int getBoardHeightMm()
  {
    return boardHeightMm;
  }

  @Override
  public String getBoardName()
  {
    return name;
  }

  @Override
  public int getBoardWidthMm()
  {
    return boardWidthMm;
  }

  @Override
  public String getFirstBarcode()
  {
    return firstBarcode;
  }

  @Override
  public void setBoardName(String name)
  {
    this.name=name;
  }

  @Override
  public void setBoardHeightMm(int boardHeightMm)
  {
    this.boardHeightMm=boardHeightMm;
  }

  @Override
  public void setBoardWidthMm(int boardWidthMm)
  {
    this.boardWidthMm=boardWidthMm;
  }

  @Override
  public void setFirstBarcode(String barcode)
  {
    this.firstBarcode=barcode;
  }

}
