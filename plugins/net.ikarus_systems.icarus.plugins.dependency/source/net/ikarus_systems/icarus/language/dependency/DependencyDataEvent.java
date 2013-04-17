package net.ikarus_systems.icarus.language.dependency;

import net.ikarus_systems.icarus.language.MutableSentenceData;
import net.ikarus_systems.icarus.language.SentenceDataEvent;

public class DependencyDataEvent extends SentenceDataEvent {

	int startIndex = -1, endIndex = -1;
	int fieldMask = 0;

	public DependencyDataEvent(MutableSentenceData source) {
		this(source, -1, -1);
	}

	public DependencyDataEvent(MutableSentenceData source, int startIndex,
			int endIndex) {
		super(source);
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		fieldMask = 0;
	}

	public String toString() {
		return String.format("%d: %d to %d", fieldMask, startIndex, endIndex);
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

	public int getFieldMask() {
		return fieldMask;
	}

	void set(int type, int startIndex, int endIndex, int fieldMask) {
		this.type = type;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.fieldMask = fieldMask;
	}

	void addField(int field) {
		fieldMask = fieldMask | field;
	}

	public boolean isIndexChanged() {
		return (fieldMask & DependencyConstants.DATA_FIELD_INDEX) != 0;
	}

	public boolean isFormChanged() {
		return (fieldMask & DependencyConstants.DATA_FIELD_FORM) != 0;
	}

	public boolean isPosChanged() {
		return (fieldMask & DependencyConstants.DATA_FIELD_POS) != 0;
	}

	public boolean isRelationChanged() {
		return (fieldMask & DependencyConstants.DATA_FIELD_RELATION) != 0;
	}

	public boolean isHeadChanged() {
		return (fieldMask & DependencyConstants.DATA_FIELD_HEAD) != 0;
	}

	public boolean isAllChanged() {
		return fieldMask >= DependencyConstants.DATA_FIELD_ALL - 1;
	}
}
