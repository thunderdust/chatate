package liu.weiran.chatate.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.Group;
import liu.weiran.chatate.R;
import liu.weiran.chatate.adapters.GroupUsersAdapter;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.GroupService;
import liu.weiran.chatate.service.listener.GroupEventListener;
import liu.weiran.chatate.service.receiver.GroupMessageReceiver;
import liu.weiran.chatate.util.SimpleNetTask;
import liu.weiran.chatate.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends GroupBaseActivity implements
		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
		GroupEventListener {
	public static final int ADD_MEMBERS = 0;
	private static final int QUIT_GROUP = 1;
	public static List<AVUser> members = new ArrayList<AVUser>();
	GridView usersGrid;
	GroupUsersAdapter usersAdapter;
	boolean isOwner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_detail);
		initData();
		findView();
		initGrid();
		GroupMessageReceiver.addListener(this);
		onGroupUpdate();
	}

	@Override
	void onGroupUpdate() {
		initActionBar(getChatGroup().getGroupDesc());
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem invite = menu.add(0, ADD_MEMBERS, 0, R.string.invite);
		Utils.alwaysShowMenuItem(invite);
		if (isOwner == false) {
			MenuItem quit = menu.add(0, QUIT_GROUP, 0, R.string.quitGroup);
			Utils.alwaysShowMenuItem(quit);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int menuId = item.getItemId();
		if (menuId == ADD_MEMBERS) {
			Utils.goActivity(mCtx, GroupAddMembersActivity.class);
		} else if (menuId == QUIT_GROUP) {
			Group group = GroupService.getGroup(getChatGroup());
			group.quit();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void refresh() {
		new SimpleNetTask(mCtx) {
			List<AVUser> subMembers = new ArrayList<AVUser>();

			@Override
			protected void doInBack() throws Exception {
				subMembers = ChatService.findGroupMembers(getChatGroup());
			}

			@Override
			protected void onSucceed() {
				usersAdapter.clear();
				usersAdapter.addAll(subMembers);
			}
		}.execute();
	}

	private void initGrid() {
		usersAdapter = new GroupUsersAdapter(mCtx, members);
		usersGrid.setAdapter(usersAdapter);
		usersGrid.setOnItemClickListener(this);
		usersGrid.setOnItemLongClickListener(this);
	}

	private void initData() {
		isOwner = GroupService.isGroupOwner(getChatGroup(),
				AVUser.getCurrentUser());
	}

	private void findView() {
		usersGrid = (GridView) findViewById(R.id.usersGrid);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		AVUser user = (AVUser) parent.getAdapter().getItem(position);
		PersonInfoActivity.goPersonInfo(mCtx, user.getObjectId());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		final AVUser user = (AVUser) parent.getAdapter().getItem(position);
		boolean isTheOwner = GroupService.isGroupOwner(getChatGroup(), user);
		if (isTheOwner == false) {
			new AlertDialog.Builder(mCtx)
					.setMessage(R.string.kickTips)
					.setPositiveButton(R.string.sure,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									GroupService.kickMember(getChatGroup(),
											user);
								}
							}).setNegativeButton(R.string.cancel, null).show();
		}
		return true;
	}

	@Override
	public void onJoined(Group group) {

	}

	@Override
	public void onMemberUpdate(Group group) {
		onGroupUpdate();
	}

	@Override
	public void onQuit(Group group) {
		if (group.getGroupId().equals(getChatGroup().getObjectId())) {
			finish();
			if (ChatActivity.ctx != null) {
				ChatActivity.ctx.finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GroupMessageReceiver.removeListener(this);
	}
}
