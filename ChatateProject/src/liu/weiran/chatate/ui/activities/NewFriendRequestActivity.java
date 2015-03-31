package liu.weiran.chatate.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import liu.weiran.chatate.R;

import liu.weiran.chatate.adapters.NewFriendRequestAdapter;
import liu.weiran.chatate.avobject.AddRequest;
import liu.weiran.chatate.avobject.User;
import liu.weiran.chatate.service.AddRequestService;
import liu.weiran.chatate.service.PreferenceMap;
import liu.weiran.chatate.util.NetworkAsyncTask;
import liu.weiran.chatate.util.SimpleNetTask;
import liu.weiran.chatate.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class NewFriendRequestActivity extends BaseActivity implements
		OnItemLongClickListener {
	ListView listview;
	NewFriendRequestAdapter adapter;
	List<AddRequest> addRequests = new ArrayList<AddRequest>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_friend_item);
		initView();
		refresh();
	}

	private void refresh() {
		new NetworkAsyncTask(mCtx) {
			List<AddRequest> subAddRequests;

			@Override
			protected void doInBack() throws Exception {
				subAddRequests = AddRequestService.findAddRequests();
				List<AddRequest> filters = new ArrayList<AddRequest>();
				for (AddRequest addRequest : subAddRequests) {
					if (addRequest.getRequestSender() != null) {
						filters.add(addRequest);
					}
				}
				subAddRequests = filters;
			}

			@Override
			protected void onPost(Exception e) {
				if (e != null) {
					e.printStackTrace();
					Utils.toast(mCtx, R.string.pleaseCheckNetwork);
				} else {
					PreferenceMap preferenceMap = new PreferenceMap(mCtx,
							User.getCurrentUserID());
					preferenceMap.setAddRequestN(subAddRequests.size());
					adapter.addAll(subAddRequests);
				}
			}
		}.execute();
	}

	private void initView() {
		initActionBar(R.string.new_friends);
		listview = (ListView) findViewById(R.id.newfriendList);
		listview.setOnItemLongClickListener(this);
		adapter = new NewFriendRequestAdapter(this, addRequests);
		listview.setAdapter(adapter);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			int position, long arg3) {
		AddRequest invite = (AddRequest) adapter.getItem(position);
		showDeleteDialog(position, invite);
		return true;
	}

	public void showDeleteDialog(final int position, final AddRequest addRequest) {
		new AlertDialog.Builder(mCtx)
				.setMessage(R.string.deleteFriendRequest)
				.setPositiveButton(R.string.sure,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								deleteAddRequest(position, addRequest);
							}
						}).setNegativeButton(R.string.cancel, null).show();
	}

	private void deleteAddRequest(final int position,
			final AddRequest addRequest) {
		new SimpleNetTask(mCtx) {
			@Override
			public void onSucceed() {
				adapter.remove(position);
			}

			@Override
			protected void doInBack() throws Exception {
				addRequest.delete();
			}
		}.execute();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
