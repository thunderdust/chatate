package liu.weiran.chatate.service.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/*A Broadcast receiver which broadcast the action of finish chatting.*/
public class FinishReceiver extends BroadcastReceiver {

	// predefined finish chatting action code
	private static final String FINISH_ACTION = "liu.weiran.chatate.chat_finish";
	private Activity mActivity;

	public FinishReceiver(Activity a) {
		this.mActivity = a;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && intent.getAction().equals(FINISH_ACTION)) {
			mActivity.finish();
		}
	}

	public static FinishReceiver register(Activity a) {
		FinishReceiver fr = new FinishReceiver(a);
		a.registerReceiver(fr, new IntentFilter(FinishReceiver.FINISH_ACTION));
		return fr;
	}

	public static void broadcastFinish(Context context) {
		context.sendBroadcast(new Intent(FINISH_ACTION));
	}
}
