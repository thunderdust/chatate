package liu.weiran.chatate.ui.activities;

import liu.weiran.chatate.service.receiver.FinishReceiver;
import android.os.Bundle;

public class BaseEntryActivity extends BaseActivity {

	FinishReceiver finishReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		finishReceiver = FinishReceiver.register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(finishReceiver);
	}
}

