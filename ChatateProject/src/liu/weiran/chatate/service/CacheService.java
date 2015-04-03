package liu.weiran.chatate.service;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Group;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.base.Name;
import java.util.*;

/*Cached information about user, groups that can be searched in a query request*/
public class CacheService {

	private static Map<String, AVUser> cache_user = new HashMap<String, AVUser>();
	private static Map<String, ChatGroup> cache_group_chat = new HashMap<String, ChatGroup>();
	private static List<String> friendIDList = new ArrayList<String>();
	private static ChatGroup current_group;

	public static AVUser lookupUser(String userId) {
		return cache_user.get(userId);
	}

	public static void registerUserCache(String userId, AVUser user) {
		cache_user.put(userId, user);
	}

	public static void registerUserCache(AVUser user) {
		registerUserCache(user.getObjectId(), user);
	}

	public static void registerBatchUser(List<AVUser> users) {
		for (AVUser user : users) {
			registerUserCache(user);
		}
	}

	public static ChatGroup lookupChatGroup(String groupId) {
		return cache_group_chat.get(groupId);
	}

	public static void registerChatGroups(List<ChatGroup> chatGroups) {
		for (ChatGroup chatGroup : chatGroups) {
			registerChatGroup(chatGroup);
		}
	}

	public static void registerChatGroup(ChatGroup chatGroup) {
		cache_group_chat.put(chatGroup.getObjectId(), chatGroup);
	}

	public static List<String> getfriendIDList() {
		return friendIDList;
	}

	public static void setfriendIDList(List<String> friendIDList) {
		CacheService.friendIDList = Collections.unmodifiableList(friendIDList);
	}

	public static ChatGroup getCurrentChatGroup() {
		return current_group;
	}

	public static void setCurrentChatGroup(ChatGroup currentChatGroup) {
		CacheService.current_group = currentChatGroup;
	}

	public static boolean isCurrentGroup(Group group) {
		if (getCurrentChatGroup() != null
				&& getCurrentChatGroup().getObjectId().equals(
						group.getGroupId())) {
			return true;
		} else {
			return false;
		}
	}

	public static List<AVUser> cacheUserAndGet(List<String> ids)
			throws AVException {
		Set<String> uncachedIds = new HashSet<String>();
		for (String id : ids) {
			if (lookupUser(id) == null) {
				uncachedIds.add(id);
			}
		}
		findUsers(new ArrayList<String>(uncachedIds));
		List<AVUser> users = new ArrayList<AVUser>();
		for (String id : ids) {
			users.add(lookupUser(id));
		}
		return users;
	}

	public static List<AVUser> findUsers(List<String> userIds)
			throws AVException {
		if (userIds.size() <= 0) {
			return new ArrayList<AVUser>();
		}
		AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
		q.whereContainedIn(Name.OBJECT_ID, userIds);
		q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
		List<AVUser> users = q.find();
		registerBatchUser(users);
		return users;
	}
}
