package de.mpg.mis.neuesbibliothekssystem.stacks.xml;

import java.util.*;
import java.util.regex.*;

public class XMLParser implements ParsingInterface
{
  protected static String comment="<!--.+?-->";
  protected static Pattern commentPat=Pattern.compile(comment,Pattern.DOTALL);
  protected static Pattern cdata=Pattern.compile("<!\\[CDATA\\[.+?\\]\\]>",Pattern.DOTALL);
  protected static Pattern entity=Pattern.compile("&[^\\s]+?;");
  protected static String sTagAuf="<\\s*[^!][^\\s>]*";
  protected static Pattern sTagAufPat=Pattern.compile(sTagAuf);
  protected static String tagZu="\\s*>";
  protected static String emptyTagZu="\\s*/>";
  protected static Pattern emptyTagPat=Pattern.compile("<[^>]+/>",Pattern.DOTALL);
  protected static String eTagAuf="</\\s*";
  protected static Pattern tagAuf=Pattern.compile(sTagAuf+"[^><]*>");
  protected static Pattern tagName=Pattern.compile(sTagAuf);
  protected static String tagNameWeg="<\\s*";
  protected static String haekchen="(('[^']*')|(\"[^\"]*\"))";
  protected static String attval="\\s*=\\s*";
  protected static Pattern attvalPat=Pattern.compile(attval);
  protected static Pattern attribut=Pattern.compile("[^\\s]+?"+attval+haekchen);
  protected static Pattern blacklist=Pattern.compile("[\\s'\"$%§&;!?]");
  //in Häkchen gesetzte '>' etwa in Attributwerten sind wohl valide. Das muss bei '[^>]' natürlich berücksichtigt werden! Bsp.: http://www.springerlink.com/content/978-3-540-79889-7. Siehe auch getEmptyTag().
  protected static Pattern sTagPat0=Pattern.compile("<[^>]*>");
  protected static Pattern sTagPat=Pattern.compile("\\A<[^>]*>");
  protected static String eTagStr=eTagAuf+"[^>]*?>\\z";
  protected static Pattern content=Pattern.compile(">[^\\<]+<");
  protected static String gansWeg="((\\A[\"'])|([\"']\\z))";

  protected Hashtable cdataEntities;
  protected LinkedList<String> entities;
  protected LinkedList<Element> textStuecke;
  protected Element oberst;
  protected String dtd;
  
  private boolean searchWeiter=true;
  private Element searchElement=null;

  public boolean pingelig=false;

  public XMLParser(String xml,String dtd) throws Exception
  {
    this.dtd=dtd;
    textStuecke=new LinkedList<Element>();
    oberst=new Element(normEmptyTags(preparse("<oberst>"+fillUp(xml,dtd)+"</oberst>")),new Element());
  }

  protected String fillUp(String xml,String dtd)
  {
    return xml;
  }

  public final LinkedList<Element> getTextStuecke()
  {
    return (LinkedList<Element>)textStuecke.clone();
  }

  public Element getOberst()
  {
    return oberst;
  }

  public static final String[] getEmptyTag(String elementName,String xml) throws Exception
  {
    if(isElementName(elementName))
    {
      Pattern pat=Pattern.compile("<\\s*"+elementName+"\\s[^>]+/>");
//      Pattern pat=Pattern.compile("<\\s*"+elementName+"\\s.+?/>");
      Matcher mat=pat.matcher(xml);
      String[] aus={xml,"",""};
      if(mat.find())
      {
        aus[1]=mat.group();
        aus[0]=xml.substring(0,mat.start());
        aus[2]=xml.substring(mat.end(),xml.length());
      }
      return aus;
     }
     else
     {
       throw new Exception("no element name");
    } 
  }
  
  public static final String[] getTagContent(String elementName,String xml,boolean pingelig) throws Exception
  {
    String[] aus=getTag(elementName,xml,pingelig);
    if(aus[1]!=null)
    {
      aus[1]=aus[1].replaceFirst("\\A<[^>]+>","").replaceFirst("<\\/[^>]+>\\z","");
    }
    return aus;
  }
  
