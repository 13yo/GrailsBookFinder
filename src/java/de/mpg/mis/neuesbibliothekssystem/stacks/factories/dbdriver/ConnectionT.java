package de.mpg.mis.neuesbibliothekssystem.stacks.factories.dbdriver;

import java.sql.*;
import java.util.regex.*;
import java.util.*;
import java.io.FileWriter;

public class ConnectionT {
    public static final Pattern gansPat = Pattern.compile(
	    "((\\A|[^\\\\\"])'.+?[^\\\\]')|((\\A|[^\\\\'])\".+?[^\\\\]\")",
	    Pattern.DOTALL);
    protected Connection con;
    public boolean print = true;
    public boolean file = false;
    public boolean exec = false;
    public String fileName;

    public static String getGans(String string, String vor, String nach,
	    LinkedList<String> tokens, LinkedList<String> ganses) {
	int zGans = 1;
	StringBuffer buffer = new StringBuffer();
	String lauf = string;
	Matcher matGans = gansPat.matcher(lauf);
	while (matGans.find()) {
	    buffer.append(lauf.substring(0, matGans.start()));
	    String group = matGans.group();
	    int len = group.length();
	    String first = group.substring(0, 1);
	    if (!(first.equals(group.substring(len - 1, len)))) {
		buffer.append(first);
		group = group.substring(1, len);
	    }
	    String halter = vor + (new Integer(zGans++)).toString() + nach;
	    buffer.append(halter);
	    tokens.addFirst(halter);
	    ganses.addFirst(group);
	    lauf = lauf.substring(matGans.end(), lauf.length());
	    matGans = gansPat.matcher(lauf);
	}
	buffer.append(lauf);
	return buffer.toString();
    }

    public static String getReGans(String string, LinkedList<String> tokens,
	    LinkedList<String> ganses) {
	ListIterator<String> ti = tokens.listIterator();
	ListIterator<String> gi = ganses.listIterator();
	while (ti.hasNext()) {
	    String token = ti.next();
	    String gans = gi.next();
	    string = string.replaceFirst(token, gans);
	}
	return string;
    }

    public class PreparedStatementT {
	protected PreparedStatement ps;
	protected StatementString statement;
	protected int zQues = 0;
	protected String[] vars;

	protected class StatementString {
	    protected String statement;
	    protected String markedStatement;
	    protected Pattern quesPat = Pattern.compile("\\?");
	    protected LinkedList<String> gansTokens = new LinkedList<String>();
	    protected LinkedList<String> gansGanses = new LinkedList<String>();

	    public StatementString(String stat) {
		statement = stat;
		String zwi = getGans(statement, "!-", "!", gansTokens,
			gansGanses);
		StringBuffer buffer = new StringBuffer();
		Matcher quesMat = quesPat.matcher(zwi);
		while (quesMat.find()) {
		    buffer.append(zwi.substring(0, quesMat.start()));
		    buffer.append("!" + (new Integer(zQues++)).toString() + "!");
		    zwi = zwi.substring(quesMat.end(), zwi.length());
		    quesMat = quesPat.matcher(zwi);
		}
		buffer.append(zwi);
		markedStatement = buffer.toString();
		System.out.print("StatementString:" + markedStatement + " : ");
		ListIterator<String> li = gansTokens.listIterator();
		ListIterator<String> lii = gansGanses.listIterator();
		while (li.hasNext()) {
		    System.out.print(li.next() + "->" + lii.next() + "\t");
		}
		System.out.println();
	    }

	    public String getOriginalStatement() {
		return statement;
	    }

	    public String getStatement() {
		String aus = markedStatement;
		for (int a = 0; a < zQues; a++) {
		    aus = aus.replaceFirst("!" + a + "!", vars[a]);
		}
		return getReGans(aus, gansTokens, gansGanses);
	    }
	}

	protected PreparedStatementT(String stat) throws Exception {
	    ps = con.prepareStatement(stat);
	    statement = new StatementString(stat);
	    vars = new String[zQues];
	    for (int a = 0; a < zQues; a++) {
		vars[a] = "?";
	    }
	}

	protected void gibAus() throws Exception {
	    String aus = statement.getStatement();
	    if (print) {
		System.out.println(aus);
	    }
	    if (file) {
		FileWriter fw = new FileWriter(fileName);
		fw.write(aus);
		fw.close();
	    }
	}

	public boolean execute() throws Exception {
	    gibAus();
	    if (exec) {
		return ps.execute();
	    }
	    return false;
	}

	public ResultSet executeQuery() throws Exception {
	    gibAus();
	    return ps.executeQuery();
	}

	public int executeUpdate() throws Exception {
	    gibAus();
	    if (exec) {
		return ps.executeUpdate();
	    }
	    return 0;
	}

	public void setString(int i, String string) throws SQLException {
	    ps.setString(i, string);
	    vars[i - 1] = string;
	}

	public void setInt(int i, int v) throws SQLException {
	    ps.setInt(i, v);
	    vars[i - 1] = (new Integer(v)).toString();
	}

	public void setLong(int i, long l) throws SQLException {
	    ps.setLong(i, l);
	    vars[i - 1] = (new Long(l)).toString();
	}

	public void setNull(int i, int n) throws SQLException {
	    ps.setNull(i, n);
	    vars[i - 1] = "null";
	}

	public void setBytes(int i, byte[] array) throws SQLException {
	    ps.setBytes(i, array);
	    vars[i - 1] = "TIMESTAMP-Array";
	}
    }

    public ConnectionT(Connection con) {
	this.con = con;
    }

    public void commit() throws SQLException {
	con.commit();
    }

    public void close() throws SQLException {
	con.close();
    }

    public PreparedStatementT prepareStatement(String sql) throws Exception {
	return new PreparedStatementT(sql);
    }
}
