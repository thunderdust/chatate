package liu.weiran.chatate.ui.activities;

import liu.weiran.chatate.R;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.util.Utils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.avos.avoscloud.AVUser;

public class SplashActivity extends BaseEntryActivity {
	private static final int GO_MAIN_MSG = 1;
	private static final int GO_LOGIN_MSG = 2;
	public static final int SPLASH_DURATION = 2500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

		if (AVUser.getCurrentUser() != null) {
			UserService.updateUserInfo();
			handler.sendEmptyMessageDelayed(GO_MAIN_MSG, SPLASH_DURATION);
		} else {
			handler.sendEmptyMessageDelayed(GO_LOGIN_MSG, SPLASH_DURATION);
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_MAIN_MSG:
				MainActivity.goMainActivity(SplashActivity.this);
				finish();
				break;
			case GO_LOGIN_MSG:
				Utils.goActivity(mCtx, LoginActivity.class);
				finish();
				break;
			}
		}
	};
}
