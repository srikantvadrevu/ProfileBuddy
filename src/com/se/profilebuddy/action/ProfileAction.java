package com.se.profilebuddy.action;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.util.Log;

import com.se.profilebuddy.database.ManageLocationDB;
import com.se.profilebuddy.object.ProfileLocation;

/**
 * Supporting class for Location Service. Provides methods to fetch, check and
 * manage location profiles.
 */
public class ProfileAction
{

	private static final String TAG = ProfileAction.class.getSimpleName();

	// -- instance variables
	private static volatile ProfileAction instance = null;
	private List<ProfileLocation> locationList;
	private ManageLocationDB locationDB;
	private Location currentLocation;
	private AudioManager audioManager;

	public void setCurrentLocation(Location currentLocation)
	{
		this.currentLocation = currentLocation;
	}

	private ProfileAction(Context context)
	{
		this.locationDB = new ManageLocationDB(context);
		this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * Singleton object implementation
	 * 
	 * @param context
	 *            - {@link Context}
	 * @return singleton instance of {@link ProfileLocation}
	 */
	public static ProfileAction getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new ProfileAction(context);
		}
		return instance;
	}

	/**
	 * This method checks if the current location of the user is inside the
	 * radius of the predefined location.
	 * 
	 * @param profileLocation
	 *            - {@link ProfileLocation}
	 * @return status of the check.
	 */
	private boolean insideFence(ProfileLocation profileLocation)
	{
		Log.i(TAG, "Entering insideFence, params - " + profileLocation);

		boolean insideFence = false;
		Location definedLocation = new Location("defined");
		definedLocation.setLatitude(profileLocation.getLatitude());
		definedLocation.setLongitude(profileLocation.getLongitude());

		float distance = currentLocation.distanceTo(definedLocation);
		// -- comparing distance with radius defined
		if (distance < profileLocation.getRadius())
		{
			insideFence = true;
		}
		Log.i(TAG, "Exiting insideFence, distance - " + distance + ", radius - "
				+ profileLocation.getRadius());
		return insideFence;
	}

	/**
	 * This method calls the AudioManager to set defined modes.
	 * 
	 * @param mode
	 *            - Mode to be set
	 */
	private void profileAction(int mode)
	{
		Log.i(TAG, "Entering profileAction(), changing mode - " + mode);
		switch (mode)
		{
		case 0:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			break;
		case 1:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			break;
		case 2:
			audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			break;
		default:

		}
		Log.i(TAG, "Exiting profileAction()");
	}

	/**
	 * This method creates fetches all the defined locations and calls helper
	 * methods - insideFence and profileAction
	 */
	public void manageProfile()
	{
		locationDB.open();
		locationList = locationDB.fetchLocations();
		locationDB.close();
		boolean status = false;
		for (ProfileLocation location : locationList)
		{
			status = insideFence(location);
			if (status)
			{
				profileAction(location.getMode());
				break;
			}
		}
	}
}
