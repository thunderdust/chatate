package liu.weiran.chatate.ui.activities.book;

import java.io.IOException;

import com.avos.avoscloud.AVException;

import liu.weiran.chatate.R;
import liu.weiran.chatate.service.CloudService;
import liu.weiran.chatate.ui.activities.BaseActivity;
import liu.weiran.chatate.ui.activities.MainActivity;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;
import liu.weiran.chatate.util.book.ReturnedContentHandler;
import liu.weiran.chatate.util.book.tdHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ReadPeerLoginActivity extends BaseActivity {

	/* View elements */
	private String TAG = "login";
	private Button loginButton;
	private CheckBox rem_password, auto_login;
	private EditText et_username;
	private EditText et_password;
	/* Account variables */
	private String username = null;
	private String password = null;
	private static String access_token;
	private static String uid;
	private static String site_username;
	/* Http components */
	private tdHttpClient mHttpClient;
	private ReturnedContentHandler mContentHandler;
	/* Content type label for content handler */
	private static int TYPE_STRING = 1;
	private static int TYPE_INT = 2;
	/* Remember password and auto login flag */
	private static String passwordCheckBoolean = "isRemPwCheck";
	private static String autoLoginCheckBoolean = "isAutoLoginCheck";
	private SharedPreferences sp;

	private final int RESULT_OK = 1;
	private final int RESULT_CANCEL = -1;
	private static String loginFeedback = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_readpeer_login);

		// cancel auto login and remember password after log out
		Boolean isAutoLoginCanceled = false;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			isAutoLoginCanceled = extras.getBoolean("isAutoLoginCanceled");
		}
		getElements();
		loadPreferences(isAutoLoginCanceled);
		initialize();
		checkAutoLogin();
	}

	private void getElements() {
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		rem_password = (CheckBox) findViewById(R.id.checkBox_remberPassword);
		auto_login = (CheckBox) findViewById(R.id.checkBox_autoLogin);
	}

	private void initialize() {

		mHttpClient = tdHttpClient.getClientInstance();
		rem_password.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (rem_password.isChecked())
					sp.edit().putBoolean(passwordCheckBoolean, true).commit();
				else
					sp.edit().putBoolean(passwordCheckBoolean, false).commit();
			}
		});
		auto_login.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (auto_login.isChecked())
					sp.edit().putBoolean(autoLoginCheckBoolean, true).commit();
				else
					sp.edit().putBoolean(autoLoginCheckBoolean, false).commit();
			}

		});

		loginButton = (Button) findViewById(R.id.btn_login);
		loginButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				username = et_username.getText().toString();
				password = et_password.getText().toString();

				if (rem_password.isChecked()) {
					Editor editor = sp.edit();
					editor.putString("username", username);
					editor.putString("password", password);
					editor.commit();
				}
				try {
					attemptLogin();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private void loadPreferences(boolean isAutoLoginCanceled) {

		sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		// If user log out from last use, cancel auto login
		// erase user information
		if (isAutoLoginCanceled) {
			rem_password.setChecked(false);
			auto_login.setChecked(false);
			SharedPreferences.Editor editor = sp.edit();
			editor.remove("password");

			// Should clear access_token as well
		}

		else {
			// Check for remember password and auto login settings
			if (sp.getBoolean(passwordCheckBoolean, false)) {
				rem_password.setChecked(true);
				et_username.setText(sp.getString("username", ""));
				et_password.setText(sp.getString("password", ""));
			}
			if (sp.getBoolean(autoLoginCheckBoolean, false)) {
				auto_login.setChecked(true);
			}
		}
	}

	private void checkAutoLogin() {
		if (auto_login.isChecked()) {
			username = et_username.getText().toString();
			password = et_password.getText().toString();
			try {
				attemptLogin();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void attemptLogin() throws IOException, Exception {

		Log.v(TAG, "Trying to login");
		mHttpClient.clearCookies();

		new NetworkAsyncTask(mCtx) {
			@Override
			protected void doInBack() throws Exception {
				loginFeedback = mHttpClient.logIn(username, password);
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					Utils.toast(e.getMessage());
				} else {
					handleLoginFeedback(loginFeedback);
				}
			}
		}.execute();
	}

	private void handleLoginFeedback(String loginFeedback) {

		if (loginFeedback != null && loginFeedback != "unauthorized") {
			getLoginData(loginFeedback);
			// After login successfully, jump to bookshelf activity
			Toast.makeText(this, "Welcome Back, " + site_username + " !",
					Toast.LENGTH_SHORT).show();

			Intent passInforIntent = new Intent(this, MainActivity.class);
			passInforIntent.putExtra("access_token", access_token);
			passInforIntent.putExtra("uid", uid);
			passInforIntent.putExtra("username", site_username);
			setResult(RESULT_OK, passInforIntent);
			finish();

		} else if (loginFeedback == "unauthorized") {
			Toast.makeText(this, "account unauthorized", Toast.LENGTH_SHORT)
					.show();
			Intent passInforIntent = new Intent(this, MainActivity.class);
			setResult(RESULT_CANCEL, passInforIntent);
			finish();
		} else if (loginFeedback == null) {
			Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show();
			Intent passInforIntent = new Intent(this, MainActivity.class);
			setResult(RESULT_CANCEL, passInforIntent);
			finish();
		}

	}

	private void getLoginData(String data) {
		mContentHandler = ReturnedContentHandler.getHandlerInstance();

		// Pass necessary data to profile activity
		access_token = mContentHandler.getValueFromContentReturned(data,
				"access_token", TYPE_STRING);

		// Save access token to cache
		SharedPreferences sp = this.getSharedPreferences("token",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("token", access_token);
		editor.commit();
		
		uid = mContentHandler.getValueFromContentReturned(data, "uid",
				TYPE_STRING);
		
		site_username = mContentHandler.getValueFromContentReturned(data,
				"name", TYPE_STRING);
		
		Log.i(TAG, uid);
		Log.i(TAG, access_token);
		Log.i(TAG, site_username);
	}
}
