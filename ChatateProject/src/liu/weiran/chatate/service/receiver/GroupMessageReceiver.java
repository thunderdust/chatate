package liu.weiran.chatate.service.receiver;

import android.content.Context;
import com.avos.avoscloud.*;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.service.CacheService;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.listener.GroupEventListener;
import liu.weiran.chatate.service.listener.MessageListener;
import liu.weiran.chatate.util.ChatUtils;
import liu.weiran.chatate.util.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupMessageReceiver extends AVGroupMessageReceiver {
  public static Set<GroupEventListener> groupListeners = new HashSet<GroupEventListener>();
  public static Set<MessageListener> msgListeners = new HashSet<MessageListener>();

  @Override
  public void onJoined(Context context, Group group) {
    for (GroupEventListener listener : groupListeners) {
      listener.onJoined(group);
    }
  }

  @Override
  public void onInviteToGroup(Context context, Group group, String byPeerId) {
    Logger.d("onInviteToGroup " + byPeerId + " groupId=" + group.getGroupId());
  }

  @Override
  public void onInvited(Context context, Group group, List<String> invitedPeers) {
    Logger.d("onInvited " + invitedPeers + " groupId=" + group.getGroupId());
  }

  @Override
  public void onKicked(Context context, Group group, List<String> kickedPeers) {
    Logger.d("kick " + group.getGroupId() + " ids=" + kickedPeers);
  }

  @Override
  public void onMessageSent(Context context, Group group, AVMessage message) {
    Logger.d("onMessageSent");
    ChatUtils.logAVMessage(message);
    ChatService.onMessageSent(message, msgListeners, group);
  }

  @Override
  public void onMessageFailure(Context context, Group group, AVMessage message) {
    Logger.d("onMessageFailure");
    ChatUtils.logAVMessage(message);
    ChatService.onMessageFailure(message, msgListeners, group);
  }

  @Override
  public void onMessage(Context context, Group group, AVMessage msg) {
    Logger.d("onMessage");
    ChatUtils.logAVMessage(msg);
    ChatService.onMessage(context, msg, msgListeners, group);
  }

  @Override
  public void onQuit(Context context, Group group) {
    for (GroupEventListener listener : groupListeners) {
      listener.onQuit(group);
    }
    Logger.d(group.getGroupId() + " quit");
  }

  //签名被拒或聊天室满1000人被拒
  @Override
  public void onReject(Context context, Group group, String op, List<String> targetIds) {
    Logger.d(op + ":" + targetIds + " rejected");
  }

  public void notifyMemberUpdate(final Group group) {
    if (CacheService.isCurrentGroup(group)) {
      final ChatGroup chatGroup = CacheService.lookupChatGroup(group.getGroupId());
      chatGroup.fetchInBackground(new GetCallback<AVObject>() {
        @Override
        public void done(AVObject object, AVException e) {
          if (e != null) {
            e.printStackTrace();
          } else {
            CacheService.registerChatGroup(chatGroup);
            for (GroupEventListener listener : groupListeners) {
              listener.onMemberUpdate(group);
            }
          }
        }
      });
    }
  }

  @Override
  public void onMemberJoin(Context context, Group group, List<String> joinedPeerIds) {
    notifyMemberUpdate(group);
    Logger.d(joinedPeerIds + " join " + group.getGroupId());
  }

  @Override
  public void onMemberLeft(Context context, Group group, List<String> leftPeerIds) {
    notifyMemberUpdate(group);
    Logger.d(leftPeerIds + " left " + group.getGroupId());
  }

  @Override
  public void onError(Context context, Group group, Throwable e) {
    //Utils.toast(context, e.getMessage());
    e.printStackTrace();
    //ChatService.onMessageError(e, msgListeners);
  }

  public static void addMsgListener(MessageListener listener) {
    msgListeners.add(listener);
  }

  public static void removeMsgListener(MessageListener listener) {
    msgListeners.remove(listener);
  }

  public static void addListener(GroupEventListener listener) {
    groupListeners.add(listener);
  }

  public static void removeListener(GroupEventListener listener) {
    groupListeners.remove(listener);
  }
}
