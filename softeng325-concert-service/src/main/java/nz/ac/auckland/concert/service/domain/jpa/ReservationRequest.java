package nz.ac.auckland.concert.service.domain.jpa;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.jaxb.LocalDateTimeAdapter;
import nz.ac.auckland.concert.common.types.PriceBand;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Unused class to represent reservation requests. 
 * 
 * A ReservationRequest describes a request to reserve seats in terms of:
 * _numberOfSeats the number of seats to try and reserve.
 * _seatType      the priceband (A, B or C) in which to reserve the seats.
 * _concertId     the identity of the concert for which to reserve seats.
 * _date          the date/time of the concert for which seats are to be 
 *                reserved.
 *
 */

public class ReservationRequest {
	
	private int _numberOfSeats;
	
	private PriceBand _seatType;
	
	private Long _concertId;
	
	private LocalDateTime _date;
	
	public ReservationRequest() {}
	
	public ReservationRequest(int numberOfSeats, PriceBand seatType, Long concertId, LocalDateTime date) {
		_numberOfSeats = numberOfSeats;
		_seatType = seatType;
		_concertId = concertId;
		_date = date;
	}
	
	public int getNumberOfSeats() {
		return _numberOfSeats;
	}
	
	public PriceBand getSeatType() {
		return _seatType;
	}
	
	public Long getConcertId() {
		return _concertId;
	}
	
	public LocalDateTime getDate() {
		return _date;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ReservationRequest))
            return false;
        if (obj == this)
            return true;

        ReservationRequest rhs = (ReservationRequest) obj;
        return new EqualsBuilder().
            append(_numberOfSeats, rhs._numberOfSeats).
            append(_seatType, rhs._seatType).
            append(_concertId, rhs._concertId).
            append(_date, rhs._date).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_numberOfSeats).
	            append(_seatType).
	            append(_concertId).
	            append(_date).
	            hashCode();
	}
	
	public ReservationRequestDTO toReservationReqDTO() {
		return new ReservationRequestDTO(_numberOfSeats, _seatType, _concertId, _date);
	}
}
