/*
Copyright (C) 2006 Nokia Corporation
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
package com.android.airs.handlerUIs;

import android.preference.PreferenceActivity;

import com.android.airs.*;

public class PhoneSensorsHandlerUI implements HandlerUI
{   
	public HandlerEntry init()
	{
		HandlerEntry entry = new HandlerEntry();
		entry.name = new String("Phone Sensors");
		entry.description = new String("Senses various phone-based information like orientation etc");
		entry.resid = R.drawable.phone;
		return (entry);
	}

	public int setDisplay()
	{
		return R.xml.prefsphonesensors;
	}

	public String About()
	{
	    String AboutText = new String(
	    		"Senses various phone-based sensors.\n\n"+
	    		"The orientation is only supported for models with internal compass.");

		return AboutText;
	}
	
	public String AboutTitle()
	{
		return "Phone Sensors";
	}

	public void configurePreference(PreferenceActivity prefs)
	{
	}
}
