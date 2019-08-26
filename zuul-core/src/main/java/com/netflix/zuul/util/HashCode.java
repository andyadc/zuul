package com.netflix.zuul.util;

/**
 * andy.an
 */
public class HashCode {
	/*
	 * Based on the algorithm in "Effective Java"
	 */

	// ========================================
	// Static vars: public, protected, then private
	// ========================================

	private static final int SEED = 17;
	private static final long SCALE = 37;

	// ========================================
	// Instance vars: public, protected, then private
	// ========================================

	private int mVal;

	// ========================================
	// Constructors
	// ========================================

	/**
	 * Create a new HashCode object
	 */
	public HashCode() {
		mVal = SEED;
	}

	// ========================================
	// Methods, grouped by functionality, *not* scope
	// ========================================

	/**
	 * Utility function to make it easy to compare two, possibly null, objects.
	 *
	 * @param o1 first object
	 * @param o2 second object
	 * @return true iff either both objects are null, or
	 * neither are null and they are equal.
	 */
	public static boolean equalObjects(Object o1, Object o2) {
		if (o1 == null) {
			return (o2 == null);
		} else if (o2 == null) {
			return false;
		} else {
			return o1.equals(o2);
		}
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>obj</i>.
	 *
	 * @param obj value being added
	 * @return the new hash code.
	 */
	public int addValue(Object obj) {
		return foldIn((obj != null) ? obj.hashCode() : 0);
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>b</i>.
	 *
	 * @param b value being added
	 * @return the new hash code.
	 */
	public int addValue(boolean b) {
		return foldIn(b ? 0 : 1);
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>i</i>.
	 *
	 * @param i value being added
	 * @return the new hash code.
	 */
	public int addValue(byte i) {
		return foldIn(i);
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>i</i>.
	 *
	 * @param i value being added
	 * @return the new hash code.
	 */
	public int addValue(char i) {
		return foldIn(i);
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>i</i>.
	 *
	 * @param i value being added
	 * @return the new hash code.
	 */
	public int addValue(short i) {
		return foldIn(i);
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>i</i>.
	 *
	 * @param i value being added
	 * @return the new hash code.
	 */
	public int addValue(int i) {
		return foldIn(i);
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>f</i>.
	 *
	 * @param f value being added
	 * @return the new hash code.
	 */
	public int addValue(float f) {
		return foldIn(Float.floatToIntBits(f));
	}

	// --------------------
	// Arrays

	/**
	 * Augment the current computed hash code with the
	 * value <i>f</i>.
	 *
	 * @param f value being added
	 * @return the new hash code.
	 */
	public int addValue(double f) {
		return foldIn(Double.doubleToLongBits(f));
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(Object[] array) {
		int val = hashCode();
		for (Object obj : array) {
			val = addValue(obj);
		}
		return val;
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(boolean[] array) {
		int val = hashCode();
		for (boolean b : array) {
			val = addValue(b);
		}
		return val;
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>i</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(byte[] array) {
		int val = hashCode();
		for (byte i : array) {
			val = addValue(i);
		}
		return val;
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(char[] array) {
		int val = hashCode();
		for (char i : array) {
			val = addValue(i);
		}
		return val;
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(short[] array) {
		int val = hashCode();
		for (short i : array) {
			val = addValue(i);
		}
		return val;
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(int[] array) {
		int val = hashCode();
		for (int i : array) {
			val = addValue(i);
		}
		return val;
	}

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(float[] array) {
		int val = hashCode();
		for (float f : array) {
			val = addValue(f);
		}
		return val;
	}

	// --------------------
	// Utility methods

	/**
	 * Augment the current computed hash code with the
	 * value <i>array</i>.
	 *
	 * @param array value being added
	 * @return the new hash code.
	 */
	public int addValue(double[] array) {
		int val = hashCode();
		for (double f : array) {
			val = addValue(f);
		}
		return val;
	}

	// --------------------
	// Internals

	private int foldIn(int c) {
		return setVal((SCALE * mVal) + c);
	}

	private int foldIn(long c) {
		return setVal((SCALE * mVal) + c);
	}

	private int setVal(long l) {
		mVal = (int) (l ^ (l >>> 32));
		return mVal;
	}

	// ----------------------------------------
	// Generic object protocol

	/**
	 * Get the currently computed hash code value.
	 */
	@Override
	public int hashCode() {
		return mVal;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		HashCode h = (HashCode) obj;
		return (h.hashCode() == hashCode());
	}

	@Override
	public String toString() {
		return "{HashCode " + mVal + "}";
	}
}
