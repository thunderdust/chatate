package liu.weiran.chatate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.Session;
import liu.weiran.chatate.adapters.GroupAdapter;
import liu.weiran.chatate.avobject.ChatGroup;
import liu.weiran.chatate.service.CacheService;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.listener.GroupEventListener;
import liu.weiran.chatate.ui.views.xlist.XListView;
import liu.weiran.chatate.R;
import liu.weiran.chatate.base.MyApplication;
import liu.weiran.chatate.service.receiver.GroupMessageReceiver;
import liu.weiran.chatate.service.GroupService;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupListActivity extends BaseActivity implements
		GroupEventListener, AdapterView.OnItemClickListener,
		XListView.IXListViewListener {
	public static final int GROUP_NAME_REQUEST = 0;
	XListView groupListView;
	List<ChatGroup> chatGroups = new ArrayList<ChatGroup>();
	GroupAdapter groupAdapter;
	String newGroupName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_list);
		findView();
		initList();
		onRefresh();
		initActionBar(MyApplication.mCtx.getString(R.string.group));
		GroupMessageReceiver.addListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.group_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.create) {
			UpdateContentActivity.goActivityForResult(this,
					MyApplication.mCtx.getString(R.string.groupName),
					GROUP_NAME_REQUEST);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void initList() {
		groupAdapter = new GroupAdapter(mCtx, chatGroups);
		groupListView.setPullRefreshEnable(true);
		groupListView.setPullLoadEnable(false);
		groupListView.setXListViewListener(this);
		groupListView.setAdapter(groupAdapter);
		groupListView.setOnItemClickListener(this);
	}

	private void findView() {
		groupListView = (XListView) findViewById(R.id.groupList);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == GROUP_NAME_REQUEST) {
				newGroupName = UpdateContentActivity.getResultValue(data);
				Session session = ChatService.getSessionForCurrentUser();
				Group group = session.getGroup();
				group.join();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onJoined(final Group group) {
		// new Group
		if (newGroupName != null) {
			new NetworkAsyncTask(mCtx) {
				ChatGroup chatGroup;

				@Override
				protected void doInBack() throws Exception {
					chatGroup = GroupService.setNewChatGroupData(
							group.getGroupId(), newGroupName);
				}

				@Override
				protected void onPost(Exception e) {
					newGroupName = null;
					if (e != null) {
						Utils.toast(e.getMessage());
						Utils.printException(e);
					} else {
						chatGroups.add(0, chatGroup);
						CacheService.registerChatGroups((Arrays
								.asList(chatGroup)));
						groupAdapter.notifyDataSetChanged();
					}
				}
			}.execute();
		} else {
			ChatGroup _chatGroup = findChatGroup(group.getGroupId());
			if (_chatGroup == null) {
				throw new RuntimeException("chat group is null");
			}
			ChatActivity.goGroupChat(this, _chatGroup.getObjectId());
		}
	}

	@Override
	public void onMemberUpdate(Group group) {
		groupAdapter.notifyDataSetChanged();
	}

	@Override
	public void onQuit(Group group) {
		onRefresh();
	}

	private ChatGroup findChatGroup(String groupId) {
		for (ChatGroup chatGroup : chatGroups) {
			if (chatGroup.getObjectId().equals(groupId)) {
				return chatGroup;
			}
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GroupMessageReceiver.removeListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ChatGroup chatGroup = (ChatGroup) parent.getAdapter().getItem(position);
		Group group = ChatService.getGroupById(chatGroup.getObjectId());
		group.join();
	}

	@Override
	public void onRefresh() {
		new NetworkAsyncTask(mCtx, false) {
			List<ChatGroup> subChatGroups;

			@Override
			protected void doInBack() throws Exception {
				subChatGroups = GroupService.findGroups();
			}

			@Override
			protected void onPost(Exception e) {
				groupListView.stopRefresh();
				if (e != null) {

				} else {
					chatGroups.clear();
					chatGroups.addAll(subChatGroups);
					CacheService.registerChatGroups(chatGroups);
					groupAdapter.notifyDataSetChanged();
				}
			}
		}.execute();
	}

	@Override
	public void onLoadMore() {

	}
}
