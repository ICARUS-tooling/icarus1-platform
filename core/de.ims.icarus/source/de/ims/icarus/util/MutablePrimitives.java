/**
 * 
 */
package de.ims.icarus.util;

/**
 * @author Markus Gärtner
 *
 */
public class MutablePrimitives {

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
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
	 * @version $Id$
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

		public int increment() {
			value++;
			return value;
		}

		public int increment(int delta) {
			value += delta;
			return value;
		}

		public int decrement() {
			value--;
			return value;
		}

		public int decrement(int delta) {
			value -= delta;
			return value;
		}
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
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
	 * @version $Id$
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
	 * @version $Id$
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

		public long increment() {
			value++;
			return value;
		}

		public long increment(long delta) {
			value += delta;
			return value;
		}

		public long decrement() {
			value--;
			return value;
		}

		public long decrement(long delta) {
			value -= delta;
			return value;
		}
	}
}