  public static final String[] getTag(String elementName,String xml,boolean pingelig) throws Exception
  {
    if(isElementName(elementName))
    {
      String[] aus=getEmptyTag(elementName,xml);
      if(!aus[1].equals(""))
      {
        return aus;
      }
      Pattern pat=Pattern.compile("<\\s*"+elementName+"[\\s>]");
      Matcher mat=pat.matcher(xml);
      if(mat.find())
      {
        StringBuffer treff=new StringBuffer(mat.group());
        StringBuffer rest=new StringBuffer(xml.substring(0,mat.start()));
        Pattern patZu=Pattern.compile(eTagAuf+elementName+tagZu);
        xml=xml.substring(mat.end(),xml.length());
        Matcher matZu=patZu.matcher(xml);
        int auf=1;
        int zu=0;
        while((auf-zu>0)&&matZu.find())
        {
          zu=zu+1;
          String zwi=xml.substring(0,matZu.start())+matZu.group();
          treff.append(zwi);
          xml=xml.substring(matZu.end(),xml.length());
          matZu=patZu.matcher(xml);
          mat=pat.matcher(zwi);
          while(mat.find())
          {
            auf=auf+1;
          }
        }
        if(auf!=zu)
        {
          if(pingelig)
          {
            throw new Exception("malformed");
          }  
        }
        aus[1]=treff.toString();
        aus[0]=rest.toString();
        aus[2]=xml;
      }
      return aus;
     }
     else
     {
       throw new Exception("no element name:"+elementName+".");
    } 
  }

  public static final String getTag(String xml,boolean pingelig) throws Exception
  {
    Matcher mat=sTagAufPat.matcher(xml);
    if(mat.find())
    {
      return getTag(getTagName(mat.group()),xml,pingelig)[1];
    }
    return xml;
  }

  public static boolean isElementName(String name)
  {
    Matcher mat=blacklist.matcher(name);
    return !mat.find();
  }

  protected String resolveEntities(LinkedList<String> ents,String xml) throws Exception
  {
    return resolveCdataEntities(resolveExternalEntities(ents,xml));
  }

  protected String resolveExternalEntities(LinkedList<String> ents,String xml) throws Exception
  {
    return xml;
  }

  protected String resolveCdataEntities(String xml)
  {
    Matcher mat=entity.matcher(xml);
    StringBuffer aus=new StringBuffer();
    while(mat.find())
    {
      aus.append(xml.substring(0,mat.start()));
      String enti=mat.group();
      String auf=(String)cdataEntities.get(enti);
      if(auf!=null)
      {
        aus.append(auf);
       }
       else
       {
         aus.append(enti);
      }
      xml=xml.substring(mat.end(),xml.length());
      mat=entity.matcher(xml);
    }
    aus.append(xml);
    return aus.toString();
  }

  protected String preparse(String ein) throws Exception
  {
    cdataEntities=new Hashtable();
    entities=new LinkedList<String>();
    LinkedList<Cdat> reste=new LinkedList<Cdat>();
//    String zwi=ein.replaceAll(comment,"");
    String zwi=commentPat.matcher(ein).replaceAll("");
    Matcher mat=cdata.matcher(zwi);
    while(mat.find())
    {
      reste.add(new Cdat(zwi.substring(0,mat.start()),mat.group()));
      zwi=zwi.substring(mat.end(),zwi.length());
      mat=cdata.matcher(zwi);
    }
    if(reste.size()>0)
    {
      ListIterator<Cdat> li=reste.listIterator();
      StringBuffer buffer=new StringBuffer();
      while(li.hasNext())
      {
        buffer.append(li.next().rest);
      }
      entities=getEntities(buffer.toString());
      Object[] arr=entities.toArray();
      if(arr.length>0)
      {
        Arrays.sort(arr);
        StringBuffer names=new StringBuffer((String)arr[arr.length-1]+"a");
        li=reste.listIterator();
        buffer=new StringBuffer();
        while(li.hasNext())
        {
          Cdat cdat=li.next();
          buffer.append(cdat.rest+"&");
          buffer.append(names+";");
          cdataEntities.put(names.toString(),cdat.cdat);
          names.append("a");
        }
      }
      buffer.append(zwi);
      return buffer.toString();
     }
     else
     {
       return zwi;
    }
  }
  
