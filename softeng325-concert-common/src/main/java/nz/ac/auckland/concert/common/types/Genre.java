package nz.ac.auckland.concert.common.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enumerated type for classifying performers.
 *
 */
@XmlRootElement
@XmlEnum(String.class)
public enum Genre {Pop, HipHop, RhythmAndBlues, Acappella, Metal, Rock}