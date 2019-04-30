import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
public class Node extends Edge {	
	private int Node_id=1;
	private int []Transaction_id;
	private int []Edge_id;
	private boolean isActive=true;
	
	public Node()
	{
		Timer myTimer=new Timer();
		TimerTask myTask=new TimerTask(){
			public void run()
			{
				Transaction tr=new Transaction();
				Transaction_id[Node_id]=tr.TransactionId;
			}
		};
		myTimer.schedule(myTask, 0,3000);
		Random r=new Random();
		int number=0;
		if(EdgeCount>0)
			number=r.nextInt(EdgeCount);//Edge sayýsý kadar rastgele sayý üret ve baðlantýyý rastgele yap
		else
			number=1;
		Edge_id[Node_id]=number;
		Node_id+=1;
		isActive=true;
	}
	public void DeleteNode()
	{
		isActive=false;
		Node_id=0;
	}
	public void AddTransaction(int fee)
	{
		int mysize=Transaction_id.length;
		Transaction tr=new Transaction();
		tr.TransactionId+=1;
		tr.SetTransactionFee(fee);
		tr.SetTransactionSpeed(10);
		++mysize;
		Transaction_id[mysize]=tr.TransactionId;
	}
	public void ChangeTransactionFee(int start,int finish)
	{
		
	}
	public void ChangeHashRate(int hashRate)
	{
		
	}
	public void ChangeStake()
	{
		
	}
}
