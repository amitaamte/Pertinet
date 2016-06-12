/**
 * Student ID: 17412423
 * Student Name: Amita Shirish Amte
 *  
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Method to parse input file and display result
public class SolvePetrinet {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		List<Place> place = new ArrayList<Place>();
		List<Transition> tran = new ArrayList<Transition>();

		//Reading name of file and max cycle iteration entered from command line
		String filename = args[0];
		String max = args[1];
		int max1 = Integer.valueOf(max);
		// create a Buffered Reader object instance with a FileReader
		BufferedReader file = new BufferedReader(new FileReader(filename));
		// read the first line from the text file
		String read = file.readLine();
		Petrinet p2 = new Petrinet("common petrinet object");
		Transition t1 = new Transition("Common Transition");
		
		//Repeat loop till entire file is parsed
		while(read!=null){
			// Using space as a delimiter to read values
			String[] delimiter = read.split(" ");

			//Checks if entry is place,edge or transition and inserts it into correct list
			if (delimiter[0].equals("place"))
			{
				Place p = new Place(delimiter[1], Integer.valueOf(delimiter[2]));
				p2.add(p);
			} else if (delimiter[0].equals("transition"))
			{
				Transition t = new Transition(delimiter[1]);
				p2.add(t);
			} else if (delimiter[0].equals("edge"))
			{
				boolean flag = true;

				for (Place p : p2.places) {
					if (delimiter[1].equals(p.getName())) {
						for (Transition t : p2.transitions) {
							if (delimiter[2].equals(t.getName())) {
								Arc a = new Arc("incoming", p, t);
								p2.add(a);
								flag = false;
								break;
							}
						}
						break;
					}
				}

				if (flag) {
					for (Transition t : p2.transitions) {
						if (delimiter[1].equals(t.getName())) {
							for (Place p : p2.places) {
								if (delimiter[2].equals(p.getName())) {
									Arc a = new Arc("outgoing", t, p);
									p2.add(a);
									break;
								}
							}
							break;
						}
					}
				}
			}

			read = file.readLine();
		}

		file.close();

		List<Transition> fire = new ArrayList<Transition>();
		Random rand = new Random();
		for (int i = 0; i < max1; i++) {
			fire = p2.getTransitionsAbleToFire();
			int size = fire.size();
			int a = rand.nextInt(size);
			Transition t = fire.get(a);
			t.fire();
			System.out.println("Cycle"+" "+(i+1));
			for(Place p4 : p2.places)
			{
				System.out.print(p4.getName()+" ");
				System.out.println(p4.getTokens());
			}
		}
	}
}

class PetrinetObject {

	private String name;

	public PetrinetObject(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}

class Place extends PetrinetObject{

	public static final int UNLIMITED = 100;
	private int tokens = 0;
	private int maxTokens = UNLIMITED;


	protected Place(String name) {
		super(name);
	}

	protected Place(String name, int initial) {
		this(name);
		this.tokens = initial;
		
	}

	public boolean hasAtLeastTokens(int threshold) {
		return (tokens >= threshold);
	}


	public boolean maxTokensReached(int newTokens) {
		if (hasUnlimitedMaxTokens()) {
			return false;
		}

		return (tokens+newTokens > maxTokens);
	}

	private boolean hasUnlimitedMaxTokens() {
		return maxTokens == UNLIMITED;
	}


	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;
	}

	public void setMaxTokens(int max) {
		this.maxTokens = max;
	}

	public void addTokens(int weight) {
		this.tokens += weight;
	}

	public void removeTokens(int weight) {
		this.tokens -= weight;
	}
}

//Edges that can fire
class Arc extends PetrinetObject {

	Place place;
	Transition transition;
	Direction direction;
	int weight = 1;

	enum Direction {

		PLACE_TO_TRANSITION {
			@Override
			public boolean canFire(Place p, int weight) {
				return p.hasAtLeastTokens(weight);
			}

			@Override
			public void fire(Place p, int weight) {
				p.removeTokens(weight);
			}

		},

		TRANSITION_TO_PLACE {
			@Override
			public boolean canFire(Place p, int weight) {
				return ! p.maxTokensReached(weight);
			}

			@Override
			public void fire(Place p, int weight) {
				p.addTokens(weight);
			}

		};

		public abstract boolean canFire(Place p, int weight);

		public abstract void fire(Place p, int weight);
	}

	private Arc(String name, Direction d, Place p, Transition t) {
		super(name);
		this.direction = d;
		this.place = p;
		this.transition = t;
	}

	protected Arc(String name, Place p, Transition t) {
		this(name, Direction.PLACE_TO_TRANSITION, p, t);
		t.addIncoming(this);
	}

	protected Arc(String name, Transition t, Place p) {
		this(name, Direction.TRANSITION_TO_PLACE, p, t);
		t.addOutgoing(this);
	}

	public boolean canFire() {
		return direction.canFire(place, weight);
	}

	public void fire() {
		this.direction.fire(place, this.weight);
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return weight;
	}
}

// Edge that can't fire
class InhibitorArc extends Arc {


	protected InhibitorArc(String name, Place p, Transition t) {
		super(name, p, t);
	}

	@Override
	public boolean canFire() {
		return (place.getTokens() < this.getWeight());
	}


	@Override
	public void fire() {
		
	}
}

class Transition extends PetrinetObject{

	protected Transition(String name) {
		super(name);
	}

	private List<Arc> incoming = new ArrayList<Arc>();
	private List<Arc> outgoing = new ArrayList<Arc>();

	public boolean canFire() {
		boolean canFire = true;
		canFire = ! this.isNotConnected();

		for (Arc arc : incoming) {
			canFire = canFire & arc.canFire();
		}

		for (Arc arc : outgoing) {
			canFire = canFire & arc.canFire();
		}
		return canFire;
	}

	public void fire() {
		for (Arc arc : incoming) {
			arc.fire();
		}

		for (Arc arc : outgoing) {
			arc.fire();
		}
	}

	public void addIncoming(Arc arc) {
		this.incoming.add(arc);
	}

	public void addOutgoing(Arc arc) {
		this.outgoing.add(arc);
	}

	public boolean isNotConnected() {
		return incoming.isEmpty() && outgoing.isEmpty();
	}

	public void print1()
	{
		for (Arc a : incoming)
		{
			System.out.println(a.getName());
		}
		for (Arc a : outgoing)
		{
			System.out.println(a.getName());
		}
	}

}

class Petrinet extends PetrinetObject {

	private static final String nl = "\n";
	List<Place> places              = new ArrayList<Place>();
	List<Transition> transitions    = new ArrayList<Transition>();
	List<Arc> arcs                  = new ArrayList<Arc>();
	List<InhibitorArc> inhibitors   = new ArrayList<InhibitorArc>();

	public Petrinet(String name) {
		super(name);
	}

	public void add(PetrinetObject o) {
		if (o instanceof InhibitorArc) {
			inhibitors.add((InhibitorArc) o);
		} else if (o instanceof Arc) {
			arcs.add((Arc) o);
		} else if (o instanceof Place) {
			places.add((Place) o);
		} else if (o instanceof Transition) {
			transitions.add((Transition) o);
		}
	}



	public List<Transition> getTransitionsAbleToFire() {
		ArrayList<Transition> list = new ArrayList<Transition>();
		for (Transition t : transitions) {
			if (t.canFire()) {
				list.add(t);
			}
		}
		return list;
	}

	public Transition transition(String name) {
		Transition t = new Transition(name);
		transitions.add(t);
		return t;
	}

	public Place place(String name) {
		Place p = new Place(name);
		places.add(p);
		return p;
	}

	public Place place(String name, int initial) {
		Place p = new Place(name, initial);
		places.add(p);
		return p;

	}

	public Arc arc(String name, Place p, Transition t) {
		Arc arc = new Arc(name, p, t);
		arcs.add(arc);
		return arc;
	}

	public Arc arc(String name, Transition t, Place p) {
		Arc arc = new Arc(name, t, p);
		arcs.add(arc);
		return arc;
	}

	public InhibitorArc inhibitor(String name, Place p, Transition t) {
		InhibitorArc i = new InhibitorArc(name, p, t);
		inhibitors.add(i);
		return i;
	}

	public void print()
	{
		for (Place p: places)
		{
			System.out.print(p.getName()+" ");
			System.out.println(p.getTokens());
		}
		for (Transition t: transitions)
		{
			System.out.println(t.getName());
		}
		for (Arc a : arcs)
		{
			System.out.print(a.getName());
			System.out.println(a.getWeight());
			System.out.println(a.canFire());
		}
	}


	public List<Place> getPlaces() {
		return places;
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public List<Arc> getArcs() {
		return arcs;
	}

	public List<InhibitorArc> getInhibitors() {
		return inhibitors;
	}

}

/*Note: Since I am a beginner in programming, I would like to let you know that I did go through the Internet
* for developing the logic and generating the following code. Sorry for the same as else I wouldn't be able to learn
* and code. But I did learn a lot through the process.
* */








