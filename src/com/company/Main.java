package com.company;

import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Main {
    public static DB db = new DB();
    private static String site;
    private static String site1;
    private static String site2;
    private static int count = 0;
    private static int limit;
    private static int countParent = 0;
    private static int countParent2 = 0;
    private static String findParentId;

    private static int startchildID = 0;

    public static void main(String[] args) throws SQLException, IOException, Throwable {

        //getChildID();


        //Scanner s = new Scanner(System.in);
        //site1 = s.nextLine();
        site1 = "techspot.com";
        limit = 10;
        site2 = "http://" + site1 + "/";
        processPage(site2);
        recurseveCall();

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

    public static void recurseveCall() throws SQLException, IOException {
        String sql = "SELECT childsite FROM child ;";
        ResultSet rsrs = db.runSql(sql);
        if (count < limit) {
            while (rsrs.next() != false) {
                site2 = rsrs.getString("childsite");
                System.out.println(site2 + " processPage");
                countParent2++;
                processPage(site2);
            }
            recurseveCall();
        }
    }

    public static void processPage(String URLs) throws SQLException, IOException, HttpStatusException {
        String URL;
        if (URLs.contains("www")) {
            URL = "" + URLs.substring(0, 7) + URLs.substring(11);
        } else {
            URL = URLs;
        }

        if (countParent == countParent2) {    //insert new parent
            String sql = "SELECT parentsite from parent where parentsite='" + URL + "'";
            ResultSet rsrs = db.runSql(sql);
            if (rsrs.next() == false) {
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
                countParent++;
            }
            countParent++;
        }

        //check if the given URL && parent_id is already in database
        String sql = "select * from child where childsite = '" + URL + "'" + " and parent_id=" + findParentId;
        ResultSet rs = db.runSql(sql);
        if (rs.next()) {

        } else {
            System.out.println("" + URL);

            //store the URL to database to avoid parsing again
            count++;
            sql = "insert into child (childsite,parent_id)\n" +
                    "values(?,?)";
            PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, URL);
            stmt.setString(2, findParentId);
            stmt.execute();

            //get useful information
            String a = site2 + "/";
                Document doc = Jsoup.connect(a).timeout(10 * 1000).get();//fix read time out exception
                if (doc.text().contains("research")) {
                    System.out.println(URL);
                }

                //get all links and recursively call the processPage method
                Elements questions = doc.select("a[href]");
                for (Element link : questions) {
                  //  if (link.attr("href").contains(site1)) {
                        if (count < limit) {
                            processPage(link.attr("abs:href"));
                      //  } else {
                      //  }
                    }
                }


        }
    }
}




