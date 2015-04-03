package liu.weiran.chatate.entity;

import com.avos.avoscloud.AVUser;
import liu.weiran.chatate.avobject.ChatGroup;


public class Conversation {
  private Message Message;
  private AVUser toUser;
  private ChatGroup toChatGroup;
  private int unreadCount;

  public Message getMessage() {
    return Message;
  }

  public void setMessage(Message Message) {
    this.Message = Message;
  }

  public AVUser getToUser() {
    return toUser;
  }

  public void setToUser(AVUser toUser) {
    this.toUser = toUser;
  }

  public ChatGroup getToChatGroup() {
    return toChatGroup;
  }

  public void setToChatGroup(ChatGroup toChatGroup) {
    this.toChatGroup = toChatGroup;
  }

  public int getUnreadCount() {
    return unreadCount;
  }

  public void setUnreadCount(int unreadCount) {
    this.unreadCount = unreadCount;
  }
}
