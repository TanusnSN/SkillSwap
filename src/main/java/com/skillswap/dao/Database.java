package com.skillswap.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens connections to the SQLite file (skillswap.db) with JDBC,
 * creates the tables and adds sample data the first time.
 */
public class Database {

    private static final String URL = "jdbc:sqlite:skillswap.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Create tables if they do not exist, then add sample data if empty. */
    public static void init() {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY, name TEXT, email TEXT, credits INTEGER, "
                    + "skills_offered TEXT, skills_wanted TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS skills ("
                    + "id INTEGER PRIMARY KEY, name TEXT, category TEXT, description TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS sessions ("
                    + "id INTEGER PRIMARY KEY, skill_id INTEGER, teacher_id INTEGER, "
                    + "type TEXT, duration INTEGER, max_participants INTEGER, rating REAL)");

            st.execute("CREATE TABLE IF NOT EXISTS bookings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, session_id INTEGER, "
                    + "student_id INTEGER, cost INTEGER, booked_at TEXT DEFAULT CURRENT_TIMESTAMP)");

            int userCount;
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                rs.next();
                userCount = rs.getInt(1);
            }
            if (userCount == 0) {
                seed(st);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database setup failed", e);
        }
    }

    /** Sample data so the website has something to show. */
    private static void seed(Statement st) throws SQLException {
        // id, name, email, credits, skills offered, skills wanted
        st.execute("INSERT INTO users VALUES (1,'Yasha','yasha@campus.edu',5,'','Guitar')");
        st.execute("INSERT INTO users VALUES (2,'Asha Nair','asha@campus.edu',5,'Java','')");
        st.execute("INSERT INTO users VALUES (3,'Ravi Kumar','ravi@campus.edu',5,'Calculus','')");
        st.execute("INSERT INTO users VALUES (4,'Meera Iyer','meera@campus.edu',5,'Figma','')");
        st.execute("INSERT INTO users VALUES (5,'Karthik S','karthik@campus.edu',5,'French','')");
        st.execute("INSERT INTO users VALUES (6,'Sana Ali','sana@campus.edu',5,'Guitar','')");
        st.execute("INSERT INTO users VALUES (7,'Dev Patel','dev@campus.edu',5,'SQL','')");
        st.execute("INSERT INTO users VALUES (8,'Nisha Rao','nisha@campus.edu',5,'Algebra','')");
        st.execute("INSERT INTO users VALUES (9,'Arjun M','arjun@campus.edu',5,'Design','')");

        // id, name, category, description
        st.execute("INSERT INTO skills VALUES (1,'Java OOP Basics','Programming','Classes, objects, inheritance and interfaces explained with simple examples.')");
        st.execute("INSERT INTO skills VALUES (2,'Calculus Crash Course','Maths','Limits, derivatives and integrals before your exam. Bring your doubts.')");
        st.execute("INSERT INTO skills VALUES (3,'UI Design with Figma','Design','Design clean app screens and learn the basics of colour and layout.')");
        st.execute("INSERT INTO skills VALUES (4,'Spoken French','Languages','Everyday conversation practice for complete beginners.')");
        st.execute("INSERT INTO skills VALUES (5,'Guitar for Beginners','Music','Learn your first chords and play a full song in one session.')");
        st.execute("INSERT INTO skills VALUES (6,'SQL & Databases','Programming','Tables, joins and queries. Perfect before your DBMS lab.')");
        st.execute("INSERT INTO skills VALUES (7,'Linear Algebra Help','Maths','Matrices, vectors and eigenvalues made easy.')");
        st.execute("INSERT INTO skills VALUES (8,'Poster & Logo Design','Design','Make eye-catching posters for your college fest.')");

        // id, skill_id, teacher_id, type, duration, max_participants, rating
        st.execute("INSERT INTO sessions VALUES (1,1,2,'ONE_ON_ONE',60,1,4.9)");
        st.execute("INSERT INTO sessions VALUES (2,2,3,'GROUP_WORKSHOP',90,10,4.8)");
        st.execute("INSERT INTO sessions VALUES (3,3,4,'ONE_ON_ONE',60,1,4.7)");
        st.execute("INSERT INTO sessions VALUES (4,4,5,'GROUP_WORKSHOP',60,10,4.6)");
        st.execute("INSERT INTO sessions VALUES (5,5,6,'ONE_ON_ONE',45,1,5.0)");
        st.execute("INSERT INTO sessions VALUES (6,6,7,'GROUP_WORKSHOP',90,12,4.8)");
        st.execute("INSERT INTO sessions VALUES (7,7,8,'ONE_ON_ONE',60,1,4.5)");
        st.execute("INSERT INTO sessions VALUES (8,8,9,'GROUP_WORKSHOP',60,15,4.7)");
    }
}