package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * DTO class to represent seats at the concert venue. 
 * 
 * A SeatDTO describes a seat in terms of:
 * _row    the row of the seat.
 * _number the number of the seat.
 *
 */

@Entity
@Table(name = "SEATS")
public class Seat {
	
	@Id
	private String _seatAsString;
	
	@Enumerated(EnumType.STRING)
	private SeatRow _row;
	
	@Enumerated(EnumType.STRING)
	private PriceBand _seatType;
	
	@Column(name = "SeatNumber")
	@Convert(converter = SeatNumberConverter.class)
	private SeatNumber _snumber;
	
	private Boolean _taken;
	
	@Version
	private Long _version;
	
	private Boolean _zBooked;
	
	public Seat() {}
	
	public Seat(SeatRow row, SeatNumber number) {
		_row = row;
		_snumber = number;
		_seatAsString = _row + _snumber.toString();
	}
	
	public String getId() {
		return _seatAsString;
	}
	
	public SeatRow getRow() {
		return _row;
	}
	
	public PriceBand getSeatType() {
		return _seatType;
	}
	
	public SeatNumber getNumber() {
		return _snumber;
	}
	
	public Boolean getTaken() {
		return _taken;
	}
	
	public void setTaken(boolean reserved) {
		_taken = reserved;
	}
	
	public Long getVersion() {
		return _version;
	}
	
	public Boolean getIsBooked() {
		return _zBooked;
	}
	
	public void setBooked(boolean booked) {
		_zBooked = booked;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Seat))
            return false;
        if (obj == this)
            return true;

        Seat rhs = (Seat) obj;
        return new EqualsBuilder().
            append(_row, rhs._row).
            append(_snumber, rhs._snumber).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_row).
	            append(_snumber).
	            hashCode();
	}
	
	@Override
	public String toString() {
		return _row + _snumber.toString();
	}
	
	public SeatDTO toSeatDTO() {
		return new SeatDTO(_row, _snumber);
	}
}