  protected String normEmptyTags(String xml) throws Exception
  {
    StringBuffer aus=new StringBuffer();
    Matcher mat=emptyTagPat.matcher(xml);
    while(mat.find())
    {
      aus.append(xml.substring(0,mat.start()));
      String group=mat.group();
      String name=getTagName(group);
      group=group.replaceFirst("/>","></"+name+">");
      aus.append(group);
      xml=xml.substring(mat.end(),xml.length());
      mat=emptyTagPat.matcher(xml);
    }
    aus.append(xml);
    return aus.toString();
  }

  public static LinkedList<String> getEntities(String ein)
  {
    Matcher mat=entity.matcher(ein);
    LinkedList<String> aus=new LinkedList<String>();
    while(mat.find())
    {
      String group=mat.group();
      if(!aus.contains(group))
      {
        aus.add(group);
      }
    }
    return aus;
  }

  protected class Cdat
  {
    public String rest;
    public String cdat;

    public Cdat(String a,String b)
    {
      rest=a;
      cdat=b;
    }
  }

  public static String getTagName(String ein) throws Exception
  {
    Matcher mat=tagName.matcher(ein);
    if(mat.find())
    {
      return mat.group().replaceFirst(tagNameWeg,"");
     }
     else
     {
       throw new Exception("kein tagName");
    } 
  }

  public static Hashtable<String,String> getTagAttribute(String ein) throws Exception
  {
//	System.out.print("roh:"+ein+"...");
    Hashtable<String,String> aus=new Hashtable<String,String>();
    Matcher mat=attribut.matcher(ein);
    while(mat.find())
    {
      String group=mat.group();
      Matcher matAtv=attvalPat.matcher(group);
      if(matAtv.find())
      {
    	String vor=group.substring(0,matAtv.start());
    	String nach=group.substring(matAtv.end(),group.length());
        aus.put(vor,nach.replaceAll("(\\A['\"])|(['\"]\\z)",""));
//        System.out.print(vor+":"+nach+"\t");
      }
      /*
      String[] av=mat.group().split(attval);
      if(av.length>1)
      {
        aus.put(av[0],av[1].replaceAll("(\\A['\"])|(['\"]\\z)",""));
        System.out.print(av[0]+":"+av[1]+"\t");
      } 
      */ 
    }
//    System.out.println();
    return aus;
  }
  
  public Element searchElement(Element start,String tag,String attribut,String value,String content,boolean lowerCase)
  {
  	searchWeiter=true;
  	searchElement=null;
  	searchElementRec(start,tag,attribut,value,content,lowerCase);
  	return searchElement;
  }
  
  public void searchElementRec(Element start,String tag,String attribut,String value,String content,boolean lowerCase)
  {
  	if(start.check(tag,attribut,value,content,lowerCase))
  	{
  		searchWeiter=false;
  		searchElement=start;
  	 }
  	 else
  	 {
    	 LinkedList<Element> untere=start.getUnter();
  	   ListIterator<Element> li=untere.listIterator();
  	   while(searchWeiter&&li.hasNext())
     	{
  		  searchElementRec(li.next(),tag,attribut,value,content,lowerCase);
  	  }
  	}
  }
  
  public class Element
  {
    public class Elementkopf
    {
      protected String name;
      protected Hashtable<String,String> attribute;

