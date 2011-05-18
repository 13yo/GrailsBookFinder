package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.ImageObserver;

public interface SpineRepresentation
{
  public Image getSpineImage();
  
  public void setSpineImage(Image image);
  
  public Dimension getSpineImageDimension();
  
  public Dimension getSpineDimensionMm();
  
  public Image getScaledSpineImage();
  
  public boolean isDummy();
  
  public void setIsDummy(boolean isDummy);
  
  public boolean isSpecial();
  
  public void setIsSpecial(boolean isSpecial);
  
  public void scaleSpineImage(float width,float height,int hints,ImageObserver observer);
  
  public BibliographicStatus getBibliographicStatus();
  
  public void setBibliographicStatus(BibliographicStatus bibliographicStatus);
  
  public void init();
}
