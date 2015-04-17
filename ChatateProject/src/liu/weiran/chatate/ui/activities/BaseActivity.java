package liu.weiran.chatate.ui.activities;

import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.util.Utils;
import android.app.ActionBar;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public class BaseActivity extends FragmentActivity {

	protected Context mCtx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mCtx = this;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	void hideSoftInputView() {
		Utils.hideSoftInputView(this);
	}

	void setSoftInputMode(int mode) {
		getWindow().setSoftInputMode(mode);
	}

	void initActionBar() {
		initActionBar(null);
	}

	void initActionBar(String title) {
		ActionBar actionBar = getActionBar();
		if (title != null) {
			actionBar.setTitle(title);
		}
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	void initActionBar(int id) {
		initActionBar(MyApplication.mCtx.getString(id));
	}
}
