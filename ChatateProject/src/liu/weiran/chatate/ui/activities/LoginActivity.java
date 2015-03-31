package liu.weiran.chatate.ui.activities;

import com.avos.avoscloud.AVUser;

import liu.weiran.chatate.R;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends BaseEntryActivity implements OnClickListener {

	EditText usernameET, passwordET;
	Button loginBtn;
	TextView registerBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		init();
	}

	private void init() {
		usernameET = (EditText) findViewById(R.id.et_username);
		passwordET = (EditText) findViewById(R.id.et_password);
		loginBtn = (Button) findViewById(R.id.btn_login);
		registerBtn = (TextView) findViewById(R.id.btn_register);
		loginBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == registerBtn) {
			Utils.goActivity(mCtx, RegisterActivity.class);
		} else if (v == loginBtn) {
			login();
		}
	}

	private void login() {
		final String name = usernameET.getText().toString();
		final String password = passwordET.getText().toString();

		if (TextUtils.isEmpty(name)) {
			Utils.toast(mCtx, R.string.username_cannot_null);
			return;
		}

		if (TextUtils.isEmpty(password)) {
			Utils.toast(mCtx, R.string.password_cannot_null);
			return;
		}

		new NetworkAsyncTask(mCtx) {

			@Override
			protected void doInBack() throws Exception {
				AVUser.logIn(name, password);
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					Utils.toast(e.getMessage());
				} else {
					Utils.goActivity(mCtx, MainActivity.class);
					finish();
				}
			}
		}.execute();
	}
}
