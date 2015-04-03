package liu.weiran.chatate.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.adapters.BaseListAdapter;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.Name;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.ui.views.xlist.XListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ChatUtils {
	public static void handleListResult(XListView listView,
			BaseListAdapter adapter, List datas) {
		if (Utils.isCollectionNotEmpty(datas)) {
			adapter.addAll(datas);
			if (datas.size() == Name.PAGE_SIZE) {
				listView.setPullLoadEnable(true);
			} else {
				listView.setPullLoadEnable(false);
			}
		} else {
			listView.setPullLoadEnable(false);
			if (adapter.getCount() == 0) {
				Utils.toast(R.string.noResult);
			} else {
				Utils.toast(R.string.dataLoadFinish);
			}
		}
	}

	public static void stopRefresh(XListView xListView) {
		if (xListView.getPullRefreshing()) {
			xListView.stopRefresh();
		}
	}

	public static void setUserView(View conView, AVUser user) {
		ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
		TextView nameView = ViewHolder.findViewById(conView, R.id.username);
		UserService.displayAvatar(User.getAvatarUrl(user), avatarView);
		nameView.setText(user.getUsername());
	}

	public static String convid(String myId, String otherId) {
		List<String> ids;
		ids = new ArrayList<String>();
		ids.add(myId);
		ids.add(otherId);
		return convid(ids);
	}

	public static String convid(List<String> peerIds) {
		Collections.sort(peerIds);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < peerIds.size(); i++) {
			if (i != 0) {
				sb.append(":");
			}
			sb.append(peerIds.get(i));
		}
		return Utils.md5(sb.toString());
	}

	public static void logAVMessage(AVMessage avMsg) {
		Logger.d("avMsg message=" + avMsg.getMessage() + " timestamp="
				+ avMsg.getTimestamp() + " toPeerIds=" + avMsg.getToPeerIds()
				+ " fromPeerId=" + avMsg.getFromPeerId() + " receiptTs="
				+ avMsg.getReceiptTimestamp() + " groupId="
				+ avMsg.getGroupId() + " isRequestReceipt="
				+ avMsg.isRequestReceipt());
	}
}
