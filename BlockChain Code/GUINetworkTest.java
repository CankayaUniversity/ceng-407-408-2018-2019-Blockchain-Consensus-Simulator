import static org.junit.Assert.assertTrue;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;

import java.awt.image.BufferedImage ;
import java.util.ArrayList;
import java.awt.image.BufferStrategy ;

public class GUINetworkTest extends JFrame
implements ActionListener,MouseListener
{
	private static final long serialVersionUID = 1L;
	private Container pane ;
	BufferedImage display;
	static int width=1024,height=768;
	BufferStrategy strategy ;
	Network net;
	public boolean isValid=false;
	public int newNodeCount=0;
	public int newNodeFlag=1;
	public int nodeNumber=0;
	public int edgeNumber=0;
	public GUINetworkTest()
	{
		this.init() ;
		this.addWindowListener(new WindowAdapter()
		{ public void windowClosing(WindowEvent e) { System.exit(0); }});

		this.setSize(width, height);
		this.setVisible(true);
		
	}
	public GUINetworkTest(int nodeCount,int flag)
	{
		newNodeCount=nodeCount;
		newNodeFlag=flag;
		++nodeNumber;
		++edgeNumber;
		createNetwork();
	}
	// Initialize the network and some random nodes.
	public void init(){
		pane = getContentPane();
		pane.addMouseListener(this);
		pane.requestFocus();
		Timer clock = new Timer(10, this); 
		clock.start();
	}
	public void createNetwork()
	{
		net = new Network(30, 0.01f, .1f, 0.2f, 100f, 12345);
		// Start with some random nodes.
		for(int k=0;k<nodeNumber;k++){
			ArrayList<String> target = new ArrayList<String>();
			if(k >= edgeNumber){
				for(int j=0;j<nodeNumber;j++){
					String nodeAddress=net.RandomNode();
					target.add(nodeAddress);
				}
			}
			Node node = new TestNode(""+k, target,(float)(.1+Math.random()), 10);
			net.addNode(node, (float)(300 + Math.sin(k * 2 * Math.PI / nodeNumber)*200), (float)(300 + Math.cos(k * 2 * Math.PI / nodeNumber)*200),20);
		}
	}
	// Paint method is override to perform double buffering.
	public void paint(Graphics g){
		if(display==null){
			createBufferStrategy(2);
			strategy = getBufferStrategy();
			display = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g2 = strategy.getDrawGraphics();
		paint2(g2);
		strategy.show();
	}
	
	// App specific drawing goes here to benefit from double buffering.
	public void paint2(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(0,0, width, height);
		net.draw(g,2f);
	}
	public void actionPerformed(ActionEvent e ){
		// Add nodes at random.
		if(Math.random() < 0.003 && newNodeFlag==0){
			ArrayList<String> target = new ArrayList<String>();
			for(int j=0;j<newNodeCount;j++){
				target.add(net.RandomNode());
			}
			Node node = new TestNode(""+(int)(Math.random()*10000), target, (float)(.1+Math.random()), 10);
			net.addNode(node, (float)(100 + Math.random()*800), (float)(100 + Math.random()*500),20);
		}
		repaint();
	}
	public void mousePressed(MouseEvent e){
		pane.requestFocus();
	}
	public void mouseClicked(MouseEvent e){
	}

	public void mouseReleased(MouseEvent e){
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}