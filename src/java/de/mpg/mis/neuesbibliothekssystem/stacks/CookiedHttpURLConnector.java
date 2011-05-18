package de.mpg.mis.neuesbibliothekssystem.stacks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class CookiedHttpURLConnector
{
  protected URL url;
  protected HttpURLConnection connection;
  protected String cookie;
  public int countOut=10;
  protected int count;
  public Long timeOut=10000l;
  protected Long time;
  protected Pattern aPat=Pattern.compile("<\\s*a\\s+[^>]+>");
  protected Pattern hrefPat=Pattern.compile("href=['\"]?[^'\"\\s>]+");
  public boolean dontSendCookies=false;
  protected String requestMethod;
  protected Map<String,List<String>> requestProperties;
  protected Proxy proxy=null;
  
  public CookiedHttpURLConnector(URL url,String cookie) throws Exception
  {
    dontSendCookies=false;
    requestProperties=new HashMap<String,List<String>>();
    init0(url,cookie);
  }
  
  public CookiedHttpURLConnector(URL url,String cookie,boolean dontSendCookies) throws Exception
  {
    this.dontSendCookies=dontSendCookies;
    requestProperties=new HashMap<String,List<String>>();
    init0(url,cookie);
  }
  
  public CookiedHttpURLConnector(URL url,String cookie,boolean dontSendCookies,String requestMethod,Map<String,List<String>> requestProperties) throws Exception
  {
    this.dontSendCookies=dontSendCookies;
    if(!isNullString(requestMethod))
    {
      this.requestMethod=requestMethod;
    }
    this.requestProperties=requestProperties;
    init0(url,cookie);
  }
  
  public void setProxy(Proxy proxy)
  {
  	this.proxy=proxy;
  }
  
  public Proxy getProxy()
  {
  	return proxy;
  }
  
  private void init0(URL url,String cookie) throws Exception
  {
    this.url=url;
    this.cookie=cookie;
    count=0;
    time=System.currentTimeMillis();
    negotiate();
  }
  
  protected void prepareConnection(HttpURLConnection connection) throws Exception
  {
    connection.setInstanceFollowRedirects(false);
//    HttpURLConnection.setFollowRedirects(false);
    if(!isNullString(requestMethod))
    {
      connection.setRequestMethod(requestMethod);
    }
    Set<String> requestKeys=requestProperties.keySet();
    Iterator<String> iterator=requestKeys.iterator();
    while(iterator.hasNext())
    {
      String key=iterator.next();
      StringBuffer rebu=new StringBuffer();
      Iterator<String> it=requestProperties.get(key).iterator();
      while(it.hasNext())
      {
        if(rebu.length()>0)
        {
          rebu.append("; "); 
        }           
        rebu.append(it.next());
      }
      connection.setRequestProperty(key,rebu.toString());
    }
  }
  
  protected void negotiate() throws Exception
  {
  	if(proxy==null)
  	{
      connection=(HttpURLConnection)url.openConnection();
  	 }
  	 else
  	 {
  		 connection=(HttpURLConnection)url.openConnection(proxy);
  	}
    prepareConnection(connection);
    if(!isNullString(cookie)&&!dontSendCookies)
    {
      connection.addRequestProperty("Cookie",cookie);
    }
    connection.connect();
//    System.out.println(connection.getRequestProperties().toString());
    int responseCode=connection.getResponseCode();
    String cookieZwi=readCookies(connection.getHeaderFields());
    if(!isNullString(cookieZwi))
    {
      cookie=cookie+";"+cookieZwi;
    }
    System.out.println("Cookied...:negotiate:"+responseCode+"/"+cookie);
    if((responseCode>299)&&(responseCode<400))
    {
//      String content=(String)connection.getContent();
      String content=getContentString();
      System.out.println("Cookied...:negotiate:"+content);
//      System.out.println("Cookied...:negotiate:"+connection.getHeaderField("Location").toString());
      String redirect=getRedirection(content);
      redirect=connection.getHeaderField("Location");
      if(redirect.indexOf("http:")!=0)
      {
      	redirect="http://"+url.getHost()+redirect;
      }
      URL urlZwi=new URL(redirect);
      /*
      URL urlZwi=connection.getURL();
      if(!redirect.equals(""))
      {
      	urlZwi=new URL(redirect);
      }
      */
      if(!url.equals(urlZwi))
      {
        Long diff=System.currentTimeMillis()-time;
        if((count++<countOut)||(timeOut<diff))
        {
          url=urlZwi;
          negotiate();
         }
         else
         {
           throw new Exception("count("+count+"/"+countOut+")- or timeOut("+diff+"/"+timeOut+")");
        }
      }
    }
  }

  protected String getRedirection(String content)
  {
    String aus="";
    Matcher mat=aPat.matcher(content);
    if(mat.find())
    {
      mat=hrefPat.matcher(mat.group());
      if(mat.find())
      {
        aus=mat.group().replaceFirst("href=['\"]?",""); 
      }
    }
    return aus;
  }
  
  protected String readCookies(Map<String,List<String>> map)
  {
    String aus="";
    Set<String> keys=map.keySet();
    Iterator<String> it=keys.iterator();
    while(it.hasNext())
    {
      String key=it.next();
      if((key!=null)&&key.toLowerCase().equals("set-cookie"))
      {
        Iterator<String> lit=map.get(key).iterator();
        while(lit.hasNext())
        {
          String was=lit.next();
          if(aus.equals(""))
          {
            aus=was; 
           }
           else
           {
             aus=aus+"; "+was;
          }
        }
      }
    }
    return aus;
  }
  
  public static boolean isNullString(String string)
  {
    return (string==null)||(string.equals(""));
  }
  
  public URL getUrl()
  {
    return url;
  }
  
  public HttpURLConnection getConnection()
  {
    return connection;
  }
  
  public String getCookie()
  {
    return cookie;
  }
  
  public String getContentString() throws Exception
  {
    String encoding=connection.getContentEncoding();
//    System.out.println("Cookied..:getContentString:encoding:"+encoding);
    InputStream stream=null;
    if((encoding!=null)&&encoding.equals("gzip"))
    {
      stream=new GZIPInputStream(connection.getInputStream());
     }
     else
     {
       stream=connection.getInputStream();
    }   
    StringBuffer aus=new StringBuffer();
    byte[] buffer=new byte[4096];
    int intsRead;
    while((intsRead=stream.read(buffer))!=-1)
    {
      for(int a=0;a<intsRead;a++)
      {
        aus.append((char)buffer[a]);
      }
    }
    return aus.toString();
  }
//    return (String)connection.getContent();
  
  public File getContentFile(String pfad) throws Exception
  {
    File file=getFile(pfad);
    FileOutputStream fw=new FileOutputStream(file);
    InputStream stream=connection.getInputStream();
    byte[] buffer=new byte[4096];
    int bytesRead;
    while((bytesRead=stream.read(buffer))!=-1)
    {
      fw.write(buffer,0,bytesRead);
    }
    fw.flush();
    fw.close();
    stream.close();
    return file;
  }
  
  public static File getFile(String pfad) throws Exception
  {
    String name=(new Long(System.currentTimeMillis())).toString();
    File aus=new File(pfad+name+".dat");
    while(aus.exists())
    {
      name=name+"a";
      aus=new File(pfad+name+".dat");
    }
    return aus;
  }
}
