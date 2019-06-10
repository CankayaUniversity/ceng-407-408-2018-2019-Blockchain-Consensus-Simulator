
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Network implements Runnable{

	ConcurrentHashMap<String, Node> nodes; // Node address -> node object
	ConcurrentHashMap<String, Link> links; //  from+"-"+to > Link
	PriorityBlockingQueue<Transmission> transmit_queue; // Transmissions that have not arrived yet
	public List<Node> nodeList = new ArrayList<Node>();
	public List<BlockchainTest> blockList = new ArrayList<BlockchainTest>();
	boolean stopped = false; // Stops the network's thread if set to true.

	Random rand; // A pseudo-random number generator, so well constructed experiments can be replicated.
	float default_link_rate; // Rate in bytes/time for a link not manually initialized.
	float latency_per_distance; // Latency is determined by pixel distance between nodes if link is not manually initialized.
	float max_random_latency; // Each link is also given an additional latency at random up to this amount.
	float packet_drop_chance; // The chance that a packet will be dropped.
	float clock_desynchronization; // Node clocks maybe be off from the network clock by up to this amount.
	
	// Current time in the network.
	double net_time = 0 ;
	// Last system time the the network time was updated at.
	long last_time = System.currentTimeMillis() ;
	// The rate of passage of net_time relative to system time.
	public double time_speed=1;

	// Initializes a network with the given default values for links.
	// Also starts the network's thread.
	public Network(
			float default_link_rate, 
			float latency_per_distance, 
			float max_random_latency,
			float packet_drop_chance,
			float clock_desynchronization,
			int random_seed){
		this.default_link_rate = default_link_rate;
		this.latency_per_distance = latency_per_distance;
		this.max_random_latency = max_random_latency;
		this.packet_drop_chance = packet_drop_chance;
		nodes = new ConcurrentHashMap<String, Node>();
		links = new ConcurrentHashMap<String, Link>();
		transmit_queue = new PriorityBlockingQueue<Transmission>();
		rand = new Random(random_seed);
		Thread t = new Thread(this);
		t.start();
	}
	public Network(String addressOfNode,int processNumber)
	{
		for(int i=0;i<nodeList.size();i++)
		{
			if(nodeList.get(i).address==addressOfNode)
			{
				if(processNumber==0)//hash rate
				{
					
				}
				else if(processNumber==1)//add transaction
				{
					String s=Integer.toString(nodeList.get(i).transactionCount+=1);
					nodeList.get(i).transactionList.add(new Transaction(s));
				}
				else if(processNumber==2)//delete edge
				{
					
				}
				else if(processNumber==3)//delete node
				{
					stop(addressOfNode);
				}
				else if(processNumber==4)//delete network
				{
					stop();
				}
			}
		}
	}
	// Adds a node to the network at the given position with the given download rate.
	// Also starts the node's thread.
	public synchronized void addNode(Node n, float x, float y, float rate){
		nodeList.add(n);
		BlockchainTest b=new BlockchainTest(n);
		b.CreateBlock(0);
		blockList.add(b);
		n.x = x;
		n.y = y;
		n.refill_rate = rate;
		n.maximum_flow = 10*rate;
		n.flow = n.maximum_flow;
		n.last_time = getTime();
		n.clock_offset =(rand.nextFloat()*2f-1f)*clock_desynchronization;
		n.network = this;
		nodes.put(n.address, n);
		Thread t = new Thread(n);
		t.start();
	}
	// Note links will be created automatically as needed with latency based on distance.
	// This method is only needed if you want to change a link from the default latency(net time) or rate (bytes/net time).
	public void setLink(String from, String to, float latency, float rate){
		links.put(from+"-"+to, new Link(from, to, latency, rate, rate*10, getTime()));
	}

	// Returns the simulation time since the network started. The "network time".
	// time_speed is public and can be adjusted at run time.
	// Nodes should use Node.getTime() to properly simulate clock desycnrhonization.
	public double getTime(){
		long current_time = System.currentTimeMillis();
		net_time+=(current_time-last_time)*time_speed/1000.0;
		last_time = current_time ;
		return net_time ;
	}

	// Attempts to send a message over the network. Nodes should use the Node send function.
	// Queues the message and figures out the arrival time, delivering appropriately.
	public void sendMessage(String from, String to, byte[] message){
		Node f = nodes.get(from), t = nodes.get(to);
		if(f!=null && !f.stopped && t!=null && !t.stopped){ // Verify nodes are running on the network.
			Link l = links.get(from+"-"+to);
			if(l == null){
				l = new Link(from, to, 
						latency_per_distance * distance(from,to) + rand.nextFloat()*max_random_latency,
						default_link_rate, default_link_rate*10, getTime());
				links.put(from+"-"+to, l);	
			}
			double sent = getTime();
			// Each link has a maximum throughput and each node has a maximum download rate.
			// Message cannot exceed either of these.
			double arrival = Math.max(l.sendTime(message.length, sent), t.sendTime(message.length, sent));
			Transmission m = new Transmission (from, to, message, sent, arrival);
			transmit_queue.add(m);
		}
	}

	// Stops the threads of the network and all nodes in the network.
	public void stop(){
		Iterator<String> i = nodes.keySet().iterator();
		while(i.hasNext()){
			nodes.get(i.next()).stop();
		}
		stopped = true;
	}

	// Stops a specific node by address.
	public void stop(String node){
		nodes.get(node).stop();
	}

	// Returns the ID of a random node in the network.
	// Useful for connecting new nodes and creating random failures.
	// Returns "" if there are no nodes.
	public synchronized String RandomNode(){
		if(nodes.size()==0) return "";
		int which =(int)(nodes.size()*rand.nextFloat());
		Iterator<String> i = nodes.keySet().iterator();
		int w = 0;
		while(w++ < which) i.next();
		return nodes.get(i.next()).address;
	}
	// The main run method of the network. It basically just waits to deliver messages.
	public void run() {
		while(!stopped){
			double time = getTime();
			while(transmit_queue.size() > 0 && transmit_queue.peek().arrivaltime < time){
				Transmission m = transmit_queue.poll();
				// Verify receiver node is running before delivering message.
				Node t = nodes.get(m.to);
				if(t!=null && !t.stopped && !m.dropped){
					t.receive(m.from, m.message);
				}
			}
			try{Thread.sleep(2); } catch(InterruptedException e){}
		}
	}

	// Draws the network to the given graphics object.
	// Links that have not been used within the given time will not be shown.
	public void draw(Graphics g, float inactive_time){
		// Draw links.
		Iterator<String> i = links.keySet().iterator();
		while(i.hasNext()){
			links.get(i.next()).draw(g, inactive_time);
		}
		// Draw nodes.
		i = nodes.keySet().iterator();
		while(i.hasNext()){
			nodes.get(i.next()).draw(g);
			int x1,y1,x2,y2;
	        x1=200;
	        y1=650;
	        x2=300;
	        y2=650;
	        
	        //Declaring coordinates for first cube
	        int x1p1=100,x1p2=100,x1p3=200,x1p4=200,x1p5=100;
	        int y1p1=600,y1p2=700,y1p3=700,y1p4=600,y1p5=600;
	        
	        //Declare g2d and set color
	        Graphics2D g2d = (Graphics2D) g;
	        g2d.setColor(Color.WHITE);
	        int x=70;
	        int y=730;
	        //Create General Path
	        GeneralPath cube = new GeneralPath();
	        //Create and draw cubes and lines
	        for(int j=0;j<nodeList.size();j++) {
	        	// one base
	            int base1X[] = {x1p1, x1p2, x1p3, x1p4, x1p5};
	            int base1Y[] = {y1p1, y1p2, y1p3, y1p4, y1p5};
	            // second base
	            int base2X[] = {x1p1-25, x1p2-25, x1p3-25, x1p4-25, x1p5-25};
	            int base2Y[] = {y1p1-25, y1p2-25, y1p3-25, y1p4-25, y1p5-25};
	        	for (int k = 1; k <= 4; k++) {
	        		// create the first base
	        		            cube.moveTo(base1X[ k - 1], base1Y[ k - 1]);
	        		            cube.lineTo(base1X[ k], base1Y[ k]);

	        		// create the second base
	        		            cube.moveTo(base2X[ k - 1], base2Y[ k - 1]);
	        		            cube.lineTo(base2X[ k], base2Y[ k]);

	        		// create the lines between the bases
	        		            cube.moveTo(base1X[ k], base1Y[ k]);
	        		            cube.lineTo(base2X[ k], base2Y[ k]);          
	        	}
	        	//Draw cube
	        	g2d.draw(cube);        	
	        	//Draw line
	        	if(j<nodeList.size()-1) {
	            	g.drawLine(x1, y1, x2, y2);  
	        	}        	
	        	//Update x coordinates to draw next line 
	        	x1+=200;
	        	x2+=200;        	
	        	//Update x coordinates to draw next cube 
	        	x1p1+=200;
	        	x1p2+=200;
	        	x1p3+=200;
	        	x1p4+=200;
	        	x1p5+=200;
	            g2d.drawString("Blocks Mined:"+blockList.get(j).blocks.getChain().toString(), x, y);
	            x+=200;
	        }   
	    }
		// Draw transactions
		Iterator<Transmission> i2 = transmit_queue.iterator();
		while(i2.hasNext()){
			i2.next().draw(g);
		}
	}
	
	// Distance between nodes.
	public float distance(String from, String to){
		Node a = nodes.get(from);
		Node b = nodes.get(to);
		if(a == null || b == null){
			return 99999999;
		}else{
			return (float)Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
		}
	}

	// Link class for keeping track of flow rate of links.
	private class Link{
		String from, to;
		public float latency; // seconds
		public float refill_rate; // bytes per second (recharges flow over time)
		public float maximum_flow; // bytes at maximum flow
		public float flow; // current available flow
		public double last_time; // time flow was last updated
		public double lastarrival; // last time a message arrived at a destination.

		public Link(String from, String to, float latency, float refill, float max, double time){
			this.from = from;
			this.to = to;
			this.latency = latency;
			this.refill_rate = refill;
			this.maximum_flow = max;
			this.last_time = time;
			this.flow = max;
		}
		// Updates the amount of available flow.
		public void updateFlow(double time){
			flow += refill_rate * (time - last_time);
			if(flow > maximum_flow){
				flow = maximum_flow;
			}
			last_time = time;
		}

		// Returns the time at which the message will arrive considering pipe width and latency
		public double sendTime(int size, double request_time){
			updateFlow(request_time); // enforces flow build up cap out when not in use.
			double arrival = request_time + latency + Math.max((size-flow) / refill_rate, 0);
			flow -= size ; 
			lastarrival = arrival;
			return arrival;
		}

		// Draws a link if it's been active recently. Scales from green to black to red based on load.
		public synchronized void draw(Graphics gr, float inactivitetime){
			if(getTime() - lastarrival < inactivitetime){
				int r=0,g=0;
				float mid = maximum_flow/2;
				if( flow < mid){
					r = (int)(255* (mid - flow) / mid) ;
				} else {
					g =  (int)(255* (flow - mid) / mid) ;
				}
				gr.setColor(Color.ORANGE);
				Node f = nodes.get(from); 
				Node t = nodes.get(to);
				if(f!=null && t!=null && !f.stopped && !t.stopped){
					int oy = 10;
					if(f.address.compareTo(t.address) < 0){
						oy = -10;
					}
					gr.drawLine((int)f.x, (int)f.y+oy, (int)t.x, (int)t.y+oy);
				}
			}
		}
	}
	// Transmission class for queueing messages to arrive at future times.
	private class Transmission implements Comparable<Transmission>{
		double senttime ;
		double arrivaltime ;
		String from;
		String to;
		byte[] message;
		boolean dropped;
		public Transmission(String from, String to, byte[] message, double senttime, double arrivaltime){
			this.from = from;
			this.to = to;
			this.message = message;
			this.senttime = senttime;
			this.arrivaltime = arrivaltime;
			dropped = rand.nextFloat() < packet_drop_chance;
		}
		public int compareTo(Transmission o) {
			return (int)(10000 * (arrivaltime - o.arrivaltime));
		}

		public synchronized void draw(Graphics g){
			Node f = nodes.get(from), t = nodes.get(to);
			if( f != null && t != null && !f.stopped && !t.stopped){
				double s = (getTime()-senttime) / (float)(arrivaltime - senttime);
				float x = (float)((1-s) * f.x + s * t.x), y = (float)((1-s)*f.y + s*t.y);
				if(dropped){
					g.setColor(Color.RED);
				}else{
					g.setColor(Color.WHITE);
				}
				int oy = 10;
				if(f.address.compareTo(t.address) < 0){
					oy = -10;
				}
				g.drawOval((int)x-2, (int)(y-2)+oy,8,8);
			}
		}
	}
}