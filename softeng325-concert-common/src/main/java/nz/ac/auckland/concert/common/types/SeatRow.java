package nz.ac.auckland.concert.common.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumerated type to model seat rows for the concert venue.
 *
 */
@XmlRootElement
@XmlEnum(String.class)
public enum SeatRow {
	A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R
}
