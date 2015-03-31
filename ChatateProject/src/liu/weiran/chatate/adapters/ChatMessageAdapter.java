package liu.weiran.chatate.adapters;

import java.io.File;
import java.util.List;

import com.avos.avoscloud.AVUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.RoomType;
import liu.weiran.chatate.service.AudioHelper;
import liu.weiran.chatate.service.CacheService;
import liu.weiran.chatate.service.UserService;
import liu.weiran.chatate.ui.activities.AnnotationActivity;
import liu.weiran.chatate.ui.activities.ChatActivity;
import liu.weiran.chatate.ui.activities.ImageBrowserActivity;
import liu.weiran.chatate.ui.views.PlayButton;
import liu.weiran.chatate.ui.views.ViewHolder;
import liu.weiran.chatate.util.EmoticonUtils;
import liu.weiran.chatate.util.ImageUtils;
import liu.weiran.chatate.util.PathUtils;
import liu.weiran.chatate.util.TimeUtils;

public class ChatMessageAdapter extends BaseListAdapter<Message> {
	int msgViewTypes = 8;

	public enum MsgViewType {
		ComeText(0), ToText(1), ComeImage(2), ToImage(3), ComeAudio(4), ToAudio(
				5), ComeAnnotation(6), ToAnnotation(7);
		int value;

		MsgViewType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	ChatActivity chatActivity;

	public ChatMessageAdapter(ChatActivity chatActivity, List<Message> datas) {
		super(chatActivity, datas);
		this.chatActivity = chatActivity;
	}

	public int getItemPosById(String objectId) {
		for (int i = 0; i < getCount(); i++) {
			Message itemMsg = items.get(i);
			if (itemMsg.getObjectId().equals(objectId)) {
				return i;
			}
		}
		return -1;
	}

	public Message getItem(String objectId) {
		for (Message msg : items) {
			if (msg.getObjectId().equals(objectId)) {
				return msg;
			}
		}
		return null;
	}

	@Override
	public int getItemViewType(int position) {
		Message entity = items.get(position);
		boolean comeMsg = entity.isComeMessage();
		Message.Type type = entity.getType();
		MsgViewType viewType;
		switch (type) {
		case Text:
			viewType = comeMsg ? MsgViewType.ComeText : MsgViewType.ToText;
			break;
		case Image:
			viewType = comeMsg ? MsgViewType.ComeImage : MsgViewType.ToImage;
			break;
		case Audio:
			viewType = comeMsg ? MsgViewType.ComeAudio : MsgViewType.ToAudio;
			break;
		case Annotation:
			viewType = comeMsg ? MsgViewType.ComeAnnotation
					: MsgViewType.ToAnnotation;
			break;
		default:
			throw new IllegalStateException();
		}
		return viewType.getValue();
	}

	@Override
	public int getViewTypeCount() {
		return msgViewTypes;
	}

	public View getView(int position, View conView, ViewGroup parent) {
		Message msg = items.get(position);
		boolean isComMsg = msg.isComeMessage();
		if (conView == null) {
			conView = createViewByType(msg.getType(), isComMsg);
		}
		TextView sendTimeView = ViewHolder.findViewById(conView,
				R.id.sendTimeView);
		TextView contentView = ViewHolder.findViewById(conView,
				R.id.textContent);
		View contentLayout = ViewHolder.findViewById(conView,
				R.id.contentLayout);
		ImageView imageView = ViewHolder.findViewById(conView, R.id.imageView);
		ImageView avatarView = ViewHolder.findViewById(conView, R.id.avatar);
		PlayButton playBtn = ViewHolder.findViewById(conView, R.id.playBtn);
		TextView annotationView = ViewHolder.findViewById(conView,
				R.id.annotationView);
		TextView usernameView = ViewHolder.findViewById(conView, R.id.username);

		View statusSendFailed = ViewHolder.findViewById(conView,
				R.id.status_send_failed);
		View statusSendSucceed = ViewHolder.findViewById(conView,
				R.id.status_send_succeed);
		View statusSendStart = ViewHolder.findViewById(conView,
				R.id.status_send_start);

		// timestamp
		if (position == 0
				|| TimeUtils.haveTimeGap(
						items.get(position - 1).getTimestamp(),
						msg.getTimestamp())) {
			sendTimeView.setVisibility(View.VISIBLE);
			sendTimeView.setText(TimeUtils.millisecs2DateString(msg
					.getTimestamp()));
		} else {
			sendTimeView.setVisibility(View.GONE);
		}

		String fromPeerId = msg.getFromPeerId();
		AVUser user = CacheService.lookupUser(fromPeerId);
		if (user == null) {
			throw new RuntimeException("cannot find user");
		}
		if (isComMsg == true) {
			if (chatActivity.roomType == RoomType.Single) {
				usernameView.setVisibility(View.GONE);
			} else {
				usernameView.setVisibility(View.VISIBLE);
				usernameView.setText(user.getUsername());
			}
		}
		UserService.displayAvatar(User.getAvatarUrl(user), avatarView);

		Message.Type type = msg.getType();
		if (type == Message.Type.Text) {
			contentView.setText(EmoticonUtils.replace(ctx, msg.getContent()));
			contentLayout.requestLayout();
		} else if (type == Message.Type.Image) {
			String localPath = PathUtils.getChatFileDir() + msg.getObjectId();
			String url = msg.getContent();
			displayImageByUri(imageView, localPath, url);
			setImageOnClickListener(localPath, url, imageView);
		} else if (type == Message.Type.Audio) {
			initPlayBtn(msg, playBtn);
		} else if (type == Message.Type.Annotation) {
			setAnnotationView(msg, annotationView);
		}
		if (isComMsg == false) {
			hideStatusViews(statusSendStart, statusSendFailed,
					statusSendSucceed);
			setSendFailedBtnListener(statusSendFailed, msg);
			switch (msg.getStatus()) {
			case SendFailed:
				statusSendFailed.setVisibility(View.VISIBLE);
				break;
			case SendSucceed:
				statusSendSucceed.setVisibility(View.VISIBLE);
				break;
			case SendStart:
				statusSendStart.setVisibility(View.VISIBLE);
				break;
			}
			if (ChatActivity.roomType == RoomType.Group) {
				statusSendSucceed.setVisibility(View.GONE);
			}
		}
		return conView;
	}

