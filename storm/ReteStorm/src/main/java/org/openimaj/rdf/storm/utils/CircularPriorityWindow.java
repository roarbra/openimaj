package org.openimaj.rdf.storm.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <T>
 */
public class CircularPriorityWindow <T> implements Queue <T> {

	protected PriorityQueue<TimeWrapped> data;
	protected Map<T, Count> queue;
	protected final int capacity;
	protected final long delay;
	
	/**
	 * @param size
	 * @param delay
	 * @param unit 
	 */
	public CircularPriorityWindow(int size, long delay, TimeUnit unit) {
		this.capacity = size;
		this.delay = TimeUnit.MILLISECONDS.convert(delay, unit);
		this.clear();
	}
	
	/**
	 * @return int
	 */
	public int getCapacity() {
		return capacity;
	}
	
	/**
	 * @return long
	 */
	public long getDelay() {
		return delay;
	}

	@Override
	public void clear() {
		this.data = new PriorityQueue<TimeWrapped>(this.capacity + 1);
		this.queue = new HashMap<T,Count>();
	}
	
	private void prune() {
		Iterator<TimeWrapped> pruner = new Iterator<TimeWrapped>(){
			TimeWrapped last;
			@Override
			public boolean hasNext() {
				return (last = data.peek()).getDelay(TimeUnit.MILLISECONDS) < 0;
			}
			@Override
			public TimeWrapped next() {
				return last;
			}
			@Override
			public void remove() {
				data.remove(last);
				decrement(last.getWrapped());
				last = null;
			}
		};
		
		while (pruner.hasNext())
			pruner.remove();
	}

	@Override
	public boolean contains(Object arg0) {
		return queue.containsKey(arg0) || data.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		boolean contains = true;
		for (Object item : arg0)
			contains &= contains(item);
		return contains;
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	@Override
	public int size() {
		return data.size();
	}
	
	@Override
	public T[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean decrement(T arg0) {
		Count count = queue.get(arg0);
        if (count == null) {
            return false;
        } else {
            count.dec();
            if (count.getCount() == 0) {
                queue.remove(arg0);
            }
        }
        return true;
	}
	
	private boolean increment(T arg0) {
		Count count = queue.get(arg0);
        if (count == null) {
            queue.put(arg0, new Count(1));
        } else {
            count.inc();
        }
        return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object arg0) {
		if (arg0 instanceof CircularPriorityWindow.Wrapped)
			return data.remove(arg0) && decrement(((Wrapped)arg0).getWrapped());
		try {
			T arg = (T) arg0;
			return data.remove(new Wrapped(arg)) && decrement(arg);
		} catch (ClassCastException e) {
			return false;
		}
		
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removed = true;
		for (Object item : arg0)
			removed &= remove(item);
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean removed = true;
		List<TimeWrapped> removals = new ArrayList<TimeWrapped>();
		for (TimeWrapped item : data){
			boolean toRemove = true;
			for (Object keeper : arg0)
				toRemove &= !item.equals(keeper);
			if (toRemove)
				removals.add(item);
		}
		for (TimeWrapped item : removals)
			removed &= remove(item);
		return removed;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection arg0) {
		boolean success = true;
		for (Object item : arg0)
			try {
				success &= add((T)item);
			} catch (ClassCastException e) {
				try {
					success &= add((TimeWrapped)item);
				} catch (ClassCastException ex) {
					success = false;
				}
			}
		return success;
	}

	/**
	 * @param arg0
	 * @return boolean
	 */
	public boolean add(TimeWrapped arg0){
		prune();
		if (arg0 == null) return false;
		if (data.size() == capacity){
			data.remove();
		}
		increment(arg0.getWrapped());
		return data.add(arg0);
	}
	
	@Override
	public boolean add(T arg0) {
		if (arg0 == null) return false;
		return add(new TimeWrapped(arg0,(new Date()).getTime(),this.delay,TimeUnit.MILLISECONDS));
	}
	
	/**
	 * @param arg0
	 * @return boolean
	 */
	public boolean offer(TimeWrapped arg0) {
		try {
			return add(arg0);
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	@Override
	public boolean offer(T arg0) {
		try {
			return add(arg0);
		} catch (IllegalStateException e) {
			return false;
		}
	}

	@Override
	public T element() {
		prune();
		return data.element().getWrapped();
	}

	@Override
	public T peek() {
		try {
			return element();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public T poll() {
		try {
			return remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public T remove() {
		T item = data.remove().getWrapped();
		decrement(item);
		return item;
	}
	
	@Override
	public Iterator <T> iterator() {
		prune();
		final PriorityQueue<TimeWrapped> dataclone = new PriorityQueue<TimeWrapped>(data.size());
		dataclone.addAll(data);
		return new Iterator<T>(){
			TimeWrapped last;
			@Override
			public boolean hasNext() {
				return dataclone.peek() != null;
			}
			@Override
			public T next() {
				return (last = dataclone.poll()).getWrapped();
			}
			@Override
			public void remove() {
				data.remove(last);
				decrement(last.getWrapped());
			}
		};
	}
	
	
	
	
	
	/**
	 * Inner class used to represent a generic wrapper for the generic type T contained by the queue.
	 */
	private class Wrapped {
		
		private T wrapped;
		
		public Wrapped (T toWrap) {
			this.wrapped = toWrap;
		}
		
		public T getWrapped() {
			return this.wrapped;
		}
		
		public int hashCode(){
			return this.wrapped.hashCode();
		}
		
		public boolean equals(Object obj){
			if (obj.getClass().equals(Wrapped.class))
				return this.wrapped.equals(Wrapped.class.cast(obj).getWrapped());
			else if (obj.getClass().equals(this.wrapped.getClass()))
				return this.wrapped.equals(obj);
			else
				return false;
		}
		
	}
	
	/**
	 * Inner class used to represent a timestamp wrapper for the generic type T contained by the queue.
	 */
	private class TimeWrapped extends Wrapped implements Delayed {
		
		private long droptime;
		
		public TimeWrapped (T toWrap, long ts, long delay, TimeUnit delayUnit) {
			super(toWrap);
			droptime = ts + TimeUnit.MILLISECONDS.convert(delay, delayUnit);
		}
		
		public boolean equals(Object obj){
			if (obj.getClass().equals(TimeWrapped.class))
				return getDelay(TimeUnit.MILLISECONDS) == this.getClass().cast(obj).getDelay(TimeUnit.MILLISECONDS)
						&& getWrapped().equals(TimeWrapped.class.cast(obj).getWrapped());
			else 
				return super.equals(obj);
		}

		@Override
		public int compareTo(Delayed arg0) {
			return (int) (arg0.getDelay(TimeUnit.MILLISECONDS) - getDelay(TimeUnit.MILLISECONDS));
		}

		@Override
		public long getDelay(TimeUnit arg0) {
			return arg0.convert(droptime - (new Date()).getTime(),TimeUnit.MILLISECONDS);
		}
		
	}
	
}