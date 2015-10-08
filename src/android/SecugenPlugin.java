package com.equitybankgroup.SecugenPlugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.app.Activity;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.util.Log;
import java.io.*;
import android.content.Context;
import com.equitybankgroup.SecugenPlugin.FingerPrintController;
import com.equitybankgroup.SecugenPlugin.SecureServerConnect;
import com.equitybankgroup.SecugenPlugin.ServerConnect;
import android.os.AsyncTask;
import org.json.JSONArray;
import android.widget.Toast;


public class SecugenPlugin extends CordovaPlugin {
	private FingerPrintController fpc;
	String serverUrl = "";
	String serverPrints = "";
	Context context;

	// public SecugenPlugin(){
	// 	initDevice();
	// }

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		initDevice();
		String res = "Error";
		try {
			if (action.equals("capturePrint")) {
				res = capturePrint(args);
			}else if(action.equals("verifyPrint")){
				res =  verifyPrint(args);
			}else{
				callbackContext.error("Unknown Action: "+action);
			}
			callbackContext.success(res);
			return true;
		} catch (Exception e) {
			System.err.println("Exception: "+e.getMessage());
			e.printStackTrace();
			callbackContext.error("Error: execute();;"+e.getMessage());
			return false;
		}
	}

	public void initDevice(){
		try{
			context = this.cordova.getActivity().getApplicationContext();
			fpc = new FingerPrintController(context);
			fpc.initDevice();
		}catch(Exception e){
			System.err.println("SecuginPlugin.initDevice();; Error: "+e.getMessage());
		}

	}

	public String capturePrint(JSONArray args){
		Log.d("AccountOpening","Capturing Print - PLUGIN CALL");
		try{
			JSONObject res = fpc.registerPrint();
			return res.toString();
		}catch(Exception e){
			System.err.println("Exception: "+e.getMessage());
			return "Error: capturePrint();;"+e.getMessage();
		}
	}

	public String verifyPrint(JSONArray args){
		JSONObject res = new JSONObject();
		
		Log.d("AccountOpening","Verify Print - PLUGIN CALL");
		try{
			for(int i=0;i<args.length();i++){
				serverUrl = args.get(i).toString();
				Log.d("AccountOpening","SERVER URL: "+serverUrl);
			}
			getServerPrints();
			if (serverPrints.isEmpty()) {
				res.put("response_code", "0");
				res.put("response_message", "Failed To Get Verify With Server");
				Log.d("AccountOpening","No Prints");
			} else {
				res = fpc.verify(serverPrints);
			}
		}catch(Exception e){
			System.err.println("Exception: "+e.getMessage());
			return "Error: verifyPrint();;"+e.getMessage();
		}

		return res.toString();
	}

	public void getServerPrints() {
		try {
			JSONObject params = new JSONObject();
			params.put("action", "getPrints");
             serverUrl = "http://10.1.9.100:7001/AccountOpeningAPI/getPrints";
			new DoServerTask(params).execute(serverUrl);
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
//            return "Error: capturePrint();;" + e.getMessage();
		}
	}


	private class DoServerTask extends AsyncTask<String, Void, String> {
		private JSONObject params;

		public DoServerTask(JSONObject params) {
			this.params = params;
		}

		@Override
		protected void onPreExecute() {
			// setSupportProgressBarIndeterminateVisibility(true);
		}


		@Override
		protected String doInBackground(String... urls) {
			try {
				if (serverUrl.contains("https")) {
					Log.d("AccountOpening", "SecureServerConnect Called");
					SecureServerConnect sc = new SecureServerConnect();
					return sc.processRequest("http://10.1.9.100:7001/AccountOpeningAPI/getPrints", params);
				} else {
					Log.d("AccountOpening", "ServerConnect Called");
					ServerConnect sc = new ServerConnect();
					return sc.processRequest("http://10.1.9.100:7001/AccountOpeningAPI/getPrints", params);
				}
			} catch (Exception e) {
				Log.d("AccountOpening", e.toString());
				e.printStackTrace();
				return "{'response_code':'400','message':'errorServerUnreachable'}";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// setSupportProgressBarIndeterminateVisibility(false);
			try {
				JSONObject res = new JSONObject(result);
				Log.d("AccountOpening", "Response Code: " + res.get("response_code") + "\nResponse Message: " + res.get("response_message").toString());
				if (res.getString("response_code").equalsIgnoreCase("1")) {
					serverPrints = res.get("response_payload").toString();
				} else {
					Toast.makeText(context, res.getString("response_message"), Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}
}