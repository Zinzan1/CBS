package nz.ac.auckland.concert.service.services;

import java.awt.Image;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.jaxb.Config;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.jpa.Booking;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.CreditCard;
import nz.ac.auckland.concert.service.domain.jpa.Performer;
import nz.ac.auckland.concert.service.domain.jpa.Seat;
import nz.ac.auckland.concert.service.domain.jpa.User;
import nz.ac.auckland.concert.service.util.TheatreUtility;

@Path("/resources")
public class ConcertResource {

	private static Logger _logger = LoggerFactory
			.getLogger(ConcertResource.class);
	
	private static List<AsyncResponse> _respondees = new ArrayList<AsyncResponse>();
	
	public ConcertResource() {
		
	}

	@GET
	@Path("concerts")
	@Produces("application/xml")
	public Response getConcerts() {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		List<Concert> concerts = entityManager.createQuery("select c from Concert c", Concert.class)
								.getResultList();
		List<ConcertDTO> dtoConcerts = new ArrayList<ConcertDTO>();
		
		for (Concert c : concerts ) {
			dtoConcerts.add(c.toDTO());
		}
		
		GenericEntity<List<ConcertDTO>> entity = 
				new GenericEntity<List<ConcertDTO>>(dtoConcerts) {};
		return Response.ok(entity).build();
	}

	@GET
	@Path("performers")
	@Produces("application/xml")
	public Response getPerformers() {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		List<Performer> performers = entityManager.createQuery("select p from Performer p", Performer.class)
									.getResultList();
		List<PerformerDTO> dtoPerformers = new ArrayList<PerformerDTO>();
		
		for (Performer p : performers ) {
			dtoPerformers.add(p.toDTO());
		}
		
		GenericEntity<List<PerformerDTO>> entity = 
				new GenericEntity<List<PerformerDTO>>(dtoPerformers) {};
		return Response.ok(entity).build();
	}

	@POST
	@Path("users")
	@Produces("application/xml")
	@Consumes("application/xml")
	public Response createUser(UserDTO newUser) {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		User freshUser = new User(newUser);
		List<User> userList = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
				.setParameter("user_username", newUser.getUsername())
				.getResultList();
		
		if (freshUser.hasNullValues()) {
			return Response.status(Status.BAD_REQUEST).entity(Messages.CREATE_USER_WITH_MISSING_FIELDS).build();
		}
		
		else if (userList.size() == 0 ) {
			
			entityManager.clear();
			entityManager.getTransaction().begin();
			entityManager.persist(freshUser);
			entityManager.getTransaction().commit();
			
			User user = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
					.setParameter("user_username", newUser.getUsername())
					.getSingleResult();
			
			NewCookie clientCookie = new NewCookie(Config.COOKIE_NAME, user.getUsername());
			
			return Response.created(URI.create("/users/" + user.getUsername()))
					.entity(user.toDTO())
					.cookie(clientCookie)
					.build();
		}
		
		else if (userList.size() > 0) {
			return Response.status(Status.BAD_REQUEST)
					.entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
					.build();
		}
		
		return null;
	}

	@PUT
	@Path("users/{username}")
	@Produces("application/xml")
	@Consumes("application/xml")
	public Response authenticateUser(UserDTO user, @PathParam("username")String idname) {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();

		User freshUser = new User(user);
		List<User> userList = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
				.setParameter("user_username", user.getUsername())
				.getResultList();
		
		if (freshUser.getPassword() == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS)
					.build();
		}
		
		else if (userList.size() == 0 ) {
			return Response.status(Status.BAD_REQUEST)
					.entity(Messages.AUTHENTICATE_NON_EXISTENT_USER)
					.build();
		}
		
