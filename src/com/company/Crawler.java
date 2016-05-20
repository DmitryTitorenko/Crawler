package com.company;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by GrinWey on 06.05.2016.
 */
public class Crawler {
    public static DB db = new DB();
    private static String site1;
    private static String site2;
    private static int limit;
    private static int startchildID = 0;
    private static int ii = 0;
    private static int a = 0;
    private static int q;
    private static int countChild = 0;
    private static String findParentId;
    private static int count = 0;
    private static int e = 0;


    public static void main(String[] args) throws SQLException, IOException {
        getChildID();
        System.out.println("startchildID: " + startchildID);
        limit = 10000;
        site1 = "techspot.com";
        site2 = "http://www." + site1 + "/";
        processPage(site2);
    }

    public static void getChildID() throws SQLException, IOException {
        //insert child
        String intsetParent = "insert into child (childsite)\n" +
                "values(?)";
        PreparedStatement stmt = db.conn.prepareStatement(intsetParent, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, "mygoogle");//add values('URL') in insert instead values(?)
        stmt.execute();
        //get child id
        String sql = "SELECT child_id FROM child where childsite='mygoogle'";
        ResultSet rsrs = db.runSql(sql);
        rsrs.next();
        startchildID = rsrs.getInt("child_id");
        sql = "delete from child";
        db.runSql2(sql);
    }

    public static void processPage(String URL) throws SQLException, IOException {
        //insert new parent
        String intsetParent = "insert into parent (parentsite)\n" +
                "values(?)";
        PreparedStatement stmt = db.conn.prepareStatement(intsetParent, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, URL);//add values('URL') in insert instead values(?)
        stmt.execute();

        //using parentsite find parent_id
        String sqll = "select parent_id from parent where parentsite='" + URL + "'";
        ResultSet rsss = db.runSql(sqll);
        rsss.next();
        findParentId = rsss.getString("parent_id");
        System.out.println("parent: " + URL);

        //get useful information
        String site3 = site2 + "/";
        //Document doc = Jsoup.connect(site3).timeout(10 * 1000).get();//fix read time out exception
        Document doc = Jsoup.connect(site3).timeout(10 * 1000).ignoreHttpErrors(true).get();//fix read time out exception

        //get all links and recursively call the processPage method
        Elements countRef = doc.select("a[href]");
        for (Element link : countRef) {
            //check limit
            if (count < limit) {
                //check if the given URL  is already in database
                String sql = "select * from child where childsite = '" + link.attr("abs:href") + "'";
                ResultSet rs = db.runSql(sql);
                if (rs.next()) {
                } else {
                    //don't insert parent site
                    String dsql = "select * from parent where parentsite = '" + link.attr("abs:href") + "'";
                    ResultSet ssrs = db.runSql(dsql);
                    if (ssrs.next()) {
                    } else {
                        System.out.println(link.attr("abs:href"));
                        sql = "insert into child (childsite,parent_id)\n" +
                                "values(?,?)";
                        stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        stmt.setString(1, link.attr("abs:href"));
                        stmt.setString(2, findParentId);
                        stmt.execute();
                        ii++;
                        a++;
                        count++;
                    }
                }
            }
        }
        if (count < limit) {
            if (countChild ==0) {
                q = ii - a;
                recurseveCall(0);
            } else {
                e++;
                //recurseveCall(1);
            }
        }
        else {
            System.out.println("Finish");
        }
    }

    public static void recurseveCall(int first) throws SQLException, IOException {
        countChild++;
        int rr = startchildID + q;
        String sql = "SELECT childsite FROM child where child_id>" + rr + ";";
        ResultSet rsrs = db.runSql(sql);
        while (rsrs.next() != false) {
            if (count < limit) {
                site2 = rsrs.getString("childsite");
                System.out.println(site2 + " processPage");
                processPage(site2);
            }
        }
        recurseveCall(0);
    }
}




