package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FilterInputStream;
import java.net.URL;

import javax.swing.JFrame;

public class SpineRepresentationImpl implements SpineRepresentation
{
  protected static Zwi zwi=new Zwi();
  protected static double spineFactor=0.254629;
  protected BibliographicStatus status;
  protected Image image;
  protected Dimension dimension;
  protected Dimension spineDimensionMm;
  protected Image scaledImage;
  protected Image imageKlein;
//  protected boolean klein=true;
  protected boolean klein=false;
  protected boolean isSpecial=false;
  protected boolean isDummy=false;
  
  public SpineRepresentationImpl(BibliographicStatus status,boolean isSpecial,boolean isDummy)
  {
    this.status=status;
    this.isSpecial=isSpecial;
    this.isDummy=isDummy;
//    zwi.setVisible(true);
//    zwi.setSize(500,500);
//    zwi.setLocation(100,100);
    createImage();
  }
  
  public SpineRepresentationImpl()
  {
    
  }
  
  public void init()
  {
    if(image==null)
    {
      createImage();
    }
  }
  
  protected void createImage()
  {
    /*
    String[] urls=askServer("was=ruecken&format=bild&ibarcode="+status.getBarcode()).split(" \\/{4} ");
    System.out.println("SpineRepresentation:createImage:"+urls[0].toString()+" ... "+urls[1].toString());
    */
    try
    {
      /*
      image=Toolkit.getDefaultToolkit().getImage(new URL(urls[0]));
      scaledImage=image;
      imageKlein=Toolkit.getDefaultToolkit().getImage(new URL(urls[1]));
      */
      String fileName="y:\\inhaltUmschlag\\ruecken\\"+status.getBarcode()+".jpg";
      File file=new File(fileName);
      if(!file.exists())
      {
        fileName=fileName.replaceFirst("\\.jpg",".JPG");
        if(!file.exists())
        {
          throw new Exception(fileName+"gibts nicht.");
        }
      }
      image=Toolkit.getDefaultToolkit().getImage(fileName);
      scaledImage=image;
      imageKlein=image.getScaledInstance(5,20,Image.SCALE_FAST);
     }
     catch(Exception ex)
     {
       if(image==null)
       {
         image=new BufferedImage(50,600,BufferedImage.TYPE_BYTE_BINARY);
         scaledImage=image;
         imageKlein=image.getScaledInstance(5,20,Image.SCALE_FAST);
       }
       ex.printStackTrace(); 
    }
    zwi.setImage(image);
    zwi.repaint();
    dimension=new Dimension(image.getWidth(zwi),image.getHeight(zwi));
    while(dimension.width==-1)
    {
      dimension=new Dimension(image.getWidth(zwi),image.getHeight(zwi));
    }
    if(dimension.width>dimension.height)
    {
      dimension=new Dimension(dimension.height,dimension.width);
      BufferedImage bi=new BufferedImage(dimension.height,dimension.width,BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g=(Graphics2D)bi.getGraphics();
      g.setTransform(AffineTransform.getRotateInstance(Math.PI/2,dimension.width,dimension.height));
      g.drawImage(image,0,0,null);
      image=bi;
      scaledImage=image;
      imageKlein=image.getScaledInstance(5,20,Image.SCALE_FAST);
    }
    spineDimensionMm=new Dimension((int)(dimension.width*spineFactor),(int)(dimension.height*spineFactor));
    System.out.println("SpineRepresentationImpl:createImage:"+dimension+" / "+spineDimensionMm);
  }
  
  public Dimension getSpineImageDimension()
  {
    return dimension;
  }
  
  public Dimension getSpineDimensionMm()
  {
    return spineDimensionMm;
  }
  
  public void setKlein(boolean klein)
  {
    this.klein=klein;
  }
  
  public boolean getKlein()
  {
    return klein;
  }
  
  public void scaleSpineImage(float width,float height,int hints,ImageObserver observer)
  {
    try
    {
      int swidth=(int)(image.getWidth(observer)*width);
      int sheight=(int)(image.getHeight(observer)*height);
      scaledImage=image.getScaledInstance(swidth, sheight, hints);
      System.out.println("SpinreRepresentationImpl:scaleSpineImage: "+swidth+"/"+sheight);
     }
     catch(Exception ex)
     {
       ex.printStackTrace(); 
    }
  }
  
  @Override
  public BibliographicStatus getBibliographicStatus()
  {
    return status;
  }

  @Override
  public Image getScaledSpineImage()
  {
    if(klein)
    {
      return imageKlein;
    }
    return scaledImage;
  }
  
  public Image getSpineImage()
  {
    return image;
  }
  
  public boolean isDummy()
  {
    return isDummy;
  }
  
  public boolean isSpecial()
  {
    return isSpecial;
  }
  
  protected String askServer(String query)
  {
    String content="";
    try
    {
      URL url=new URL("http://libwww.mis.mpg.de/cgi-bin/werkstatt/regal/wobuch/getBibliographicInformation.pl?"+query);
      content=readStream(((FilterInputStream)url.getContent())).replaceFirst(".+?\\n\\n","");
     }
     catch(Exception ex)
     {
       ex.printStackTrace(); 
    }
     return content;
  }
  
  protected String readStream(FilterInputStream stream)
  {
    StringBuffer aus=new StringBuffer();
    try
    {
      char c=(char)stream.read();
      while((c>0)&&(c<65535))
      {
        aus.append(c);
//        System.out.println((int)c);
        c=(char)stream.read();
        
      }
      stream.close();
     }
     catch(Exception ex)
     {
       ex.printStackTrace(); 
    }
    return aus.toString();
  }
  
  protected static class Zwi extends JFrame
  {
    protected Image image;
    
    public void setImage(Image img)
    {
      image=img;
    }
    
    public void paint(Graphics g)
    {
      g.drawImage(image,0,0,this);
    }
  }

  @Override
  public void setBibliographicStatus(BibliographicStatus bibliographicStatus)
  {
    this.status=bibliographicStatus;    
  }

  @Override
  public void setIsDummy(boolean isDummy)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setIsSpecial(boolean isSpecial)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setSpineImage(Image image)
  {
    // TODO Auto-generated method stub
    
  }
}