		else if (userList.size() == 1) {
			User dbuser = (User) entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
					.setParameter("user_username", user.getUsername())
					.getSingleResult();
			UserDTO returneduserdto = dbuser.toDTO();
			
			if (!returneduserdto.getPassword().equals(user.getPassword())) {
				return Response.status(Status.BAD_REQUEST)
						.entity(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD)
						.build();
			}
			
			else {
				NewCookie clientCookie = new NewCookie(Config.COOKIE_NAME, user.getUsername());
				return Response.ok(returneduserdto)
						.cookie(clientCookie)
						.build();
			}
		}
		return null;
	}

	@PUT
	@Path("performers/image")
	@Produces("application/xml")
	@Consumes("application/xml")
	public Response getImageForPerformer(PerformerDTO performer) {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();

		List<Performer> userList = entityManager.createQuery("select p from Performer p WHERE p._id=:performer_id", Performer.class)
				.setParameter("performer_id", performer.getId())
				.getResultList();
		
		if (userList.size() == 0 ) {
			
			return Response.status(Status.BAD_REQUEST)
					.entity(Messages.NO_IMAGE_FOR_PERFORMER)
					.build();
		}
		
		else if (userList.size() == 1) {
			return Response.ok(userList.get(0).getImageName())
					.build();
		}
		return null;
	}

	@PUT
	@Path("users/reservation")
	@Produces("application/xml")
	@Consumes("application/xml")
	public Response reserveSeats(ReservationRequestDTO reservationRequest, @CookieParam(value = Config.COOKIE_NAME) Cookie clientToken) {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		if (clientToken == null) {
			return Response.status(Status.UNAUTHORIZED).entity(Messages.UNAUTHENTICATED_REQUEST).build();
		}
		
		
		else if(reservationRequest.getConcertId() == null || reservationRequest.getDate() == null || reservationRequest.getNumberOfSeats() < 1 || reservationRequest.getSeatType() == null) {
			return Response.status(Status.BAD_REQUEST).entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS).build();
		}
		
		
		else {
			List<User> userList = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
									.setParameter("user_username", clientToken.getValue())
									.getResultList();
			
			
			
			if (userList.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity(Messages.BAD_AUTHENTICATON_TOKEN).build();
			}
			
			
			else if (userList.size() == 1 ) {
				List<Concert> concerts = entityManager.createQuery("select c from Concert c WHERE c._id=:supplied_id", Concert.class)
						.setParameter("supplied_id", reservationRequest.getConcertId())
						.getResultList();
				
				
				if (concerts.size() == 0) {
					return Response.status(Status.BAD_REQUEST).entity("Concert does not exst").build();
				}
				
				
				else if (concerts.size() == 1) {
					boolean hasDate = false;
					Concert c = concerts.get(0);
					
					for(LocalDateTime ldc : c.getDates() )	{
						if(ldc.equals(reservationRequest.getDate())) {
							hasDate = true;
						}
					}
					
					
					if (!hasDate) {
						return Response.status(Status.BAD_REQUEST).entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE).build();
					}
					
					
					else {
						entityManager.clear();
						EntityTransaction tx = entityManager.getTransaction();
						tx.begin();
						
						List<Seat> seats = entityManager.createQuery("select s from Seat s WHERE s._seatType=:_seatrow AND s._taken=true OR s._zBooked=true", Seat.class)
								.setParameter("_seatrow", reservationRequest.getSeatType())
								.setLockMode(LockModeType.PESSIMISTIC_WRITE)
								.setHint("javax.persistence.lock.timeout", 5000 )
								.getResultList();				
						
						Set<Seat> seatSet = new HashSet<Seat>(seats);
						Set<Seat> newBookedSeats = TheatreUtility.findAvailableSeats(reservationRequest.getNumberOfSeats(), reservationRequest.getSeatType(), seatSet);
						
						
						if (newBookedSeats.isEmpty()) {
							tx.commit();
							return Response.status(Status.BAD_REQUEST).entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION).build();
						}
						
						
						else {
//						System.out.println(newBookedSeats.size());
						
						Set<SeatDTO> newBookedSeatDTOSet = new HashSet<SeatDTO>();
						
						for(Seat bookedSeat : newBookedSeats) {
							
//							System.out.println(bookedSeat);
							
							newBookedSeatDTOSet.add(bookedSeat.toSeatDTO());
							
							Seat seatToChange = entityManager.createQuery("select s from Seat s WHERE s._seatAsString=:SeatID", Seat.class)
									.setParameter("SeatID", bookedSeat.toString())
									.setLockMode(LockModeType.PESSIMISTIC_WRITE)
									.setHint("javax.persistence.lock.timeout", 5000 )
									.getSingleResult();
							
							seatToChange.setTaken(true);
//							entityManager.merge(seatToChange);
						}
						
						tx.commit();
						
						NewCookie resCookie = new NewCookie(Config.RESERVATION, System.currentTimeMillis()+"");
						
						Long ResevationId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
						
						ReservationDTO resDTO = new ReservationDTO(ResevationId, reservationRequest, newBookedSeatDTOSet);
						
						Thread thread = new Thread(new Runnable() {
							public void run() {
								try {
									Thread.sleep(5000);
									
									EntityManager em = PersistenceManager.instance().createEntityManager();
									
									EntityTransaction tx = em.getTransaction();
									tx.begin();
									
									for(Seat seatIn : newBookedSeats) {
										
										Seat seatToChangeBack = em.createQuery("select s from Seat s WHERE s._seatAsString=:SeatID", Seat.class)
												.setParameter("SeatID", seatIn.toString())
												.setLockMode( LockModeType.PESSIMISTIC_WRITE)
												.setHint("javax.persistence.lock.timeout", 5000 )
												.getSingleResult();
										
										seatToChangeBack.setTaken(false);
									}
									
									tx.commit();
									
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
						thread.start();
						
						return Response.ok(resDTO).cookie(resCookie).build();
						}
					}
				}
			}
		}
		return null;
	}

	@PUT
	@Path("users/reservation/confirm")
	@Produces("application/xml")
	@Consumes("application/xml")
	public Response confirmReservation(ReservationDTO reservation, @CookieParam(value = Config.COOKIE_NAME) Cookie clientToken, @CookieParam(value = Config.RESERVATION) Cookie reservationToken) {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		if (clientToken == null) {
			return Response.status(Status.UNAUTHORIZED).entity(Messages.UNAUTHENTICATED_REQUEST).build();
		}
		
		else if (reservationToken == null) {
			return Response.status(Status.BAD_REQUEST).entity(Messages.EXPIRED_RESERVATION).build();
		}
		
		else {
			
			if (System.currentTimeMillis() - Long.valueOf(reservationToken.getValue()).longValue() > 5000) {
				return Response.status(Status.BAD_REQUEST).entity(Messages.EXPIRED_RESERVATION).build();
			}
			
			List<User> userList = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
									.setParameter("user_username", clientToken.getValue())
									.getResultList();
			
			if (userList.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity(Messages.BAD_AUTHENTICATON_TOKEN).build();
			}
			
			else if (userList.size() == 1 ) {
				
				User authedUser = userList.get(0);
				CreditCard userCC = authedUser.getCreditCard();
				
				if (userCC == null) {
					return Response.status(Status.BAD_REQUEST).entity(Messages.CREDIT_CARD_NOT_REGISTERED).build();
				}
				
				else {
					
					Set<SeatDTO> DTOSeats = reservation.getSeats();
					
					EntityTransaction tx = entityManager.getTransaction();
					tx.begin();
					
					Concert requestedConcert = entityManager.createQuery("select c from Concert c WHERE c._id=:idFromReq", Concert.class)
					.setParameter("idFromReq", reservation.getReservationRequest().getConcertId())
					.getSingleResult();
					
					Set<Seat> jpaSeats = new HashSet<Seat>();
					
					for(SeatDTO dtoSeat : DTOSeats) {
						Seat seatToChange = entityManager.createQuery("select s from Seat s WHERE s._seatAsString=:SeatID", Seat.class)
								.setParameter("SeatID", dtoSeat.toString())
								.setLockMode(LockModeType.PESSIMISTIC_WRITE)
								.setHint("javax.persistence.lock.timeout", 5000 )
								.getSingleResult();
						jpaSeats.add(seatToChange);
						
						seatToChange.setBooked(true);
						seatToChange.setTaken(true);
					}
					
					ReservationDTO resDTO = reservation;
					ReservationRequestDTO resReqDTO = reservation.getReservationRequest();
					
					Booking booking = new Booking(resReqDTO.getConcertId(),requestedConcert.getTitle(),resReqDTO.getDate(),jpaSeats,resReqDTO.getSeatType(), authedUser);
					entityManager.persist(booking);
					authedUser.addBooking(booking);
					
					tx.commit();
					
					return Response.created(URI.create("users/bookings"))
							.build();
				}
			}
		}
			return null;
	}

	@PUT
	@Path("creditcard")
	@Produces("application/xml")
	@Consumes("application/xml")
	public Response registerCreditCard(CreditCardDTO creditCard, @CookieParam(value = Config.COOKIE_NAME) Cookie clientToken) {
		
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		if (clientToken == null) {
			return Response.status(Status.UNAUTHORIZED).entity(Messages.UNAUTHENTICATED_REQUEST).build();
		}
		
		else {
			List<User> userList = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
									.setParameter("user_username", clientToken.getValue())
									.getResultList();
			
			if (userList.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity(Messages.BAD_AUTHENTICATON_TOKEN).build();
			}
			
			else if (userList.size() == 1 ) {
				CreditCard cc = new CreditCard(creditCard, userList.get(0));

				entityManager.clear();
				
				entityManager.getTransaction().begin();
				
				entityManager.persist(cc);
				userList.get(0).setCreditCard(cc);
				
				entityManager.merge(userList.get(0));
				
				entityManager.getTransaction().commit();
				
				// Doesn't return URI about how to retrieve credit cards as this is not required
				return Response.status(Status.CREATED).build();
			}
		}
			return null;
	}

	@GET
	@Path("users/bookings")
	@Produces("application/xml")
	public Response getBookings(@CookieParam(value = Config.COOKIE_NAME) Cookie clientToken) {
		EntityManager entityManager = PersistenceManager.instance().createEntityManager();
		
		if (clientToken == null) {
			return Response.status(Status.UNAUTHORIZED).entity(Messages.UNAUTHENTICATED_REQUEST).build();
		}
		
		else {
			List<User> userList = entityManager.createQuery("select u from User u WHERE u._username=:user_username", User.class)
									.setParameter("user_username", clientToken.getValue())
									.getResultList();
			
			if (userList.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity(Messages.BAD_AUTHENTICATON_TOKEN).build();
			}
			
			else if (userList.size() == 1 ) {
				List<Booking> bookingList = entityManager.createQuery("select b from Booking b WHERE b._userBelongsTo._username=:authUser", Booking.class)
						.setParameter("authUser", clientToken.getValue())
						.getResultList();
				
				List<BookingDTO> bookingDTOList = new ArrayList<BookingDTO>();
				
				for(Booking booking : bookingList) {
					bookingDTOList.add(booking.toDTO());
				}
				
				GenericEntity<List<BookingDTO>> entity = 
						new GenericEntity<List<BookingDTO>>(bookingDTOList) {};
						
				return Response.ok(entity).build();
			}
		}
			return null;
	}
	
	@GET
	@Path("subscribe")
	public void subscribe(@Suspended AsyncResponse response) {
		_respondees.add(response);
	}
	
	@POST
	@Path("notify")
	@Consumes({"application/xml","text/plain"})
	public void sendNotification(String notificationMessage) {
		for(AsyncResponse response : _respondees) {
			response.resume(notificationMessage);		
		}
	}
}
