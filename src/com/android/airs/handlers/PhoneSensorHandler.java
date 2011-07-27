/*
Copyright (C) 2008-2011, Dirk Trossen, nors@dirk-trossen.de

This program is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation as version 2.1 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this library; if not, write to the Free Software Foundation, Inc.,
59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
*/
package com.android.airs.handlers;

import java.util.concurrent.Semaphore;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;

import com.android.airs.platform.HandlerManager;
import com.android.airs.platform.SensorRepository;

public class PhoneSensorHandler implements com.android.airs.handlers.Handler
{
	private Context nors;

	private boolean sensor_enable = false;
	private SensorManager sensorManager;
	private android.hardware.Sensor Orientation, Proximity, Light;
	// polltime for sensor
	private int polltime = 10000, polltime2 = 10000;
	// sensor values
	private float azimuth, roll, pitch, proximity, light;
	private float azimuth_old, roll_old, pitch_old, proximity_old, light_old;
	private Semaphore light_semaphore 		= new Semaphore(1);
	private Semaphore proximity_semaphore 	= new Semaphore(1);
	private Semaphore azimuth_semaphore 	= new Semaphore(1);
	private Semaphore roll_semaphore 		= new Semaphore(1);
	private Semaphore pitch_semaphore 		= new Semaphore(1);

	/**
	 * Sleep function 
	 * @param millis
	 */
	protected void sleep(long millis) 
	{
		try 
		{
			Thread.sleep(millis);
		} 
		catch (InterruptedException ignore) 
		{
		}
	}
	
	private void wait(Semaphore sema)
	{
		try
		{
			sema.acquire();
		}
		catch(Exception e)
		{
		}
	}

	/***********************************************************************
	 Function    : Acquire()
	 Input       : sensor input is ignored here!
	 Output      :
	 Return      :
	 Description : acquires current sensors values and sends to
	 		 	   QueryResolver component
	***********************************************************************/
	public byte[] Acquire(String sensor, String query)
	{
		boolean read = false;
		int value = 0;
		byte [] readings = null;
		
		// anything there?
		if (sensor_enable == true)
		{
			// see which sensors are requested
			if (sensor.equals("Az") == true)
			{				
				wait(azimuth_semaphore); 
				if (azimuth != azimuth_old)
				{
					read = true;
					value = (int)(azimuth*10);
					azimuth_old = azimuth;
				}
			}
			
			if (read == false)
				if (sensor.equals("Pi") == true)
				{					
					wait(pitch_semaphore); 
					if (pitch != pitch_old)
					{
						read = true;
						value = (int)(pitch*10);
						pitch_old = pitch;
					}
				}

			if (read == false)
				if (sensor.equals("Ro") == true)
				{					
					wait(roll_semaphore); 
					if (roll != roll_old)
					{
						read = true;
						value = (int)(roll*10);
						roll_old = roll;
					}
				}				
			if (read == false)
				if (sensor.equals("PR") == true)
				{					
					wait(proximity_semaphore); 
					if (proximity != proximity_old)
					{
						read = true;
						value = (int)(proximity*10);
						proximity_old = proximity;
					}
				}				
			if (read == false)
				if (sensor.equals("LI") == true)
				{					
					wait(light_semaphore); 
					if (light != light_old)
					{
						read = true;
						value = (int)(light*10);
						light_old = light;
					}
				}				
		}
		
		// anything to report?
		if (read == true)
		{
			readings = new byte[6];
			readings[0] = (byte)sensor.charAt(0);
			readings[1] = (byte)sensor.charAt(1);
			readings[2] = (byte)((value>>24) & 0xff);
			readings[3] = (byte)((value>>16) & 0xff);
			readings[4] = (byte)((value>>8) & 0xff);
			readings[5] = (byte)(value & 0xff);
		}

		return readings;		
	}
	
	/***********************************************************************
	 Function    : Discover()
	 Input       : 
	 Output      : string with discovery information
	 Return      : 
	 Description : provides discovery information of this particular acquisition 
	 			   module, hardcoded 
	***********************************************************************/
	public void Discover()
	{
		if (sensor_enable == true)
		{
		   SensorRepository.insertSensor(new String("Az"), new String("degrees"), new String("Azimuth"), new String("int"), -1, 0, 3600, polltime, this);
		   SensorRepository.insertSensor(new String("Pi"), new String("degrees"), new String("Pitch"), new String("int"), -1, -1800, 1800, polltime, this);
		   SensorRepository.insertSensor(new String("Ro"), new String("degrees"), new String("Roll"), new String("int"), -1, -900, 900, polltime, this);	
		   SensorRepository.insertSensor(new String("PR"), new String("-"), new String("Proximity"), new String("int"), -1, 0, 1000, polltime2, this);	
		   SensorRepository.insertSensor(new String("LI"), new String("Lux"), new String("Light"), new String("int"), -1, 0, 50000, polltime2, this);	
		}
	}
	
	public PhoneSensorHandler(Context activity)
	{
		nors = activity;
		
		// read polltime
		polltime  = HandlerManager.readRMS_i("PhoneSensorsHandler::OrientationPoll", 10) * 1000;
		polltime2 = HandlerManager.readRMS_i("PhoneSensorsHandler::ProximityPoll", 10) * 1000;
		// read whether or not we need to enable GPS
		sensor_enable = HandlerManager.readRMS_b("PhoneSensorsHandler::SensorsON", false);

		// no sensors being shown?
		if (sensor_enable == false)
			return;
		
		// try to open sensor services
		try
		{
			sensorManager = (SensorManager)nors.getSystemService(Context.SENSOR_SERVICE);
			Orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			Proximity 	= sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			Light		= sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
     	    sensorManager.registerListener(sensorlistener, Light, SensorManager.SENSOR_DELAY_NORMAL);
     	    sensorManager.registerListener(sensorlistener, Proximity, SensorManager.SENSOR_DELAY_NORMAL);
     	    sensorManager.registerListener(sensorlistener, Orientation, SensorManager.SENSOR_DELAY_NORMAL);
			sensor_enable = true;
			// arm semaphores
			wait(light_semaphore); 
			wait(proximity_semaphore); 
			wait(azimuth_semaphore); 
			wait(roll_semaphore); 
			wait(pitch_semaphore); 
		}
		catch(Exception e)
		{
			sensor_enable = false;
		}
	}
	
	public void destroyHandler()
	{
		if (sensor_enable == true)
			sensorManager.unregisterListener(sensorlistener);	
	}

    private SensorEventListener sensorlistener = new SensorEventListener() 
    {
    	public void	 onSensorChanged(SensorEvent event)    
    	{
    		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
    		{
				 azimuth=event.values[0];
				 pitch=event.values[1];
				 roll=event.values[2];	

				 // now release the semaphores
				 azimuth_semaphore.release(); 
				 roll_semaphore.release(); 
				 pitch_semaphore.release(); 
    		}

    		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY)
    		{
				 proximity=event.values[0];
				 // now release the semaphores
				 proximity_semaphore.release(); 
    		}
    		if (event.sensor.getType() == Sensor.TYPE_LIGHT)
    		{
				 light=event.values[0];
				 // now release the semaphores
				 light_semaphore.release(); 
    		}
       	}

		public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) 
		{
		}
    };  
}
