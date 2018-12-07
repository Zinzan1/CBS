package nz.ac.auckland.concert.service.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import nz.ac.auckland.concert.service.domain.jpa.Seat;

@ApplicationPath("/services")
public class ConcertApplication extends Application{

	private Set<Object> _singletons = new HashSet<Object>();
	private Set<Class<?>> _classes = new HashSet<Class<?>>();

	public ConcertApplication() {
		
		_singletons.add(new ConcertResource());
		_classes.add(PersistenceManager.class);
		
		EntityManager em = null;
		
		try {
			em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			em.createQuery("delete from Booking").executeUpdate();
			em.createQuery("delete from User").executeUpdate();
			em.createQuery("delete from CreditCard").executeUpdate();
			
			List<Seat> seats = em.createQuery("select s from Seat s", Seat.class).setLockMode(LockModeType.PESSIMISTIC_WRITE)
					.setHint("javax.persistence.lock.timeout", 5000 ).getResultList();
			for (Seat seatIn : seats) {
				seatIn.setBooked(false);
				seatIn.setTaken(false);
			}
			
			em.flush();
			em.clear();
			
			em.getTransaction().commit();	
		}
		
		catch(Exception e){
			
		}
		
		finally {
			if(em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	public Set<Object> getSingletons() {
	// Return a Set containing an instance of ParoleeResource that will be
	// used to process all incoming requests on Parolee resources.
		PersistenceManager.instance();
		return _singletons;
	}
	  
	@Override
	public Set<Class<?>> getClasses() {
		return _classes;
	}
}
