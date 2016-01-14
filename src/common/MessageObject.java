package common;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageObject implements Serializable{
    
    private String name, ssn, address, message;
    private int code, affectedRows;
    
    public enum TransactionTypes {
        GETALL, ADD, DELETE, UPDATE, SUCCESS, FAILURE
    };

    private String TransactionType = null;
    ArrayList<ArrayList<String>> results;

    //CONSTRUCTORS
    public MessageObject(String transTypeIn, ArrayList<ArrayList<String>> resultsIn, String messageIn) {
        results = resultsIn;
        TransactionType = transTypeIn;
        message = messageIn;
    }
    
    public MessageObject(String transTypeIn, String messageIn, int rowsAffectedIn){
        TransactionType = transTypeIn;
        message = messageIn;
        affectedRows = rowsAffectedIn;
    }
    
    public MessageObject(String transTypeIn,String nameIn, String ssnIn, String addressIn, int codeIn){
        TransactionType = transTypeIn;
        name = nameIn;
        ssn = ssnIn;
        address = addressIn;
        code = codeIn;
    }
    
    /**
     * @return the results
     */
    public ArrayList<ArrayList<String>> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(ArrayList<ArrayList<String>> results) {
        this.results = results;
    }
    
    public MessageObject(String transTypeIn) {
        TransactionType = transTypeIn;
    }

    /**
     * @param transactionType
     *            the transactionType to set
     */
    public void setTransactionType(String transactionType) {
        TransactionType = transactionType;
    }

    public String getTransactionType() {
        return TransactionType;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the ssn
     */
    public String getSsn() {
        return ssn;
    }

    /**
     * @param ssn the ssn to set
     */
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }
    
    public int getAffectedRows() {
        return affectedRows;
    }
    
    public String getMessage() {
        return message;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }
}
