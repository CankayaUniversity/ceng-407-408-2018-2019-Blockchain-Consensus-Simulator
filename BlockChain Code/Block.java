import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.google.gson.Gson;
import test.SHA256;


public class Block<T extends Tx> {
	public long timeStamp;
	private int index;
	private List<T> transactionList = new ArrayList<T>();
	private String Blockhash;
	private String previousHash;
	private String merkleRoot;
	private String nonce = "0000";
	
	// caches Transaction SHA256 hashes
    public Map<String,T> map = new HashMap<String,T>();
    
	public Block<T> add(T tx) {
		transactionList.add(tx);
		map.put(tx.hash(), tx);
		computeHash();
		return this;
	}
	public Block<T> Clone() {
		Block<T> clone = new Block();
		clone.setIndex(this.getIndex());
		clone.setPreviousHash(this.getPreviousHash());
		clone.setMerkleRoot(this.getMerkleRoot());
		clone.setTimeStamp(this.getTimeStamp());
		
		List<T> clonedtx = new ArrayList<T>();
		Consumer<T> consumer = (t) -> clonedtx.add(t);
		this.getTransactions().forEach(consumer);
	    clone.setTransactions(clonedtx);
		
		return clone;
	}

	public void computeHash() {
		  Gson parser = new Gson();
		  String serializedData = parser.toJson(transactionList);	  
		  setHash(SHA256.generateHash(timeStamp + index + merkleRoot + serializedData + nonce + previousHash));
	}
	
	public String getHash() {
		
		// calc hash if not defined, just for testing...
		if (Blockhash == null) {
		   computeHash();
		}
		
		return Blockhash;
	}
	
	public void setHash(String h) {
		this.Blockhash = h;	
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}

	public List<T> getTransactions() {
		return transactionList;
	}

	public void setTransactions(List<T> transactions) {
		this.transactionList = transactions;
	}
	
	public String getMerkleRoot() {
		return merkleRoot;
	}

	public void setMerkleRoot(String merkleRoot) {
		this.merkleRoot = merkleRoot;
	}
	
	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	
	
}