      public Elementkopf(String roh) throws Exception
      {
//       System.out.println("roh:"+roh);
        Matcher mat=tagAuf.matcher(roh);
        if(mat.find())
        {
          roh=mat.group();
          name=getTagName(roh);
//          System.out.println("roh:"+roh+"...name:"+name);
          attribute=getTagAttribute(roh);
         }
         else
         {
           System.out.println("Fehler:roh:"+roh);
           throw new Exception("kein tagAuf");
        } 
      }

      protected Elementkopf(String n,Hashtable<String,String> h)
      {
        name=n;
        attribute=h;
      }

      public String getName()
      {
        return name;
      }

      public Hashtable<String,String> getAttribute()
      {
        return (Hashtable<String,String>)attribute.clone();
      }

      public final Elementkopf clone()
      {
        return new Elementkopf(name,(Hashtable<String,String>)attribute.clone());
      }
      
      public String toString()
      {
      	StringBuffer aus=new StringBuffer("<"+name);
      	Set<String> keys=attribute.keySet();
      	Iterator<String> it=keys.iterator();
      	while(it.hasNext())
      	{
      		String key=it.next();
      		String value=attribute.get(key);
      		aus.append(" "+key+"='"+value+"'");
      	}
      	aus.append(">");
      	return aus.toString();
      }
    }

    protected Elementkopf kopf;
    protected Element ober;
    protected LinkedList<Element> unter;
    protected String wert="";

    public Element()
    {
      unter=new LinkedList<Element>();
    }

    protected Element(Elementkopf k,Element o,LinkedList<Element> u,String w)
    {
      kopf=k.clone();
      ober=ober.clone();
      unter=(LinkedList<Element>)unter.clone();
      wert=new String(w);
    }

    public final Element clone()
    {
      return new Element(kopf,ober,unter,wert);
    }

    protected void addUnter(Element element)
//    public void addUnter(Element element)
    {
      unter.add(element);
    }

    public Element(String xml,Element ob) throws Exception
    {
      String merkXml=xml;
      unter=new LinkedList<Element>();
      ober=ob;
      ober.addUnter(this);
      String zwi=getTag(xml,pingelig);
      Matcher matS=sTagPat.matcher(zwi);
      if(matS.find())
      {
        String group=matS.group();
        kopf=new Elementkopf(group);
        zwi=zwi.substring(matS.end(),zwi.length());
        if(!(group.indexOf("/>")>0))
        {
          zwi=zwi.replaceFirst(eTagStr,"");
        }
      }  
      xml=zwi;
      Matcher mat=sTagAufPat.matcher(xml);
      boolean nichts=true;
      while(mat.find())
      {
        nichts=false;
        String elementName=getTagName(mat.group());
        String[] aus=getTag(elementName,xml,pingelig);
        for(int a=0;a<2;a++)
        {
//          if(!aus[a].equals(""))
          if(!aus[a].equals("")&&!aus[a].equals(merkXml)) // letztes, um Endlosrekursion auszuschließen. Leider geht so dann gar nichts mehr, warum, ist unklar.
          {
            try
            {
//              System.out.println("XMLParser:Element:init:"+aus[a]+"/"+merkXml);
              new Element(aus[a],this);
             }
             catch(Exception ex)
             {
//               was mit den so ignorierten Teilen passiert, muss noch gekl�rt werden. Der Unterschied zwischen springer.html und springer2.html kommt �brigens vom nicht eingeschalteten Pattern.DOTALL: in springer.html verhindern die newlines, dass die st�renden Teile �berhaupt gefunden werden.
            }
          }
        }
        xml=aus[2];
        mat=sTagAufPat.matcher(xml);
      }
      if(nichts)
      {
        wert=resolveEntities(entities,zwi);
//        System.out.println("wert:"+wert);
        textStuecke.add(this);
       }
       else
       {
         if(!xml.equals(""))
         {
           new Element(xml,this);
         }
      } 
    }

    public String getName()
    {
      if(kopf!=null)
      {
        return kopf.getName();
       }
       else
       {
         return "";
      } 
    }
    
