package nz.ac.auckland.concert.client.service;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.jaxb.Config;
import nz.ac.auckland.concert.common.message.Messages;
/**
 * Concrete implementation of the ConcertService interface for the Concert service. 
 *
 */
public class DefaultService implements ConcertService {
	
	private static Logger _logger = LoggerFactory
			.getLogger(DefaultService.class);
	
	private static String WEB_SERVICE_BASE_URI = "http://localhost:10000/services/resources";
	
	private static Client _client;
	
	private NewCookie _loggedInUser;
	
	private NewCookie _reservationSession;
	
	public DefaultService() {
		_client = ClientBuilder.newClient();
	}
	
	@Override
	public Set<ConcertDTO> getConcerts() throws ServiceException {
		
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/concerts").request()
				.accept(MediaType.APPLICATION_XML);

		List<ConcertDTO> concerts = builder.get(new GenericType<List<ConcertDTO>>() {});
		return new HashSet<ConcertDTO>(concerts);
	}

	@Override
	public Set<PerformerDTO> getPerformers() throws ServiceException {
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/performers").request()
				.accept(MediaType.APPLICATION_XML);

		List<PerformerDTO> performers = builder.get(new GenericType<List<PerformerDTO>>() {});
		return new HashSet<PerformerDTO>(performers);
	}

	@Override
	public UserDTO createUser(UserDTO newUser) throws ServiceException {
		
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/users").request().accept(MediaType.APPLICATION_XML);
		Response response = builder.post(Entity.xml(newUser));
		
		if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			String serverErrorMsg = response.readEntity(String.class);
			response.close();
			throw new ServiceException(serverErrorMsg);
		}
		UserDTO returnedUser = response.readEntity(UserDTO.class);
		processCookieFromResponse(response);
		response.close();
		
		return returnedUser;
	}

	@Override
	public UserDTO authenticateUser(UserDTO user) throws ServiceException {
		
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/users/" + user.getUsername()).request().accept(MediaType.APPLICATION_XML);
		Response response = builder.put(Entity.xml(user));
		
		if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			String serverErrorMsg = response.readEntity(String.class);
			response.close();
			throw new ServiceException(serverErrorMsg);
		}
		
		UserDTO returnedUser = response.readEntity(UserDTO.class);
		processCookieFromResponse(response);
		response.close();
		
		return returnedUser;
	}

	@Override
	public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {

	        // Create download directory if it doesn't already exist.
	        File downloadDirectory = new File(Config.DOWNLOAD_DIRECTORY);
	        downloadDirectory.mkdir();

	        // Create an AmazonS3 object that represents a connection with the
	        // remote S3 service.
	        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
	                Config.AWS_ACCESS_KEY_ID, Config.AWS_SECRET_ACCESS_KEY);
	        AmazonS3 s3 = AmazonS3ClientBuilder
	                .standard()
	                .withRegion(Regions.AP_SOUTHEAST_2)
	                .withCredentials(
	                        new AWSStaticCredentialsProvider(awsCredentials))
	                .build();

	        Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/performers/image").request().accept(MediaType.APPLICATION_XML);
			Response response = builder.put(Entity.xml(performer));
			
			if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
				String serverErrorMsg = response.readEntity(String.class);
				response.close();
				throw new ServiceException(serverErrorMsg);
			}
			
			String returnedImageName = response.readEntity(String.class);
			response.close();
			BufferedImage img = null;
			try {
				File f = new File(downloadDirectory, returnedImageName);
				
				GetObjectRequest req = new GetObjectRequest(Config.AWS_BUCKET, returnedImageName);
	            s3.getObject(req, f);
	            
	            img = ImageIO.read(f);
			} catch (AmazonServiceException e) {
			    throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
			} catch (IOException e) {
				throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
			}
			return img;
	}

	@Override
	public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
		
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/users/reservation").request().accept(MediaType.APPLICATION_XML);
		addCookieToInvocation(builder, _loggedInUser);
		Response response = builder.put(Entity.xml(reservationRequest));
		
		if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			String serverErrorMsg = response.readEntity(String.class);
			response.close();
			throw new ServiceException(serverErrorMsg);
		}
		
		ReservationDTO returnedRes = response.readEntity(ReservationDTO.class);
		processCookieFromResponse(response);
		response.close();
		
		return returnedRes;
	}

	@Override
	public void confirmReservation(ReservationDTO reservation) throws ServiceException {
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/users/reservation/confirm").request().accept(MediaType.APPLICATION_XML);
		addCookieToInvocation(builder, _loggedInUser);
		addCookieToInvocation(builder, _reservationSession);
		Response response = builder.put(Entity.xml(reservation));
		
		if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			String serverErrorMsg = response.readEntity(String.class);
			response.close();
			throw new ServiceException(serverErrorMsg);
		}
		
//		ReservationDTO returnedRes = response.readEntity(ReservationDTO.class);
		processCookieFromResponse(response);
		response.close();
	}

	@Override
	public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
		
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/creditcard").request().accept(MediaType.APPLICATION_XML);
		addCookieToInvocation(builder, _loggedInUser);
		Response response = builder.put(Entity.xml(creditCard));
		
		if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			String serverErrorMsg = response.readEntity(String.class);
			response.close();
			throw new ServiceException(serverErrorMsg);
		}
		
		response.close();
		
	}

	@Override
	public Set<BookingDTO> getBookings() throws ServiceException {
		
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/users/bookings").request().accept(MediaType.APPLICATION_XML);
		addCookieToInvocation(builder, _loggedInUser);
		Response response = builder.get();
		response.close();
		
		if(response.getStatus() == Status.BAD_REQUEST.getStatusCode() || response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
			String serverErrorMsg = response.readEntity(String.class);
			response.close();
			throw new ServiceException(serverErrorMsg);
		}
		
		List<BookingDTO> bookings = builder.get(new GenericType<List<BookingDTO>>() {});
		return new HashSet<BookingDTO>(bookings);
	}
	
	public void subscribe() {
		Builder builder = _client.target(WEB_SERVICE_BASE_URI + "/subscribe").request();
		addCookieToInvocation(builder, _loggedInUser);
		builder.async().get(new InvocationCallback<String>() {
			@Override
			public void completed(String message) {
				System.out.println(message);
			}
	
			@Override
			public void failed(Throwable arg0) {	
			}
		});
	}
	
	
	
	
	
	
	
	private void addCookieToInvocation(Builder builder, Cookie cookieToAdd) {
		if(cookieToAdd != null) {
			builder.cookie(cookieToAdd);
		}
	}
	
	private void processCookieFromResponse(Response response) {
		Map<String, NewCookie> cookies = response.getCookies();
		
		if(cookies.containsKey(Config.COOKIE_NAME)) {
			NewCookie retrievedCookie = cookies.get(Config.COOKIE_NAME);
//			System.out.println(retrievedCookie.getName() + "\n" + retrievedCookie.getValue());
			_loggedInUser = retrievedCookie;
		}
		
		if(cookies.containsKey(Config.RESERVATION)) {
			NewCookie resCookie = cookies.get(Config.RESERVATION);
//			System.out.println(resCookie.getName() + "\n" + resCookie.getValue());
			_reservationSession = resCookie;
		}
	}

}
