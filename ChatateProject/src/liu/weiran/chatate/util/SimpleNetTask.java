package liu.weiran.chatate.util;

import liu.weiran.chatate.R;
import android.content.Context;

public abstract class SimpleNetTask extends NetworkAsyncTask {
	protected SimpleNetTask(Context cxt) {
		super(cxt);
	}

	protected SimpleNetTask(Context cxt, boolean openDialog) {
		super(cxt, openDialog);
	}

	@Override
	protected void onPost(Exception e) {
		if (e != null) {
			e.printStackTrace();
			Utils.toast(mCtx, R.string.pleaseCheckNetwork);
		} else {
			onSucceed();
		}
	}

	protected abstract void doInBack() throws Exception;

	protected abstract void onSucceed();
}
