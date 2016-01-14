package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import common.MessageObject;

public class CustClient extends JFrame implements ActionListener {

    // Declare GUI components here
    JButton getAllButton, addButton, updateButton, deleteButton, searchButton;
    JPanel topPanel;
    JTextArea txtOutput;
    JTextField txtName, txtAddress, txtSsn, txtCode;
    JLabel lblName, lblAddress, lblSsn, lblCode, lblClientStatus;

    private Socket connection;

    private ObjectInputStream in;

    private ObjectOutputStream out;

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param args
     */

    public static void main(String[] args) {

        CustClient client = new CustClient();

        client.connectToServer();

    }

    /**
     * 
     * CustClient()
     * 
     * 
     * 
     * Sets up interface and attempts to connect to the server.
     */

    public CustClient() {
        super("Customer Database");
        super.setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(100, 100, 750, 400);
        setLayout(new BorderLayout());
        final ActionListener thisFrame = this;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Code to set up GUI components and listeners
                doGUI();
            }

            private void doGUI() {
                // TOP PANEL
                int txtbxWidth = 5;
                topPanel = new JPanel(new BorderLayout());
                GridLayout grid1 = new GridLayout(2, 4);
                JPanel topA = new JPanel(grid1);
                lblName = new JLabel("Name: ");
                txtName = new JTextField(txtbxWidth);
                lblSsn = new JLabel("SSN: ");
                txtSsn = new JTextField(txtbxWidth);
                lblAddress = new JLabel("Address: ");
                txtAddress = new JTextField(txtbxWidth);
                lblCode = new JLabel("Code: ");
                txtCode = new JTextField(txtbxWidth);
                topA.add(lblName);
                topA.add(txtName);
                topA.add(lblSsn);
                topA.add(txtSsn);
                topA.add(lblAddress);
                topA.add(txtAddress);
                topA.add(lblCode);
                topA.add(txtCode);

                JPanel topB = new JPanel(new FlowLayout());

                getAllButton = new JButton("Get All");
                addButton = new JButton("Add");
                updateButton = new JButton("Update");
                searchButton = new JButton("Search");
                deleteButton = new JButton("Delete");

                getAllButton.addActionListener(thisFrame);
                addButton.addActionListener(thisFrame);
                updateButton.addActionListener(thisFrame);
                searchButton.addActionListener(thisFrame);
                deleteButton.addActionListener(thisFrame);

                topB.add(getAllButton);
                topB.add(addButton);
                topB.add(updateButton);
                topB.add(searchButton);
                topB.add(deleteButton);

                lblClientStatus = new JLabel("Client Disconnected");

                topPanel.add(topA, BorderLayout.NORTH);
                topPanel.add(topB, BorderLayout.CENTER);
                topPanel.add(lblClientStatus, BorderLayout.SOUTH);
                add(topPanel, BorderLayout.CENTER);

                // BOTTOM PANEL
                txtOutput = new JTextArea(15, 10);
                txtOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));

                add(new JScrollPane(txtOutput), BorderLayout.SOUTH);
                repaint();
                revalidate();
            }
        });

    }

    /**
     * 
     * connectToServer()
     * 
     * 
     * 
     * Creates a Socket to connect to the server. If successful, the
     * 
     * input and output streams are obtained.
     */

    public void connectToServer() {

        try {

            // Enter your port number in place of '97xx' in the following
            // statement

            connection = new Socket("turing.cs.niu.edu", 9720);

            System.out.println("Socket opened");
            lblClientStatus.setText("Socket Opened");

            out = new ObjectOutputStream(connection.getOutputStream());

            in = new ObjectInputStream(connection.getInputStream());

            System.out.println("Streams opened");
            lblClientStatus.setText("Streams Opened");

        } catch (UnknownHostException e) {

            System.err.println("Unable to resolve host name");
            lblClientStatus.setText("Client Disconnected");

        } catch (IOException e) {

            System.err.println("Unable to establish connection");

            System.err.println("IOException " + e);
            lblClientStatus.setText("Client Disconnected");

        }

    }

    /**
     * 
     * actionPerformed()
     * 
     * 
     * 
     * Responds to ActionEvents from buttons.
     * 
     * 
     * 
     * @param e
     *            - An ActionEvent.
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isFieldsValidLen()) {
            return;
        }

        if (e.getSource() == getAllButton) {

            handleGetAll();

        } else if (e.getSource() == addButton) {

            handleAdd();

        } else if (e.getSource() == updateButton) {

            handleUpdate();

        } else if (e.getSource() == deleteButton) {

            handleDelete();

        }

    }

    /*
     * This function's purpose is to retrieve all records from the
     * table and display them for the user.
     * 
     * This fucntion sends prepares the request object (messageObject)
     * and then sends it to the server. It receives back a messageobject
     * and then examines that object to determine is the process was
     * successful. This method then relays that information to the user.
     */
    public synchronized void handleGetAll() {
        MessageObject msg = null;
        ResultSet results = null;
        ArrayList<ArrayList<String>> resultsList = new ArrayList<ArrayList<String>>();
        int cnt = 0;
        int space = 0;
        int spaces = 0;

        // send request to the server
        try {
            out.writeObject(new MessageObject(
                    MessageObject.TransactionTypes.GETALL.toString()));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

     // Retrieve status results from the server
        try {
            msg = (MessageObject) in.readObject();
            resultsList = msg.getResults();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // inform the user of the failure and the reason for the failure if
        // there is one
        if (msg.getTransactionType().compareTo(
                MessageObject.TransactionTypes.FAILURE.toString()) == 0) {
            txtOutput.append("Failed: " + msg.getMessage());
            txtOutput.append("  \n");
            return;
        }

        // if the resultsList is empty then just report that to the user
        if (resultsList.size() == 0) {
            txtOutput.append("The Table is empty.");
            txtOutput.append("  \n");
        }

        // display the results neatly to the user
        // this will calculate the allotted space and
        // buffer it for display.
        for (ArrayList<String> recordList : resultsList) {
            for (String record : recordList) {
                txtOutput.append(record);
                if (++cnt % 4 == 1)
                    space = 20;
                else if (cnt % 4 == 2)
                    space = 10;
                else if (cnt % 4 == 3)
                    space = 40;
                else
                    space = 10;
                spaces = space - record.length();
                for (int i = 0; i < spaces; i++)
                    txtOutput.append(" ");
                // 2 spaces for padding
                txtOutput.append("  ");
            }
            txtOutput.append("  \n");
        }
    }

    /*
     * This functions purpose is to allow the user to add a record to
     * the table. It validates that the code field is a valid number.
     * 
     * This fucntion sends prepares the request object (messageObject)
     * and then sends it to the server. It receives back a messageobject
     * and then examines that object to determine is the process was
     * successful. This method then relays that information to the user.
     */
    public void handleAdd() {
        int code = 0;
        MessageObject msg = null;

        try {
            code = Integer.parseInt(txtCode.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Invalid number entry for code", "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

     // send request to the server
        try {
            out.writeObject(new MessageObject(
                    MessageObject.TransactionTypes.ADD.toString(), txtName
                            .getText(), txtSsn.getText(), txtAddress.getText(),
                    code));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retrieve status results from the server
        try {
            msg = (MessageObject) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // inform the user of the failure and the reason for the failure if
        // there is one
        if (msg.getTransactionType().compareTo(
                MessageObject.TransactionTypes.FAILURE.toString()) == 0) {
            txtOutput.append("Failed: " + msg.getMessage());
            txtOutput.append("  \n");
            return;
        } else {
            txtOutput.append(msg.getAffectedRows() + " Rows effected.");
            txtOutput.append("  \n");
        }
    }

    /*
     * This function allows a user to update the address of a record
     * by targeting the record's ssn. If either field is empty is will
     * prompt the user of an input error.
     * 
     * This fucntion sends prepares the request object (messageObject)
     * and then sends it to the server. It receives back a messageobject
     * and then examines that object to determine is the process was
     * successful. This method then relays that information to the user.
     */
    public void handleUpdate() {
        int code = 0;
        MessageObject msg = null;

        if (txtSsn.getText().isEmpty() || txtAddress.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry. SSN and address must not be blank!",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

     // send request to the server
        try {
            out.writeObject(new MessageObject(
                    MessageObject.TransactionTypes.UPDATE.toString(), txtName
                            .getText(), txtSsn.getText(), txtAddress.getText(),
                    code));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retrieve status results from the server
        try {
            msg = (MessageObject) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (msg.getTransactionType().compareTo(
                MessageObject.TransactionTypes.FAILURE.toString()) == 0) {
            txtOutput.append("Failed: " + msg.getMessage());
            txtOutput.append("  \n");
            return;
        } else {
            txtOutput.append(msg.getAffectedRows() + " Rows effected.");
            txtOutput.append("  \n");
        }
    }

    /*
     * This fucntion allows the user to delete a record by targeting
     * the ssn. The ssn field cannot be blank for this operation and
     * a popup error message is displayed to the user if it is.
     * 
     * This fucntion sends prepares the request object (messageObject)
     * and then sends it to the server. It receives back a messageobject
     * and then examines that object to determine is the process was
     * successful. This method then relays that information to the user.
     */
    public void handleDelete() {
        int code = 0;
        MessageObject msg = null;

        if (txtSsn.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry. SSN must not be blank!", "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

     // send request to the server
        try {
            out.writeObject(new MessageObject(
                    MessageObject.TransactionTypes.DELETE.toString(), txtName
                            .getText(), txtSsn.getText(), txtAddress.getText(),
                    code));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retrieve status results from the server
        try {
            msg = (MessageObject) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

     // inform the user of the failure and the reason for the failure if there is one
        if (msg.getTransactionType().compareTo(
                MessageObject.TransactionTypes.FAILURE.toString()) == 0) {
            txtOutput.append("Failed: " + msg.getMessage());
            txtOutput.append("  \n");
            return;
        } else {
            txtOutput.append(msg.getAffectedRows() + " Rows effected.");
            txtOutput.append("  \n");
        }
    }

    /*
     * This function checks each text box to ensure
     * that each is within the length limitations
     * that the database requires. It returns true
     * if all text fields are valid. It returns false
     * if any one is not. It will also display a pop-up
     * error to the user.
     */
    public boolean isFieldsValidLen() {
        if (txtName.getText().length() > 20) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry. Name must be fewer than 20 characters. It is currently: "
                            + txtName.getText().length(), "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtSsn.getText().length() > 10) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry. SSN must be fewer than 10 characters. It is currently: "
                            + txtSsn.getText().length(), "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtAddress.getText().length() > 40) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry. Address must be fewer than 40 characters. It is currently: "
                            + txtAddress.getText().length(), "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtCode.getText().length() > 10) {
            JOptionPane.showMessageDialog(null,
                    "Invalid entry. Code must be fewer than 10 characters. It is currently: "
                            + txtCode.getText().length(), "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

}