    public Element getNearestNamed()
    {
      String aus=getName();
      if((aus==null)||(aus.equals("")))
      {
        Element zwi=getOber();
        if(zwi!=null)
        {
          return getOber().getNearestNamed();
         }
         else
         {
           return this; 
        }
      }
      return this;
    }

    public Hashtable<String,String> getAttribute()
    {
      if(kopf!=null)
      {
        return kopf.getAttribute();
       }
       else
       {
         return new Hashtable<String,String>();
      } 
    }

    public String getWert()
    {
      return wert;
    }

    public Element getUnterWert()
    {
      if(!wert.equals(""))
      {
        return this;
       }
       else
       {
         ListIterator<Element> li=unter.listIterator();
         boolean weiter=true;
         Element aus=null;
         while((weiter)&&(li.hasNext()))
         {
           Element akt=li.next();
           aus=akt.getUnterWert();
           weiter=(aus!=null);
         }
         return aus;
      } 
    }

    public void getUnterste(LinkedList<Element> liste)
    {
      if(!wert.equals(""))
      {
        liste.add(this);
       }
       else
       {
         ListIterator<Element> li=unter.listIterator();
         while(li.hasNext())
         {
           li.next().getUnterste(liste);
         }
      } 
    }

    public boolean isOberOf(Element el)
    {
      if(this==el)
      {
        return true;
      }
      if(el==null)
      {
        return false;
      }
      return isOberOf(el.getOber());
    }

    public Element getLeastCommonOber(Element el)
    {
//      System.out.println("XMLParser:getLeastCommonOber:"+getName()+"("+getAttribute()+") ["+el.getName()+"]");
      if(isOberOf(el))
      {
        return this;
      }
      return ober.getLeastCommonOber(el);
    }

    public Element getLeastCommonOber(LinkedList<Element> liste)
    {
      Element obe=null;
      if(liste!=null)
      {
        if(liste.size()>0)
        {
          liste=(LinkedList<Element>)liste.clone();
          obe=liste.removeFirst();
          ListIterator<Element> li=liste.listIterator();
          while(li.hasNext())
          {
            Element zwi=li.next();
//            System.out.println("XMLParser:getLeastCommonOber:liste:"+zwi.getName());
            obe=obe.getLeastCommonOber(zwi);
          }
        }  
      }
      return obe;
    }

    public Element getOber()
    {
      return ober;
    }

    public LinkedList<Element> getUnter()
    {
      return (LinkedList<Element>)unter.clone();
    }
    
    public String getTextContent()
    {
    	LinkedList<Element> untere=new LinkedList<Element>();
    	getUnterste(untere);
    	ListIterator<Element> li=untere.listIterator();
    	StringBuffer aus=new StringBuffer();
    	while(li.hasNext())
    	{
    		Element element=li.next();
    		aus.append(element.getWert());
    	}
    	return aus.toString();
    }

    public boolean check(String name,String attribut,String value,String content,boolean lowerCase)
    {
    	boolean aus=true;
    	if(name!=null)
    	{
//    		System.out.println("XMLParser:check:name:"+normalize(getName(),lowerCase));
    		aus=normalize(getName(),lowerCase).equals(normalize(name,lowerCase));
    	}
    	if(aus&&(attribut!=null))
    	{
    		String val=normalize(getAttribute(),lowerCase).get(normalize(attribut,lowerCase));
//    		System.out.println("XMLParser:check:attribut:"+normalize(attribut,lowerCase)+"/"+val);
    		aus=(val!=null);
    		if(aus&&(value!=null))
    		{
    			aus=normalize(val,lowerCase).equals(normalize(value,lowerCase));
    		}
    	 }
    	 else
    	 {
    		 if(value!=null)
    		 {
    			 aus=normalize(getAttribute(),lowerCase).values().contains(normalize(value,lowerCase));
    		 }
    	}
    	if(aus&&(content!=null))
    	{
    		String textContent=getTextContent();
//    		System.out.println("XMLParser:check:content"+textContent);
    		if(textContent!=null)
    		{
    			aus=normalize(textContent,lowerCase).equals(normalize(content,lowerCase));
    		 }
    		 else
    		 {
    			 aus=false;
    		}
    	}
    	return aus;
    }
    
