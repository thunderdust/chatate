package liu.weiran.chatate.ui.activities;

import android.os.Bundle;
import liu.weiran.chatate.R;

public class NotifySettingActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notify_setting);
		initActionBar(R.string.notifySetting);
	}
}
