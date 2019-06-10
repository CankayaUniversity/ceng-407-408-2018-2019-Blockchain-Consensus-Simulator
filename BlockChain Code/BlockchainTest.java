import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.time.*;
import java.util.Random;
import test.SHA256;

public class BlockchainTest {
	
	public Blockchain<Transaction> blocks = new Blockchain<Transaction>();
	public List<Miner> minerList = new ArrayList<Miner>();
	public int blockNumber=0;
	private Node n;
	BlockchainTest(Node node)
	{
		n=node;
	}
	public void CreateBlock(int flag)
	{	
		if(flag==0)
		{
			while(true)
			{
				Random r=new Random(); //random sýnýfý
				int a=r.nextInt(100);
				String transactionName=Integer.toString(a);
				try {
					if(n.transactionCount==10) {
						System.out.print("\nAll transaction is added!!\n");
						break;
					}
					else {
						TimeUnit.MILLISECONDS.sleep(50);
						Transaction t=new Transaction(transactionName);
						blocks.add(t);
						n.transactionList.add(t);
						++n.transactionCount;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(flag==1)
		{
			++n.transactionCount;
			Transaction t=new Transaction(Integer.toString(n.transactionCount));
			n.transactionList.add(t);
			blocks.add(t);
		}
		++blockNumber;
		testChain();
	}
	public void testChain()
	{
		Blockchain<Transaction> chain2 = new Blockchain<Transaction>();
		chain2=blocks.Clone();
		System.out.println(String.format("Chain 1 Hash: %s", blocks.getHead().getHash()));
		System.out.println(String.format("Chain 2 Hash: %s", chain2.getHead().getHash()));
		System.out.println(
				String.format("Chains Are In Sync: %s", blocks.getHead().getHash().equals(chain2.getHead().getHash())));
		printCurrentChain();
		assertTrue(blocks.blockChainHash().equals(chain2.blockChainHash()));
		assertTrue(blocks.validate());
		System.out.println(String.format("Chain is Valid: %s", blocks.validate()));
		blockMinerTest(n.transactionList);
	}
	public String getChainHash()
	{
		return blocks.getHead().getHash();
	}
	public void printCurrentChain()
	{
		System.out.println("Current Chain Head Transactions: ");
		for (Block block : blocks.chain) {
			for (Object tx : block.getTransactions()) {
				System.out.println("\t" + tx);
			}
		}
	}
	
	public void blockMinerTest(List<Transaction> t) {
		for (int i = 0; i < minerList.size(); i++) {
			for(int j=0;j<t.size();j++)
			{
				minerList.get(i).mine(t.get(j));
			}
		}
		System.out.println("Number of Blocks Mined = " + blocks.getChain().size());
		System.out.print("Name of the Block Mined:");
		System.out.print(blocks.getChain().toString());
		System.out.print("\n");
	}
	 
	public void testValidateBlockchain() {

		Blockchain<Transaction> chain = new Blockchain<Transaction>();	
		for (int i = 0; i < minerList.size() ; i++) {
	   	       chain.add(new Transaction("tx:"+i));
		}
		// is chain valid 
		System.out.println(String.format("Chain is Valid: %s", blocks.validate()));
        // get second block from chain and add a tx..		
		Block<Transaction> block = chain.getChain().get(1);  
		Transaction tx = new Transaction("X");
		block.add(tx);
		// is chain valid, should not be changed a block... 
		System.out.println(String.format("Chain is Valid: %s", blocks.validate()));

	}

}
