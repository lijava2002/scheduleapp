//  RankDAO.java

/**
 * Changelog:
 * 2016-02-24 : Added getList() method
 * 2016-02-24 : Added getMap() method
 * 
 * 2016-02-27 : Added getMapReversed() method
 * 
 * 2016-03-24 : Grouped and ordered methods into logical groupings
 * 2016-03-24 : Formatted to match Google Java Style
 */

/**
 *
 * @author Alan Johnson
 */
package domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import util.DBConnectionPool;

public class RankDAO {

  private DBConnectionPool connPool;
  private static final Logger logger = Logger.getLogger(RankDAO.class.getName());

  private static final String GET_STATEMENT = "SELECT * "
      + "FROM rank";

  public ObservableList<String> getList() {

    PreparedStatement request = null;
    Connection conn = null;

    try {
      conn = DBConnectionPool.getPoolConnection();
      request = conn.prepareStatement(GET_STATEMENT);

      ResultSet rset = request.executeQuery();
      ArrayList<String> rankList = new ArrayList<>();

      while (rset.next()) {
        rankList.add(rset.getString("name"));
      }

      return FXCollections.observableArrayList(rankList);

    } catch (SQLException se) {
      logger.log(Level.WARNING, "A database error occurred. " + se.getMessage());
    } catch (Exception e) {
      logger.log(Level.WARNING, "Exception: " + e.getMessage());
    } finally {

      if (request != null) {
        try {
          request.close();
        } catch (SQLException se) {
          se.printStackTrace(System.err);
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
      }
    }

    return null;
  }

  public Map<Integer, String> getMap() {

    PreparedStatement request = null;
    Connection conn = null;

    try {
      conn = DBConnectionPool.getPoolConnection();
      request = conn.prepareStatement(GET_STATEMENT);

      ResultSet rset = request.executeQuery();
      HashMap<Integer, String> rankList = new HashMap<>();

      int number = 1;
      while (rset.next()) {
        rankList.put(number++, rset.getString("name"));
      }

      return rankList;

    } catch (SQLException se) {
      logger.log(Level.WARNING, "A database error occurred. " + se.getMessage());
    } catch (Exception e) {
      logger.log(Level.WARNING, "Exception: " + e.getMessage());
    } finally {

      if (request != null) {
        try {
          request.close();
        } catch (SQLException se) {
          se.printStackTrace(System.err);
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
      }
    }

    return null;
  }

  public Map<String, Integer> getMapReversed() {
    Map<Integer, String> orderedMap = getMap();
    Map<String, Integer> reversedMap = new HashMap<>();

    for (Integer key : orderedMap.keySet()) {
      reversedMap.put((String) orderedMap.get(key), key);
    }

    return reversedMap;
  }

  public ComboBoxModel getComboModel() {

    PreparedStatement request = null;
    Connection conn = null;

    ArrayList<Person> personList = new ArrayList<>();
    try {
      conn = DBConnectionPool.getPoolConnection();
      request = conn.prepareStatement(GET_STATEMENT);

      ResultSet rset = request.executeQuery();
      ArrayList<String> rankList = new ArrayList<>();

      while (rset.next()) {
        rankList.add(rset.getString("name"));
      }

      String[] rankArray = (String[]) rankList.toArray(new String[0]);

      return new DefaultComboBoxModel(rankArray);

    } catch (SQLException se) {
      logger.log(Level.WARNING, "A database error occurred. " + se.getMessage());
    } catch (Exception e) {
      logger.log(Level.WARNING, "Exception: " + e.getMessage());
    } finally {

      if (request != null) {
        try {
          request.close();
        } catch (SQLException se) {
          se.printStackTrace(System.err);
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
      }
    }

    return null;
  }

}
