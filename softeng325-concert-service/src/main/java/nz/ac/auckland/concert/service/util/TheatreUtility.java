package nz.ac.auckland.concert.service.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.Seat;
import nz.ac.auckland.concert.utility.TheatreLayout;

/**
 * Utility class with a search method that identifies seats that are available
 * to reserve.
 *
 */
public class TheatreUtility {

	/**
	 * Attempts to find a specified number of seats, within a given priceband,
	 * that aren't currently booked.
	 * 
	 * @param numberOfSeats
	 *            the number of seats required.
	 * @param price
	 *            the priceband to search.
	 * @param bookedSeats
	 *            the set of seats that is currently booked.
	 * 
	 * @return a set of seats that are available to book. When successful the
	 *         set is non-empty and contains numberOfSeats seats that are within
	 *         the specified priceband. When not successful (i.e. when there are
	 *         not enough seats available in the required priceband, this method
	 *         returns the empty set.
	 * 
	 */
	public static Set<Seat> findAvailableSeats(int numberOfSeats,
			PriceBand price, Set<Seat> bookedSeats) {
		List<Seat> openSeats = getAllAvailableSeatsByPrice(price,
				bookedSeats);

		if (openSeats.size() < numberOfSeats) {
			return new HashSet<Seat>();
		}

		return getSpecificAvailableSeats(
				new Random().nextInt(openSeats.size()), numberOfSeats,
				openSeats);
	}


	protected static Set<Seat> getSpecificAvailableSeats(int startIndex,
			int numberOfSeats, List<Seat> openSeats) {
		Set<Seat> availableSeats = new HashSet<Seat>();
		while (numberOfSeats > 0) {
			if (startIndex > openSeats.size() - 1) {
				startIndex = 0;
			}
			availableSeats.add(openSeats.get(startIndex));
			startIndex++;
			numberOfSeats--;
		}
		return availableSeats;

	}

	protected static List<Seat> getAllAvailableSeatsByPrice(PriceBand price,
			Set<Seat> bookedSeats) {
		List<Seat> openSeats = new ArrayList<Seat>();
		Set<SeatRow> rowsInPriceBand = TheatreLayout
				.getRowsForPriceBand(price);

		for (SeatRow row : rowsInPriceBand) {
			for (int i = 1; i <= TheatreLayout.getNumberOfSeatsForRow(row); i++) {
				Seat seat = new Seat(row, new SeatNumber(i));
				if (!bookedSeats.contains(seat)) {
					openSeats.add(seat);
				}
			}
		}
		return openSeats;
	}
}
