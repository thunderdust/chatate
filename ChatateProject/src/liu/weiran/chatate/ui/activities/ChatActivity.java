package liu.weiran.chatate.ui.activities;

import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.RoomType;
import android.view.View;
import android.view.View.OnClickListener;

public class ChatActivity extends BaseActivity implements OnClickListener {

	public static RoomType roomType;

	@Override
	public void onClick(View v) {
	}

	public void resendMsg(final Message resendMsg) {
		//MsgAgent.resendMsg(resendMsg, sendCallback);
	}
}
