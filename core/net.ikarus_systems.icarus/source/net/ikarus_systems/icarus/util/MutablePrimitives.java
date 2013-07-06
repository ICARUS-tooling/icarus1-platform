/**
 * 
 */
package net.ikarus_systems.icarus.util;

/**
 * @author Markus Gärtner
 *
 */
public class MutablePrimitives {

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 7 2013-02-27 13:18:56Z mcgaerty $
	 *
	 */
	public static class MutableBoolean {
		private boolean value;

		public MutableBoolean(boolean value) {
			this.value = value;
		}
		
		public MutableBoolean() {
			this(false);
		}

		public boolean getValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 7 2013-02-27 13:18:56Z mcgaerty $
	 *
	 */
	public static class MutableInteger {
		private int value;

		public MutableInteger(int value) {
			this.value = value;
		}
		
		public MutableInteger() {
			this(0);
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 7 2013-02-27 13:18:56Z mcgaerty $
	 *
	 */
	public static class MutableFloat {
		private float value;

		public MutableFloat(float value) {
			this.value = value;
		}
		
		public MutableFloat() {
			this(0F);
		}

		public float getValue() {
			return value;
		}

		public void setValue(float value) {
			this.value = value;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 7 2013-02-27 13:18:56Z mcgaerty $
	 *
	 */
	public static class MutableDouble {
		private double value;

		public MutableDouble(double value) {
			this.value = value;
		}
		
		public MutableDouble() {
			this(0D);
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id: MutablePrimitives.java 7 2013-02-27 13:18:56Z mcgaerty $
	 *
	 */
	public static class MutableLong {
		private long value;

		public MutableLong(long value) {
			this.value = value;
		}
		
		public MutableLong() {
			this(0L);
		}

		public long getValue() {
			return value;
		}

		public void setValue(long value) {
			this.value = value;
		}
	}
}
