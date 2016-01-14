package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import common.MessageObject;

public class CustServer extends Thread {

    protected final int port = 9720; // Where 9714 is your port number

    protected ServerSocket listen_socket;

    // Constructor ---------------------------------------------- -

    // Create a ServerSocket to listen for connections.

    // Start the thread.

    public CustServer() {

        try {

            listen_socket = new ServerSocket(port);

        } catch (IOException e) {

            fail(e, "Exception creating server socket");

        }

        System.out.println("Server listening on port " + port);

        this.start();

    }

    // fail ------------------------------------------------------

    // Exit with an error message when an exception occurs

    public static void fail(Exception e, String msg) {

        System.err.println(msg + ": " + e);

        System.exit(1);

    }

    // run -------------------------------------------------------

    // The body of the server thread. Loop forever, listening for and

    // accepting connections from clients. For each connection, create a

    // new Conversation object to handle the communication through the

    // new Socket.

    public void run() {
        try {

            while (true) {

                Socket client_socket = listen_socket.accept();

                // create a Conversation object to handle this client and pass

                // it the Socket to use. If needed, we could save the
                // Conversation

                // object reference in a Vector. In this way we could later
                // iterate

                // through this vector looking for "dead" connections and
                // reclaim

                // any resources.

                Conversation conv = new Conversation(client_socket);
            }

        } catch (IOException e) {

            fail(e, "Exception listening for connections");

        }

    }

    // main-------------------------------------------------------

    // Start up the Server program.

    public static void main(String args[]) {

        new CustServer();

    }

} // end Server

// **************************************************************

// This class is the Thread that handles all communication with

// the client

class Conversation extends Thread

{

    protected Socket client;

    protected ObjectInputStream in;

    protected ObjectOutputStream out;

    // Where JavaCust44 is your database name

    private static final String URL = "jdbc:mysql://courses:3306/JavaCust44";

    protected Connection con;

    private Statement getAllStmt = null;

    private PreparedStatement addStmt = null;

    private PreparedStatement deleteStmt = null;

    private PreparedStatement updateStmt = null;

    // Constructor -----------------------------------------------

    // Initialize the streams and start the thread

    public Conversation(Socket client_socket) {

        client = client_socket;

        try {

            out = new ObjectOutputStream(client.getOutputStream());

            in = new ObjectInputStream(client.getInputStream());

        } catch (IOException e) {

            try {

                client.close();

            } catch (IOException e2) {
            }

            System.err.println("Exception getting socket streams " + e);

            return;

        }

        try {

            Class.forName("com.mysql.jdbc.Driver").newInstance();

        } catch (ClassNotFoundException e) {

            System.err.println("Exception loading DriverManager class " + e
                    + " " + e.getMessage());

            return;

        } catch (InstantiationException e) {

            System.err.println("Exception loading DriverManager class " + e);

            return;

        } catch (IllegalAccessException e) {

            System.err.println("Exception loading DriverManager class " + e);

            return;

        }

        try {

            con = DriverManager.getConnection(URL, "", "");

            // Create your Statements and PreparedStatements here

        } catch (SQLException e) {

            System.err.println("Exception connecting to database manager " + e);

            return;

        }

        // start the run loop

        this.start();

    }

    // run -------------------------------------------------------

    public void run() {

        MessageObject msg = null;

        try {

            while (true) {

                // read an object

                msg = (MessageObject) in.readObject();

                if (msg == null)

                    break;

                String transactionType = msg.getTransactionType();

                if (transactionType.equalsIgnoreCase("GETALL")) {

                    handleGetAll();

                } else if (transactionType.equalsIgnoreCase("ADD")) {

                    handleAdd(msg);

                } else if (transactionType.equalsIgnoreCase("UPDATE")) {

                    handleUpdate(msg);

                } else {

                    handleDelete(msg);
                }
            }

        } catch (IOException e) {

            System.err.println("IOException " + e);

        } catch (ClassNotFoundException e) {

            System.err.println("ClassNotFoundException " + e);

        } finally {

            try {

                client.close();

            } catch (IOException e) {

                System.err.println("IOException " + e);
            }

        }

    } // end run

    public synchronized void handleGetAll() {
        ResultSet results = null;
        ArrayList<ArrayList<String>> resultsList = new ArrayList<ArrayList<String>>();
        ArrayList<String> tmp;
        MessageObject newMessage;
        String status = "";
        String message = "";
        int rowsAffected = 0;

        // write the query
        String getAllSQL = "SELECT * FROM cust";
        // generate the statement
        try {
            getAllStmt = con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception ex) {
        }
        // execute the statement
        try {
            results = getAllStmt.executeQuery(getAllSQL);
            status = MessageObject.TransactionTypes.SUCCESS.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            status = MessageObject.TransactionTypes.FAILURE.toString();
            message = e.getMessage();
        } catch (Exception ex) {
            // error executing SQL statement
            status = MessageObject.TransactionTypes.FAILURE.toString();
            message = ex.getMessage();
        }

        try {
            while (results.next()) {
                tmp = new ArrayList<String>();

                tmp.add(results.getString("name"));
                tmp.add(results.getString("ssn"));
                tmp.add(results.getString("address"));
                tmp.add(results.getString("code"));

                resultsList.add(tmp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            status = MessageObject.TransactionTypes.FAILURE.toString();
            message += ", " + e.getMessage();
        }

        newMessage = new MessageObject(status, resultsList, message);
        try {
            out.writeObject(newMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleAdd(MessageObject clientMsg) {
        String status = "";
        String message = "";
        int rowsAffected = 0;
        
        String sql = "INSERT INTO cust (name, ssn, address, code) VALUES "
                + "(?,?,?,?)";
        try {
            addStmt = con.prepareStatement(sql);
            addStmt.setString(1, clientMsg.getName());
            addStmt.setString(2, clientMsg.getSsn());
            addStmt.setString(3, clientMsg.getAddress());
            addStmt.setInt(4, clientMsg.getCode());
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        try {
            rowsAffected = addStmt.executeUpdate();
            status = MessageObject.TransactionTypes.SUCCESS.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            status = MessageObject.TransactionTypes.FAILURE.toString();
            message = e.getMessage();
        }
        
        MessageObject newMessage = new MessageObject(status, message, rowsAffected);
        try {
            out.writeObject(newMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDelete(MessageObject clientMsg) {
        String status = "";
        String message = "";
        int rowsAffected = 0;
        
        String sql = "DELETE FROM cust WHERE ssn = ?";

        try {
            deleteStmt = con.prepareStatement(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            deleteStmt.setString(1, clientMsg.getSsn());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            rowsAffected = deleteStmt.executeUpdate();
            status = MessageObject.TransactionTypes.SUCCESS.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            status = MessageObject.TransactionTypes.FAILURE.toString();
            message = e.getMessage();
        }
        
        MessageObject newMessage = new MessageObject(status, message, rowsAffected);
        try {
            out.writeObject(newMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleUpdate(MessageObject clientMsg) {
        String status = "";
        String message = "";
        int rowsAffected = 0;

        String sql = "UPDATE cust SET address = ? WHERE ssn = ?";

        try {
            updateStmt = con.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            updateStmt.setString(1, clientMsg.getAddress());
            updateStmt.setString(2, clientMsg.getSsn());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            rowsAffected = updateStmt.executeUpdate();
            status = MessageObject.TransactionTypes.SUCCESS.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            status = MessageObject.TransactionTypes.FAILURE.toString();
            message = e.getMessage();
        }
        
        MessageObject newMessage = new MessageObject(status, message, rowsAffected);
        try {
            out.writeObject(newMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}