package liu.weiran.chatate.ui.activities;

import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.service.CacheService;


public abstract class GroupBaseActivity extends BaseActivity {
  public static ChatGroup getChatGroup() {
    return CacheService.getCurrentChatGroup();
  }

  abstract void onGroupUpdate();
}
