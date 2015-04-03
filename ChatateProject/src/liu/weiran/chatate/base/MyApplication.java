package liu.weiran.chatate.base;

import java.io.File;

import liu.weiran.chatate.R;
import liu.weiran.chatate.avobject.AddRequest;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.avobject.UpdateInfo;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.UpdateService;
import liu.weiran.chatate.ui.activities.SplashActivity;
import liu.weiran.chatate.util.ImageUtils;
import liu.weiran.chatate.util.Logger;
import liu.weiran.chatate.util.Utils;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.PushService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

// Class required to initiate and access LeanCloud SDK
public class MyApplication extends Application {

	public static final String DB_NAME = "chat.db3";
	public static final int DB_VERSION = 4;
	public static boolean isDebugModeOn = false;
	public static MyApplication mCtx;

	@Override
	public void onCreate() {
		super.onCreate();
		mCtx = this;
		/* ???? */
		Utils.fixAsyncTaskBug();

		// String publicID = "";
		// String publicKey = "";
		String appID = "6sf5fan8utg8arwjmf5me9ihxw7wxbp9gwchc9gsytwmqh3o";
		String appKey = "53ig09jggin3r4bq8eag7wpqn7hlqubkglkna341p33lg26c";
		AVOSCloud.initialize(this, appID, appKey);

		AVObject.registerSubclass(AddRequest.class);
		AVObject.registerSubclass(ChatGroup.class);
		AVObject.registerSubclass(UpdateInfo.class);

		AVInstallation.getCurrentInstallation().saveInBackground();
		PushService.setDefaultPushCallback(mCtx, SplashActivity.class);
		AVOSCloud.setDebugLogEnabled(isDebugModeOn);
		if (MyApplication.isDebugModeOn) {
			Logger.level = Logger.VERBOSE;
		} else {
			Logger.level = Logger.NONE;
		}
		initImageLoader(mCtx);
		// initBaidu();
		openStrictMode();
		if (AVUser.getCurrentUser() != null) {
			ChatService.openSession();
		}
	}

	public static MyApplication getInstance() {
		return mCtx;
	}

	public static void initImageLoader(Context ctx) {
		File caheDirectory = StorageUtils.getOwnCacheDirectory(ctx,
				"chatate/Cache");
		ImageLoaderConfiguration config = ImageUtils.getImageLoaderConfig(ctx,
				caheDirectory);
		ImageLoader.getInstance().init(config);
	}

	/* Strictmode: A developer tool for debugging accidental access */
	public void openStrictMode() {
		if (MyApplication.isDebugModeOn) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					// or .detectAll() for all detectable problems
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
					.penaltyLog()
					// .penaltyDeath()
					.build());
		}
	}

	// create table for AVObject subclass
	public static void initTables() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					if (AVUser.getCurrentUser() == null) {
						throw new NullPointerException(
								"Please run it when login");
					}
					// create AddRequest Table
					AddRequest addRequest = new AddRequest();
					addRequest.setRequestSender(AVUser.getCurrentUser());
					addRequest.setRequestReceiver(AVUser.getCurrentUser());
					addRequest.setStatus(AddRequest.STATUS_PENDING);
					addRequest.save();
					addRequest.delete();

					UpdateService.createUpdateInfo();

					// create Avatar Table for default avatar
					Bitmap bitmap = BitmapFactory.decodeResource(
							MyApplication.mCtx.getResources(), R.drawable.head);
					byte[] bs = Utils.getBytesFromBitmap(bitmap);
					AVFile file = new AVFile("head", bs);
					file.save();
					AVObject avatar = new AVObject("Avatar");
					avatar.put("file", file);
					avatar.save();

				} catch (AVException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
