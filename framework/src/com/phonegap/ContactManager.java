/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 * Copyright (c) 2011, Giant Leap Technologies AS
 */
package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class ContactManager extends Plugin {
	
    private static ContactAccessor contactAccessor;
	private static final String LOG_TAG = "Contact Query";
	private String callbackId;

	/**
	 * Constructor.
	 */
	public ContactManager()	{
	}
	
	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		if (contactAccessor == null) {
			contactAccessor = ContactAccessor.getInstance(webView, ctx);
		}
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("search")) {
				JSONArray res = contactAccessor.search(args.getJSONArray(0), args.getJSONObject(1));
				return new PluginResult(status, res, "navigator.service.contacts.cast");
			}
			else if (action.equals("save")) {
				return new PluginResult(status, contactAccessor.save(args.getJSONObject(0)));
			}
			else if (action.equals("remove")) {
				if (contactAccessor.remove(args.getString(0))) {
					return new PluginResult(status, result);					
				}
				else {
					JSONObject r = new JSONObject();
					r.put("code", 2);
					return new PluginResult(PluginResult.Status.ERROR, r);
				}
			}
			else if(action.equals("chooseContact")){
				return chooseContact(args, callbackId);
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
		}
	private static final int CONTACT_PICKER_RESULT = 1001;

	private PluginResult chooseContact(JSONArray args, String callbackId) {
		this.callbackId = callbackId;

		this.ctx.startActivityForResult(this, ContactAccessor.getInstance(this.webView, this.ctx).getContactPickerIntent(),
				CONTACT_PICKER_RESULT);

		// Return empty dummy result
		PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
		result.setKeepCallback(true);
		return result;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			// Get contact, serialize and pass back as json
			Uri contactUri = intent.getData();		
			JSONObject contact = ContactAccessor.getInstance(webView, ctx).getContactWithUri(contactUri);
			this.success(new PluginResult(PluginResult.Status.OK, contact), this.callbackId);
		}
		// If cancelled
		else if (resultCode == Activity.RESULT_CANCELED) {
			Log.d(LOG_TAG, "Contact picker cancelled");
			this.error(new PluginResult(PluginResult.Status.ERROR,
					"Contact picker cancelled"), this.callbackId);
		}

		// If something else
		else {
			Log.d(LOG_TAG, "Unknown result code");
			this.error(new PluginResult(PluginResult.Status.ERROR,
					"Contact picker error"), this.callbackId);
		}
	}
}