	private void setSendFailedBtnListener(View statusSendFailed,
			final Message msg) {
		statusSendFailed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				chatActivity.resendMsg(msg);
			}
		});
	}

	private void hideStatusViews(View statusSendStart, View statusSendFailed,
			View statusSendSucceed) {
		statusSendFailed.setVisibility(View.GONE);
		statusSendStart.setVisibility(View.GONE);
		statusSendSucceed.setVisibility(View.GONE);
	}

	public void setAnnotationView(Message msg, TextView locationView) {
		try {
			String content = msg.getContent();
			if (content != null && !content.equals("")) {
				String[] parts = content.split("&");
				String address = parts[0];
				final String latitude = parts[1];
				final String longtitude = parts[2];
				locationView.setText(address);
				locationView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(ctx,
								AnnotationActivity.class);
						intent.putExtra("type", "scan");
						intent.putExtra("latitude",
								Double.parseDouble(latitude));
						intent.putExtra("longtitude",
								Double.parseDouble(longtitude));
						ctx.startActivity(intent);
					}
				});
			}
		} catch (Exception e) {
		}
	}

	private void initPlayBtn(Message msg, PlayButton playBtn) {
		playBtn.setLeftSide(msg.isComeMessage());
		AudioHelper audioHelper = AudioHelper.getInstance();
		playBtn.setAudioHelper(audioHelper);
		playBtn.setPath(msg.getAudioPath());
	}

	private void setImageOnClickListener(final String path, final String url,
			ImageView imageView) {
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ctx, ImageBrowserActivity.class);
				intent.putExtra("path", path);
				intent.putExtra("url", url);
				ctx.startActivity(intent);
			}
		});
	}

	public static void displayImageByUri(ImageView imageView, String localPath,
			String url) {
		File file = new File(localPath);
		ImageLoader imageLoader = UserService.imageLoader;
		if (file.exists()) {
			imageLoader.displayImage("file://" + localPath, imageView,
					ImageUtils.normalImageOptions);
		} else {
			imageLoader.displayImage(url, imageView,
					ImageUtils.normalImageOptions);
		}
	}

	public View createViewByType(Message.Type type, boolean isComeMsg) {
		View baseView;
		if (isComeMsg) {
			baseView = inflater.inflate(R.layout.chat_item_base_left, null);
		} else {
			baseView = inflater.inflate(R.layout.chat_item_base_right, null);
		}
		LinearLayout contentView = (LinearLayout) baseView
				.findViewById(R.id.contentLayout);
		int contentId;
		switch (type) {
		case Text:
			contentId = R.layout.chat_item_text;
			break;
		case Audio:
			contentId = R.layout.chat_item_audio;
			break;
		case Image:
			contentId = R.layout.chat_item_image;
			break;
		case Annotation:
			contentId = R.layout.chat_item_location;
			break;
		default:
			throw new IllegalStateException();
		}
		contentView.removeAllViews();
		View content = inflater.inflate(contentId, null, false);
		if (type == Message.Type.Audio) {
			PlayButton btn = (PlayButton) content;
			btn.setLeftSide(isComeMsg);
		} else if (type == Message.Type.Text) {
			TextView textView = (TextView) content;
			if (isComeMsg) {
				textView.setTextColor(Color.BLACK);
			} else {
				textView.setTextColor(Color.WHITE);
			}
		}
		contentView.addView(content);
		return baseView;
	}
}
