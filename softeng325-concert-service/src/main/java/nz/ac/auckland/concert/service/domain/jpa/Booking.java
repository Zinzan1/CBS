package nz.ac.auckland.concert.service.domain.jpa;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.types.PriceBand;

/**
 * Persistence class to represent bookings (confirmed reservations). 
 * 
 * A Booking describes a booking in terms of:
 * _bookingId	   the unique id for a booking
 * 
 * _concertId      the unique identifier for a concert.
 * 
 * _concertTitle   the concert's title.
 * 
 * _dateTime       the concert's scheduled date and time for which the booking 
 *                 applies.
 *                 
 * _seats          the seats that have been booked (represented as a Set of 
 *                 Seat objects).
 *     
 * _userBelongsTo  the User that this booking belongs to
 *                
 * _priceBand      the price band of the booked seats (all seats are within the 
 *                 same price band).
 * 
 *                 
 *
 */

@Entity
public class Booking {
	
	@Id
	@GeneratedValue
	private Long _bookingId;
	
	private Long _concertId;
	
	private String _concertTitle;
	
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime _dateTime;
	
	@OneToMany
	@JoinTable(name = "SEATS_BOOKED", joinColumns = @JoinColumn(name = "BOOKING_ID"), inverseJoinColumns = @JoinColumn(name = "SEAT_ID"))
	private Set<Seat> _seats;
	
	@ManyToOne
	@JoinColumn(name = "BOOKING_OWNER", unique=false)
	private User _userBelongsTo;
	
	@Enumerated(EnumType.STRING)
	private PriceBand _priceBand;

	public Booking() {
	}

	public Booking(Long concertId, String concertTitle,
			LocalDateTime dateTime, Set<Seat> seats, PriceBand priceBand, User owner) {
		_concertId = concertId;
		_concertTitle = concertTitle;
		_dateTime = dateTime;

		_seats = new HashSet<Seat>();
		_seats.addAll(seats);

		_priceBand = priceBand;
		_userBelongsTo = owner;
	}

	public Long getBookingId() {
		return _bookingId;
	}
	
	public Long getConcertId() {
		return _concertId;
	}

	public String getConcertTitle() {
		return _concertTitle;
	}

	public LocalDateTime getDateTime() {
		return _dateTime;
	}

	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}
	
	public User getUserBelongsTo() {
		return _userBelongsTo;
	}

	public PriceBand getPriceBand() {
		return _priceBand;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Seat))
			return false;
		if (obj == this)
			return true;

		Booking rhs = (Booking) obj;
		return new EqualsBuilder().append(_concertId, rhs._concertId)
				.append(_concertTitle, rhs._concertTitle)
				.append(_dateTime, rhs._dateTime)
				.append(_seats, rhs._seats)
				.append(_priceBand, rhs._priceBand).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(_concertId)
				.append(_concertTitle).append(_dateTime).append(_seats)
				.append(_priceBand).hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("concert: ");
		buffer.append(_concertTitle);
		buffer.append(", date/time ");
		buffer.append(_seats.size());
		buffer.append(" ");
		buffer.append(_priceBand);
		buffer.append(" seats.");
		return buffer.toString();
	}
	
	public Set<SeatDTO> toSeatDTOSet() {
		
		Set<SeatDTO> DTOSeatSet = new HashSet<SeatDTO>();
		
		for (Seat s : _seats) {
			DTOSeatSet.add(s.toSeatDTO());
		}
		
		return DTOSeatSet;
	}
	
	public BookingDTO toDTO() {
		return new BookingDTO(_concertId, _concertTitle, _dateTime, toSeatDTOSet(), _priceBand);
	}
}
