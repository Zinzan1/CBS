package nz.ac.auckland.concert.common.jaxb;

/**
 * Class with shared configuration data for the client and Web service.
 *
 */
public class Config {
	/**
	 * Name of a cookie exchanged by clients and the Web service.
	 */
	public static final String COOKIE_NAME = "f54e4898-b923-11e8-96f8-529269fb1459";
	
	
	/**
	 * Name of the reservation timer cookie exchanged by clients and the Web service.
	 */
	public static final String RESERVATION = "reservation";
	
	// AWS S3 access credentials for concert images.
    public static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    public static final String AWS_SECRET_ACCESS_KEY = "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";

    // Name of the S3 bucket that stores images.
    public static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // Download directory - a directory named "images" in the user's home
    // directory.
    public static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    
    public static final String USER_DIRECTORY = System
            .getProperty("user.home");
    
    public static final String DOWNLOAD_DIRECTORY = USER_DIRECTORY
            + FILE_SEPARATOR + "images";
}
