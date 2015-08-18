package alex.imhere.util;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class TemporarySet<T> extends Observable {
	// TODO: 18.08.2015 make sure it's thread-safe implementation
	protected SortedSet<TemporaryElement<T>> sortedElementsSet = new TreeSet<>();
	protected List<T> list = new ArrayList<>();

	protected final Timer timer = new Timer();
	protected TimerTask timerTask = null;
	protected TemporaryElement<T> nextElementToDie = null;

	public boolean add(T object, LocalDateTime deathTime) {
		TemporaryElement<T> element = new TemporaryElement<>(object, deathTime);
		return _add(element);
	}

	public boolean remove(T object) {
		TemporaryElement<T> element = new TemporaryElement<>(object);
		return _remove(element);
	}

	public void clear() {
		_clear();
	}

	public final List<T> asReadonlyList() {
		return Collections.unmodifiableList(list);
	}

	private synchronized void _clear() {
		cancelNextDeath();
		list.clear();
		sortedElementsSet.clear();

		notifyCollectionChanged();
	}


	private synchronized boolean _add(TemporaryElement<T> isertingElement) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasAdded = sortedElementsSet.add(isertingElement);

		if (wasAdded) {
			list.add(0, isertingElement.object);

			if (nextElementToDie != null &&
					nextElementToDie.deathTime.isAfter(isertingElement.deathTime)) {
				cancelNextDeath();
			}

			if (nextElementToDie == null) {
				openNextDeath();
			}

			notifyCollectionChanged();
		}

		return wasAdded;
	}

	private synchronized boolean _remove(TemporaryElement<T> deletingElement) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasRemoved = sortedElementsSet.remove(deletingElement);

		if (wasRemoved) {
			list.remove(deletingElement.object);

			if (nextElementToDie.equals(deletingElement)) {
				cancelNextDeath();

				if (sortedElementsSet.size() != 0) {
					_add( sortedElementsSet.first() );
				}

			}

			notifyCollectionChanged();
		}

		return wasRemoved;
	}

	private synchronized void openNextDeath() {
		nextElementToDie = sortedElementsSet.first();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				killNextElement();
			}
		};

		long deathTimeMillis = nextElementToDie.deathTime.toDateTime().getMillis(),
				nowMillis = (new LocalDateTime()).toDateTime().getMillis();
		long delay = Math.max(0, deathTimeMillis - nowMillis);

		timer.schedule(timerTask, delay);
	}

	private synchronized void cancelNextDeath() {
		if (timerTask != null) {
			timerTask.cancel();
		}
		timer.purge();
		nextElementToDie = null;
		timerTask = null;
	}

	private synchronized void killNextElement() {
		_remove(nextElementToDie);
	}

	private void notifyCollectionChanged() {
		setChanged();
		notifyObservers();
	}
}
