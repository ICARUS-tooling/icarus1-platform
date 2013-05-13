package net.ikarus_systems.icarus.language.dependency;

import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.language.SentenceDataEvent;

public class DependencyDataEvent extends SentenceDataEvent {

	int startIndex = -1, endIndex = -1;

	public DependencyDataEvent(MutableSentenceData source) {
		this(source, -1, -1);
	}

	public DependencyDataEvent(MutableSentenceData source, int startIndex,
			int endIndex) {
		super(source);
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	public DependencyDataEvent(MutableSentenceData source, int type, int startIndex,
			int endIndex) {
		super(source);
		setType(type);
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	@Override
	public String toString() {
		return String.format("%d: %d to %d", getType(), startIndex, endIndex); //$NON-NLS-1$
	}

	@Override
	public MutableDependencyData getSource() {
		return (MutableDependencyData) source;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	void set(int type, int startIndex, int endIndex) {
		setType(type);
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
}
