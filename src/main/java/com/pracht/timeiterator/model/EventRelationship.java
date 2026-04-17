package com.pracht.timeiterator.model;

/**
 * Defines the relationship between consecutive events in a sequence.
 * These relationships determine how the next starting point is calculated
 * based on the current point and the event duration.
 */
public enum EventRelationship {
	/** Next event starts after the current event finishes. */
	FINISH_TO_START,
	/** Next event finishes when the current event finishes (alignment on end). */
	FINISH_TO_FINISH,
	/** Next event finishes when the current event starts. */
	START_TO_FINISH,
	/** Next event starts when the current event starts (default alignment on start). */
	START_TO_START
}
