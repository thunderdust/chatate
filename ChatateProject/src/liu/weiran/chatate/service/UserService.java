package liu.weiran.chatate.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.base.Name;
import liu.weiran.chatate.util.ImageUtils;
import liu.weiran.chatate.util.Logger;
import liu.weiran.chatate.util.Utils;

import android.text.TextUtils;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.nostra13.universalimageloader.core.ImageLoader;

public class UserService {

	public static final int ORDER_UPDATED_AT = 1;
	public static final int ORDER_DISTANCE = 0;
	public static final int PAGE_SIZE = 10;
	public static ImageLoader imageLoader = ImageLoader.getInstance();

	public static AVUser findUser(String id) throws AVException {
		AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
		// search user in cache if no network connection
		q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
		return q.get(id);
	}

	public static List<AVUser> getFriends() throws AVException {
		AVUser currentUser = AVUser.getCurrentUser();
		AVRelation<AVUser> friendRelation = currentUser
				.getRelation(User.FRIENDS);
		friendRelation.setTargetClass("_User");
		AVQuery<AVUser> query = friendRelation.getQuery(AVUser.class);
		query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
		List<AVUser> friends = query.find();
		return friends;
	}

	public static void displayAvatar(String imageUrl, ImageView avatarView) {
		imageLoader.displayImage(imageUrl, avatarView,
				ImageUtils.avatarImageOptions);
	}

	public static void displayAvatar(AVUser user, ImageView avatarView) {
		if (user != null) {
			String avatarUrl = User.getAvatarUrl(user);
			if (TextUtils.isEmpty(avatarUrl) == false) {
				displayAvatar(avatarUrl, avatarView);
			}
		}
	}

	public static List<AVUser> searchUser(String searchName, int skip)
			throws AVException {
		AVQuery<AVUser> q = AVUser.getQuery(AVUser.class);
		q.whereContains(User.USER_NAME, searchName);
		q.limit(PAGE_SIZE);
		q.skip(skip);
		AVUser user = AVUser.getCurrentUser();
		List<String> friendIds = new ArrayList<String>(
				CacheService.getfriendIDList());
		friendIds.add(user.getObjectId());
		q.whereNotContainedIn(Name.OBJECT_ID, friendIds);
		q.orderByDescending(Name.UPDATED_AT);
		q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
		List<AVUser> users = q.find();
		CacheService.registerBatchUser(users);
		return users;
	}

	public static List<AVUser> findNearbyPeople(int orderType, int skip,
			int limit) throws AVException {
		PreferenceMap preferenceMap = PreferenceMap
				.getCurUserPreference(MyApplication.mCtx);
		AVGeoPoint geoPoint = preferenceMap.getLocation();
		if (geoPoint == null) {
			Logger.i("geo point is null");
			return new ArrayList<AVUser>();
		}
		AVQuery<AVUser> q = AVObject.getQuery(AVUser.class);
		AVUser user = AVUser.getCurrentUser();
		q.whereNotEqualTo(Name.OBJECT_ID, user.getObjectId());
		if (orderType == ORDER_DISTANCE) {
			q.whereNear(User.LOCATION, geoPoint);
		} else {
			q.orderByDescending(Name.UPDATED_AT);
		}
		q.skip(skip);
		q.limit(limit);
		q.setCachePolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
		List<AVUser> users = q.find();
		CacheService.registerBatchUser(users);
		return users;
	}

	public static void saveSex(User.Gender gender, SaveCallback saveCallback) {
		AVUser user = AVUser.getCurrentUser();
		User.setGender(user, gender);
		user.saveInBackground(saveCallback);
	}

	public static List<String> transformIds(List<? extends AVObject> objects) {
		List<String> ids = new ArrayList<String>();
		for (AVObject o : objects) {
			ids.add(o.getObjectId());
		}
		return ids;
	}

	public static AVUser signUp(String name, String password)
			throws AVException {
		AVUser user = new AVUser();
		user.setUsername(name);
		user.setPassword(password);
		user.signUp();
		return user;
	}

	public static void cacheUserIfNone(String userId) throws AVException {
		if (CacheService.lookupUser(userId) == null) {
			CacheService.registerUserCache(findUser(userId));
		}
	}

	public static void saveAvatar(String path) throws IOException, AVException {
		AVUser user = AVUser.getCurrentUser();
		final AVFile file = AVFile.withAbsoluteLocalPath(user.getUsername(),
				path);
		file.save();
		user.put(User.AVATAR, file);
		user.save();
		user.fetch();
	}

	public static void updateUserInfo() {
		AVUser user = AVUser.getCurrentUser();
		if (user != null) {
			AVInstallation installation = AVInstallation
					.getCurrentInstallation();
			if (installation != null) {
				user.put(User.INSTALLATION, installation);
				user.saveInBackground();
			}
		}
	}

	public static void updateUserLocation() {
		PreferenceMap preferenceMap = PreferenceMap
				.getCurUserPreference(MyApplication.mCtx);
		AVGeoPoint lastLocation = preferenceMap.getLocation();
		if (lastLocation != null) {
			final AVUser user = AVUser.getCurrentUser();
			final AVGeoPoint location = user.getAVGeoPoint(User.LOCATION);
			if (location == null
					|| !Utils.isDoubleEqual(location.getLatitude(),
							lastLocation.getLatitude())
					|| !Utils.isDoubleEqual(location.getLongitude(),
							lastLocation.getLongitude())) {
				user.put(User.LOCATION, lastLocation);
				user.saveInBackground(new SaveCallback() {
					@Override
					public void done(AVException e) {
						if (e != null) {
							e.printStackTrace();
						} else {
							Logger.v("lastLocation save "
									+ user.getAVGeoPoint(User.LOCATION));
						}
					}
				});
			}
		}
	}

}
