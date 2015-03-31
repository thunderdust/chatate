package liu.weiran.chatate.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.app.Activity;

public abstract class NetworkAsyncTask extends AsyncTask<Void, Void, Void> {

	ProgressDialog progressDialog;
	Context mCtx;
	boolean openDialog = true;
	Exception exception;

	protected NetworkAsyncTask(Context ctx) {
		this.mCtx = ctx;
	}
	
	protected NetworkAsyncTask(Context ctx, boolean openDialog){
		this.mCtx = ctx;
		this.openDialog = openDialog;
	}
	
	public NetworkAsyncTask setOpenDialog(boolean openDialog){
		this.openDialog = openDialog;
		return this;
	}
	
	public ProgressDialog getProgressDialog(){
		return this.progressDialog;
	}
	
	@Override 
	protected void onPreExecute(){
		super.onPreExecute();
		if (openDialog){
			progressDialog = Utils.showSpinnerDialog((Activity)mCtx);
		}
	}
	
	@Override
	protected Void doInBackground(Void... params){
		try {
		      doInBack();
		    } catch (Exception e) {
		      e.printStackTrace();
		      exception = e;
		    }
		    return null;
	}
	
	@Override 
	protected void onPostExecute(Void result){
		super.onPostExecute(result);
		if (openDialog&&progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		onPost(exception);
	}
	
	protected abstract void doInBack() throws Exception;
	
	protected abstract void onPost(Exception e);

}