    protected Hashtable<String,String> normalize(Hashtable<String,String> hash,boolean lowerCase)
    {
    	Hashtable<String,String> aus=hash;
    	if(lowerCase)
    	{
    		aus=new Hashtable<String,String>();
    		Enumeration<String> keys=hash.keys();
    		while(keys.hasMoreElements())
    		{
    			String key=keys.nextElement();
    			aus.put(key.toLowerCase(),hash.get(key).toLowerCase());
    		}
    	}
    	return aus;
    }
    
    protected String normalize(String ein,boolean lowerCase)
    {
    	if(lowerCase)
    	{
    		ein=ein.toLowerCase();
    	}
    	return ein;
    }
    
    public Element hatName(String name,String attribut)
    {
      if((name==null)&&(attribut==null))
      {
        return null;
      }
      String nam=getName();
      /*
      System.out.println("test:"+nam+".");
      Hashtable hash=getAttribute();
      Enumeration en=hash.keys();
      while(en.hasMoreElements())
      {
        String el=(String)en.nextElement();
        System.out.println(el+":"+hash.get(el)+".");
      }
      System.out.println("---");
      */
      if((name==null)||name.equals(nam))
      {
        if((attribut==null)||(getAttribute().containsKey(attribut)))
        {
          return this;
        }
      }  
      Element ob=getOber();
      if(ob!=null)
      {
        return ob.hatName(name,attribut);
       }
       else
       {
         return null;
      } 
    }

    public String toXMLString()
    {
      String name=getName();
      String wert=getWert();
      Hashtable<String,String> attribute=getAttribute();
      StringBuffer tagAuf=new StringBuffer();
      StringBuffer tagZu=new StringBuffer();
      if(!name.equals(""))
      {
        tagAuf.append("<"+name);
        tagZu.append("<"+name+">");
        Enumeration<String> en=attribute.keys();
        while(en.hasMoreElements())
        {
          String attr=(String)en.nextElement();
          String value="'"+(String)attribute.get(attr)+"'";
          tagAuf.append(" "+attr+"="+value);
        }
        tagAuf.append(">");
        ListIterator<Element> li=getUnter().listIterator();
        StringBuffer aus=new StringBuffer(tagAuf);
        while(li.hasNext())
        {
          Element zwi=li.next();
          aus.append(zwi.toXMLString());
        }
        aus.append(wert);
        aus.append(tagZu);
        return aus.toString();
       }
       else
       {
         return getWert();
      } 
    }
  }

  public final Element getStueck(int pos)
  {
    Element aus=null;
    if(pos<0)
    {
      return aus;
    }
    ListIterator<Element> li=textStuecke.listIterator();
    int sum=-1;
    while((sum<pos)&&li.hasNext())
    {
      aus=li.next();
      sum=sum+aus.getWert().length();
    }
    if(sum<pos)
    {
      aus=null;
    }
    return aus;
  }

  public final Element stueckHatName(int pos,String name,String att)
  {
    return getStueck(pos).hatName(name,att);
  }

  public static final String countQuads(String xml)
  {
    Matcher mat=content.matcher(xml);
    StringBuffer aus=new StringBuffer();
    int quad=1;
    while(mat.find())
    {
      String group=mat.group();
      aus.append(xml.substring(0,mat.start()));
      aus.append("><quad n='_"+quad+"'"+group+"/quad><");
      xml=xml.substring(mat.end(),xml.length());
      mat=content.matcher(xml);
      quad=quad+1;
    }
    aus.append(xml);
    return aus.toString();
  }
  
  public static String gansWeg(String ein)
  {
    return ein.replaceAll(gansWeg,"");
  }
}
