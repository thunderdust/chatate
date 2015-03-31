package liu.weiran.chatate.ui.activities;

import com.avos.avoscloud.AVUser;

import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class RegisterActivity extends BaseEntryActivity {

	Button registerButton;
	EditText usernameEdit, passwordEdit, emailEdit;
	RadioGroup sexRadio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		getView();
		initActionBar(MyApplication.mCtx.getString(R.string.register));
		registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				register();
			}
		});
	}

	private void getView() {
		usernameEdit = (EditText) findViewById(R.id.usernameEdit);
		passwordEdit = (EditText) findViewById(R.id.passwordEdit);
		emailEdit = (EditText) findViewById(R.id.ensurePasswordEdit);
		registerButton = (Button) findViewById(R.id.btn_register);
		sexRadio = (RadioGroup) findViewById(R.id.sexRadio);
	}

	private void register() {
		final String name = usernameEdit.getText().toString();
		final String password = passwordEdit.getText().toString();
		String againPassword = emailEdit.getText().toString();
		if (TextUtils.isEmpty(name)) {
			Utils.toast(R.string.username_cannot_null);
			return;
		}

		if (TextUtils.isEmpty(password)) {
			Utils.toast(R.string.password_cannot_null);
			return;
		}
		if (!againPassword.equals(password)) {
			Utils.toast(R.string.password_not_consistent);
			return;
		}

		int checkedId = sexRadio.getCheckedRadioButtonId();
		final User.Gender gender;
		gender = checkedId == R.id.male ? User.Gender.Male : User.Gender.Female;

		new NetworkAsyncTask(mCtx) {
			@Override
			protected void doInBack() throws Exception {
				AVUser user = UserService.signUp(name, password);
				User.setGender(user, gender);
				user.setFetchWhenSave(true);
				user.save();
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					Utils.toast(MyApplication.mCtx
							.getString(R.string.registerFailed)
							+ e.getMessage());
				} else {
					Utils.toast(R.string.registerSucceed);
					UserService.updateUserLocation();
					MainActivity.goMainActivity(RegisterActivity.this);
				}
			}
		}.execute();
	}

}
