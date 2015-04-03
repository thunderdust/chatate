package liu.weiran.chatate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.base.Name;
import liu.weiran.chatate.entity.Conversation;
import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.RoomType;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.util.EmoticonUtils;
import liu.weiran.chatate.util.ImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RecentMessageAdapter extends BaseListAdapter<Conversation> {

	private LayoutInflater inflater;
	private Context ctx;

	public RecentMessageAdapter(Context context) {
		super(context);
		this.ctx = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final Conversation item = items.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.conversation_item, parent,
					false);
		}
		ImageView recentAvatarView = ViewHolder.findViewById(convertView,
				R.id.iv_recent_avatar);
		TextView recentNameView = ViewHolder.findViewById(convertView,
				R.id.recent_time_text);
		TextView recentMsgView = ViewHolder.findViewById(convertView,
				R.id.recent_msg_text);
		TextView recentTimeView = ViewHolder.findViewById(convertView,
				R.id.recent_teim_text);
		TextView recentUnreadView = ViewHolder.findViewById(convertView,
				R.id.recent_unread);

		Message msg = item.getMessage();
		if (msg.getRoomType() == RoomType.Single) {
			AVUser user = item.getToUser();
			String avatar = User.getAvatarUrl(user);
			if (avatar != null && !avatar.equals("")) {
				ImageLoader.getInstance().displayImage(avatar,
						recentAvatarView, ImageUtils.avatarImageOptions);
			} else {
				recentAvatarView
						.setImageResource(R.drawable.default_user_avatar);
			}
			recentNameView.setText(user.getUsername());
		} else {
			ChatGroup chatGroup = item.getToChatGroup();
			recentNameView.setText(chatGroup.getGroupDesc());
			recentAvatarView.setImageResource(R.drawable.group_icon);
		}

		// recentTimeView.setText(TimeUtils.getDate);
		int num = item.getUnreadCount();
		if (msg.getType() == Message.Type.Text) {
			CharSequence spannableString = EmoticonUtils.replace(ctx,
					msg.getContent());
			recentMsgView.setText(spannableString);
		} else if (msg.getType() == Message.Type.Image) {
			recentMsgView.setText("["
					+ MyApplication.mCtx.getString(R.string.image) + "]");
		} else if (msg.getType() == Message.Type.Annotation) {
			String all = msg.getContent();
			if (all != null && !all.equals("")) {
				String address = all.split("&")[0];
				recentMsgView.setText("["
						+ MyApplication.mCtx.getString(R.string.position) + "]"
						+ address);
			}
		} else if (msg.getType() == Message.Type.Audio) {
			recentMsgView.setText("["
					+ MyApplication.mCtx.getString(R.string.audio) + "]");
		}

		if (num > 0) {
			recentUnreadView.setVisibility(View.VISIBLE);
			recentUnreadView.setText(num + "");
		} else {
			recentUnreadView.setVisibility(View.GONE);
		}
		return convertView;
	}
}
