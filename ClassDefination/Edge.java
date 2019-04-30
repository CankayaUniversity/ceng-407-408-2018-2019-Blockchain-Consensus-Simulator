
public class Edge extends Network {
	private int Edge_id;
	private int PrevEdge;
	private int NextEdge;
	private boolean isActive=true;
	public int EdgeCount=0;//Networkdeki Edge Sayýsý
	public Edge()
	{
		Edge_id=0;
		PrevEdge=0;
		NextEdge=0;
	}
	public void deleteEdge()
	{
		isActive=false;
	}
	public void setEdgeId(int myId)
	{
		Edge_id=myId;
	}
	public int getEdgeId()
	{
		return Edge_id;
	}
}
