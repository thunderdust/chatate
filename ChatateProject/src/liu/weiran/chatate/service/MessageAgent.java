package liu.weiran.chatate.service;

import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.Session;

import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.base.Name;
import liu.weiran.chatate.database.DatabaseMessage;
import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.MessageBuilder;
import liu.weiran.chatate.entity.RoomType;
import liu.weiran.chatate.entity.MessageSendCallback;
import liu.weiran.chatate.util.NetworkAsyncTask;

import java.io.IOException;

public class MessageAgent {
	RoomType roomType;
	String toId;

	public MessageAgent(RoomType roomType, String toId) {
		this.roomType = roomType;
		this.toId = toId;
	}

	public interface MessageBuilderHelper {
		void specifyType(MessageBuilder MessageBuilder);
	}

	public void createAndSendMsg(MessageBuilderHelper MessageBuilderHelper,
			final MessageSendCallback callback) {
		final MessageBuilder builder = new MessageBuilder();
		builder.target(roomType, toId);
		MessageBuilderHelper.specifyType(builder);
		final Message msg = builder.preBuild();
		DatabaseMessage.insertMessage(msg);
		callback.onStart(msg);
		uploadAndSendMsg(msg, callback);
	}

	public void uploadAndSendMsg(final Message msg,
			final MessageSendCallback callback) {
		new NetworkAsyncTask(MyApplication.mCtx, false) {
			String uploadUrl;

			@Override
			protected void doInBack() throws Exception {
				uploadUrl = MessageBuilder.uploadMsg(msg);
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					e.printStackTrace();
					DatabaseMessage.updateStatus(msg.getObjectId(),
							Message.Status.SendFailed);
					callback.onError(e);
				} else {
					if (uploadUrl != null) {
						DatabaseMessage.updateContent(msg.getObjectId(),
								uploadUrl);
					}
					sendMsg(msg);
					callback.onSuccess(msg);
				}
			}
		}.execute();
	}

	public Message sendMsg(Message msg) {
		AVMessage avMsg = msg.toAVMessage();
		Session session = ChatService.getSessionForCurrentUser();
		if (roomType == RoomType.Single) {
			session.sendMessage(avMsg);
		} else {
			Group group = session.getGroup(toId);
			group.sendMessage(avMsg);
		}
		return msg;
	}

	public static void resendMsg(Message msg, MessageSendCallback sendCallback) {
		String toId;
		if (msg.getRoomType() == RoomType.Group) {
			String groupId = msg.getConvid();
			toId = groupId;
		} else {
			toId = msg.getToPeerId();
			msg.setRequestReceipt(true);
		}
		DatabaseMessage.updateStatus(msg.getObjectId(),
				Message.Status.SendStart);
		sendCallback.onStart(msg);
		MessageAgent msgAgent = new MessageAgent(msg.getRoomType(), toId);
		msgAgent.uploadAndSendMsg(msg, sendCallback);
	}
}
