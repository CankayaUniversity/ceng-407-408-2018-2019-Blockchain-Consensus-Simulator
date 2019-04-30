
public class Transaction {
	public int TransactionId;
	private int fee;
	private int speed;
	
	public Transaction()
	{
	}
	public void SetTransactionFee(int newFee)
	{
		fee=newFee;
	}
	public int GetTransactionFee()
	{
		return fee;
	}
	public void SetTransactionSpeed(int newSpeed)
	{
		speed=newSpeed;
	}
	public int getTransactionSpeed()
	{
		return speed;
	}
}
