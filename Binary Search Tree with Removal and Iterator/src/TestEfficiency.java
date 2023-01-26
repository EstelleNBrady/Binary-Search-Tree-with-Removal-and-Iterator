import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import edu.uwm.cs351.Appointment;
import edu.uwm.cs351.Duration;
import edu.uwm.cs351.NewApptBook;
import edu.uwm.cs351.Period;
import edu.uwm.cs351.Time;
import junit.framework.TestCase;


public class TestEfficiency extends TestCase {	
	Time now;
	NewApptBook s;
	Random r;
	
	@Override
	public void setUp() {
		s = new NewApptBook();
		r = new Random();
		now = new Time();
		try {
			assert 1/s.size() == 42 : "OK";
			assertTrue(true);
		} catch (ArithmeticException ex) {
			System.err.println("Assertions must NOT be enabled to use this test suite.");
			System.err.println("In Eclipse: remove -ea from the VM Arguments box under Run>Run Configurations>Arguments");
			assertFalse("Assertions must NOT be enabled while running efficiency tests.",true);
		}
	}

	private static final int POWER = 20;
	private static final int MAX_LENGTH = 1 << POWER; // one million
	private static final int SAMPLE = 100;
	
	private Appointment newAppt(int i) {
		return new Appointment(new Period(now,Duration.SECOND.scale(i+1)),"zzz");
	}

	private void addRange(int lo, int hi) {
		if (lo >= hi) return;
		int mid = lo + (hi-lo)/2;
		s.add(newAppt(mid));
		addRange(lo,mid);
		addRange(mid+1,hi);
	}
	private void addMany(int number) {
		addRange(0,number);
	}

	public void testA() {
		addMany(MAX_LENGTH);
		assertEquals(MAX_LENGTH, s.size());
	}
	
	public void testB() {
		Appointment before = new Appointment(new Period(now.subtract(Duration.DAY), Duration.HOUR),"work");
		Appointment later = new Appointment(new Period(now.add(Duration.DAY), Duration.HOUR),"play");
		s.add(later);
		s.add(before);
		addMany(MAX_LENGTH);
		assertEquals(MAX_LENGTH+2, s.size());
	}
	
	public void testC() {
		addMany(MAX_LENGTH);
		s.clear();
		assertEquals(0, s.size());
	}
	
	public void testD() {
		int width = MAX_LENGTH/SAMPLE;
		NewApptBook[] books = new NewApptBook[width];
		for (int i=0; i < width; ++i) {
			addMany(SAMPLE);
			assertEquals(SAMPLE, s.size());
			books[i] = s;
			s = new NewApptBook();
		}		
	}
	
	public void testE() {
		Appointment before = new Appointment(new Period(now.subtract(Duration.DAY), Duration.HOUR),"work");
		Appointment later = new Appointment(new Period(now.add(Duration.DAY), Duration.HOUR),"play");
		s.add(later);
		s.add(before);
		addMany(MAX_LENGTH);
		for (int i=0 ; i < MAX_LENGTH; ++i) {
			assertTrue(s.contains((Object)newAppt(i)));
		}
		assertEquals(MAX_LENGTH+2, s.size());
	}
	
	public void testF() {
		for (int i=0; i < POWER; ++i) {
			s.add(new Appointment(new Period(now.add(Duration.HOUR), Duration.HOUR.scale(i+1)), "eat"));
		}
		addMany(MAX_LENGTH/POWER);
		assertEquals(MAX_LENGTH/POWER + POWER, s.size());
	}
	
	public void testG() {
		addMany(MAX_LENGTH);
		Iterator<Appointment> it = s.iterator();
		int count = 0;
		while (it.hasNext()) {
			Appointment a = it.next();
			assertEquals(1.0+count, a.getTime().getLength().divide(Duration.SECOND));
			++count;
		}
		assertEquals(MAX_LENGTH, count);
	}
	
	public void testH() {
		addMany(MAX_LENGTH);
		Appointment a = newAppt(0);
		for (int i=0; i < MAX_LENGTH; ++i) {
			assertEquals(a, s.iterator().next());
		}
	}
	
	public void testI() {
		int width = MAX_LENGTH/SAMPLE;
		Iterator<?>[] iterators = new Iterator<?>[width];
		for (int i=0; i < width; ++i) {
			addMany(SAMPLE);
			assertEquals(SAMPLE, s.size());
			iterators[i] = s.iterator();
			s = new NewApptBook();
		}
		addMany(SAMPLE);
		Iterator<Appointment> it = s.iterator();
		for (int i=0; i < SAMPLE; ++i) {
			Appointment appt = it.next();
			assertEquals(1.0+i, appt.getTime().getLength().divide(Duration.SECOND));
			for (int j=0; j < width; ++j) {
				assertEquals(appt, iterators[j].next());
			}
		}
	}
	
	public void testJ() {
		addMany(MAX_LENGTH);
		Iterator<Appointment> it = s.iterator();
		for (int i=0; i < MAX_LENGTH; i+= 2) {
			Appointment appt = it.next();
			assertEquals(1.0+i, appt.getTime().getLength().divide(Duration.SECOND));
			it.next();
			it.remove();
		}
		assertEquals(MAX_LENGTH/2, s.size());
	}
	
	public void testK() {
		addMany(MAX_LENGTH);
		for (int i=0; i < MAX_LENGTH; i+= 2) {
			Object appt = newAppt(i);
			assertTrue(s.remove(appt));
		}
		assertEquals(MAX_LENGTH/2, s.size());
	}
	
	public void testL() {
		// starts the same as testK except with fewer elements
		addMany(MAX_LENGTH/2);
		for (int i=0; i < MAX_LENGTH/2; i+= 2) {
			Object appt = newAppt(i);
			assertTrue(s.remove(appt));
		}
		for (int i=0; i < MAX_LENGTH/2; i+= 2) {
			Object appt = newAppt(i);
			assertFalse(s.remove(appt));
		}
	}

	public void testM() {
		// starts the same as testK except with fewer elements
		addMany(MAX_LENGTH/2);
		for (int i=0; i < MAX_LENGTH/2; i+= 2) {
			Object appt = newAppt(i);
			assertTrue(s.remove(appt));
		}
		for (int i=0; i < MAX_LENGTH/2; i+= 2) {
			Object appt = newAppt(i);
			assertFalse(s.contains(appt));
		}
	}

	public void testN() {
		addMany(MAX_LENGTH/2);
		Collection<? extends Appointment> saved = s;
		s = new NewApptBook();
		s.add(newAppt(MAX_LENGTH/2));
		addRange(MAX_LENGTH/2+1,MAX_LENGTH);
		s.addAll(saved);
		assertEquals(MAX_LENGTH, s.size());
	}
}
