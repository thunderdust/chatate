package liu.weiran.chatate.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.json.JSONException;

import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.database.DatabaseHelper;
import liu.weiran.chatate.database.DatabaseMessage;
import liu.weiran.chatate.entity.Conversation;
import liu.weiran.chatate.entity.Message;
import liu.weiran.chatate.entity.RoomType;
import liu.weiran.chatate.service.listener.MessageListener;
import liu.weiran.chatate.service.receiver.MessageReceiver;
import liu.weiran.chatate.ui.activities.ChatActivity;
import liu.weiran.chatate.util.ChatUtils;
import liu.weiran.chatate.util.Logger;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.avos.avoscloud.*;

public class ChatService {

	private static final int REPLY_NOTIFY_INDEX = 1;
	private static final long NOTIFY_PERIOD = 1000;
	static long lastNotifyTime = 0;

	public static <T extends AVUser> String getPeerID(T user) {
		return user.getObjectId();
	}

	public static String getSelfID() {
		return getPeerID(AVUser.getCurrentUser());
	}

	// ???
	public static <T extends AVUser> void withUsersToWatch(List<T> users,
			boolean watch) {
		List<String> peerIds = new ArrayList<String>();
		for (AVUser user : users) {
			peerIds.add(getPeerID(user));
		}
		String selfId = getSelfID();
		Session session = SessionManager.getInstance(selfId);
		if (watch) {
			session.watchPeers(peerIds);
		} else {
			session.unwatchPeers(peerIds);
		}
	}

	public static <T extends com.avos.avoscloud.AVUser> void withUserToWatch(
			T user, boolean watch) {
		List<T> users = new ArrayList<T>();
		users.add(user);
		withUsersToWatch(users, watch);
	}

	public static Session getSessionForCurrentUser() {
		return SessionManager.getInstance(getSelfID());
	}

	public static void openSession() {
		Session session = getSessionForCurrentUser();
		session.setSignatureFactory(new SignatureFactory());
		session.open(new LinkedList<String>());
	}

	public static List<Conversation> getConversationsAndCache()
			throws AVException {
		List<Message> msgs = DatabaseMessage.getRecentMessages(User
				.getCurrentUserID());
		cacheUserOrChatGroup(msgs);
		ArrayList<Conversation> conversations = new ArrayList<Conversation>();
		DatabaseHelper dbHelper = new DatabaseHelper(MyApplication.mCtx,
				MyApplication.DB_NAME, MyApplication.DB_VERSION);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		for (Message msg : msgs) {
			Conversation conversation = new Conversation();
			if (msg.getRoomType() == RoomType.Single) {
				String chatUserId = msg.getOtherId();
				conversation.setToUser(CacheService.lookupUser(chatUserId));
			} else {
				conversation.setToChatGroup(CacheService.lookupChatGroup(msg
						.getConvid()));
			}
			int unreadCount = DatabaseMessage.getUnreadCount(db,
					msg.getConvid());
			conversation.setMessage(msg);
			conversation.setUnreadCount(unreadCount);
			conversations.add(conversation);
		}
		db.close();
		return conversations;
	}

	public static void cacheUserOrChatGroup(List<Message> msgs)
			throws AVException {
		Set<String> userIds = new HashSet<String>();
		Set<String> groupIds = new HashSet<String>();
		for (Message msg : msgs) {
			if (msg.getRoomType() == RoomType.Single) {
				userIds.add(msg.getToPeerId());
			} else {
				String groupId = msg.getConvid();
				groupIds.add(groupId);
			}
			userIds.add(msg.getFromPeerId());
		}
		CacheService.cacheUserAndGet(new ArrayList<String>(userIds));
		GroupService.cacheChatGroups(new ArrayList<String>(groupIds));
	}

	public static void closeSession() {
		Session session = ChatService.getSessionForCurrentUser();
		session.close();
	}

	public static Group getGroupById(String groupId) {
		return getSessionForCurrentUser().getGroup(groupId);
	}

	public static void notifyMsg(Context context, Message msg, Group group)
			throws JSONException {
		if (System.currentTimeMillis() - lastNotifyTime < NOTIFY_PERIOD) {
			return;
		} else {
			lastNotifyTime = System.currentTimeMillis();
		}
		int icon = context.getApplicationInfo().icon;
		Intent intent;
		if (group == null) {
			intent = ChatActivity.getUserChatIntent(context,
					msg.getFromPeerId());
		} else {
			intent = ChatActivity.getGroupChatIntent(context,
					group.getGroupId());
		}
		// why Random().nextInt()
		// http://stackoverflow.com/questions/13838313/android-onnewintent-always-receives-same-intent
		PendingIntent pend = PendingIntent.getActivity(context,
				new Random().nextInt(), intent, 0);
		Notification.Builder builder = new Notification.Builder(context);
		CharSequence notifyContent = msg.getNotifyContent();
		CharSequence username = msg.getFromName();
		builder.setContentIntent(pend).setSmallIcon(icon)
				.setWhen(System.currentTimeMillis())
				.setTicker(username + "\n" + notifyContent)
				.setContentTitle(username).setContentText(notifyContent)
				.setAutoCancel(true);
		NotificationManager man = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = builder.getNotification();
		PreferenceMap preferenceMap = PreferenceMap
				.getCurUserPreference(context);
		if (preferenceMap.isVoiceNotify()) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if (preferenceMap.isVibrateNotify()) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		man.notify(REPLY_NOTIFY_INDEX, notification);
	}

	public static void onMessage(Context context, AVMessage avMsg,
			Set<MessageListener> listeners, Group group) {
		final Message msg = Message.fromAVMessage(avMsg);
		String convid;
		if (group == null) {
			String selfId = getSelfID();
			msg.setToPeerId(selfId);
			convid = ChatUtils.convid(selfId, msg.getFromPeerId());
			msg.setRoomType(RoomType.Single);
		} else {
			convid = group.getGroupId();
			msg.setRoomType(RoomType.Group);
		}
		msg.setStatus(Message.Status.SendReceived);
		msg.setReadStatus(Message.ReadStatus.Unread);
		msg.setConvid(convid);
		handleReceivedMsg(context, msg, listeners, group);
	}

	public static void handleReceivedMsg(final Context context,
			final Message msg, final Set<MessageListener> listeners,
			final Group group) {
		new NetworkAsyncTask(context, false) {
			@Override
			protected void doInBack() throws Exception {
				if (msg.getType() == Message.Type.Audio) {
					File file = new File(msg.getAudioPath());
					String url = msg.getContent();
					Utils.downloadUncachedFile(url, file);
				}
				if (group != null) {
					GroupService.cacheChatGroupIfNone(group.getGroupId());
				}
				String fromId = msg.getFromPeerId();
				UserService.cacheUserIfNone(fromId);
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					Utils.toast(context, liu.weiran.chatate.R.string.badNetwork);
				} else {
					DatabaseMessage.insertMessage(msg);
					String otherId = getOtherId(msg.getFromPeerId(), group);
					boolean done = false;
					for (MessageListener listener : listeners) {
						if (listener.onMessageUpdate(otherId)) {
							done = true;
							break;
						}
					}
					if (!done) {
						if (AVUser.getCurrentUser() != null) {
							PreferenceMap preferenceMap = PreferenceMap
									.getCurUserPreference(context);
							if (preferenceMap.isNotifyWhenNews()) {
								try {
									notifyMsg(context, msg, group);
								} catch (JSONException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
		}.execute();
	}

	private static String getOtherId(String otherId, Group group) {
		assert otherId != null || group != null;
		if (group != null) {
			return group.getGroupId();
		} else {
			return otherId;
		}
	}

	public static void onMessageSent(AVMessage avMsg,
			Set<MessageListener> listeners, Group group) {
		Message msg = Message.fromAVMessage(avMsg);
		DatabaseMessage.updateStatusAndTimestamp(msg.getObjectId(),
				Message.Status.SendSucceed, msg.getTimestamp());
		String otherId = getOtherId(msg.getToPeerId(), group);
		for (MessageListener MessageListener : listeners) {
			if (MessageListener.onMessageUpdate(otherId)) {
				break;
			}
		}
	}

	public static void onMessageFailure(AVMessage avMsg,
			Set<MessageListener> MessageListeners, Group group) {
		Message msg = Message.fromAVMessage(avMsg);
		DatabaseMessage.updateStatus(msg.getObjectId(),
				Message.Status.SendFailed);
		String otherId = getOtherId(msg.getToPeerId(), group);
		for (MessageListener MessageListener : MessageListeners) {
			if (MessageListener.onMessageUpdate(otherId)) {
				break;
			}
		}
	}

	public static void onMessageError(Throwable throwable,
			Set<MessageListener> MessageListeners) {
		String errorMsg = throwable.getMessage();
		Logger.d("error " + errorMsg);
		if (errorMsg != null && errorMsg.startsWith("{")) {
			AVMessage avMsg = new AVMessage(errorMsg);
			// onMessageFailure(avMsg, MessageListeners, group);
		}
	}

	public static List<AVUser> findGroupMembers(ChatGroup chatGroup)
			throws AVException {
		List<String> members = chatGroup.getMembers();
		return CacheService.findUsers(members);
	}

	public static void cancelNotification(Context ctx) {
		Utils.cancelNotification(ctx, REPLY_NOTIFY_INDEX);
	}

	public static void onMessageDelivered(AVMessage avMsg,
			Set<MessageListener> listeners) {
		Message msg = Message.fromAVMessage(avMsg);
		DatabaseMessage.updateStatus(msg.getObjectId(),
				Message.Status.SendReceived);
		String otherId = msg.getToPeerId();
		for (MessageListener listener : listeners) {
			if (listener.onMessageUpdate(otherId)) {
				break;
			}
		}
	}

	public static boolean isSessionPaused() {
		return MessageReceiver.isSessionPaused();
	}

}
