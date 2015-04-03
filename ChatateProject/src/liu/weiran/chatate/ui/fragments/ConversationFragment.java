package liu.weiran.chatate.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.InjectView;
import liu.weiran.chatate.R;
import liu.weiran.chatate.adapters.RecentMessageAdapter;
import liu.weiran.chatate.entity.Conversation;
import liu.weiran.chatate.entity.RoomType;
import liu.weiran.chatate.service.ChatService;
import liu.weiran.chatate.service.listener.MessageListener;
import liu.weiran.chatate.service.receiver.GroupMessageReceiver;
import liu.weiran.chatate.service.receiver.MessageReceiver;
import liu.weiran.chatate.ui.activities.ChatActivity;
import liu.weiran.chatate.ui.views.BaseListView;

import java.util.List;

public class ConversationFragment extends BaseFragment implements
		MessageListener {

	@InjectView(R.id.convList)
	BaseListView<Conversation> listView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.message_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
		onRefresh();
	}

	private void onRefresh() {
		listView.onRefresh();
	}

	private void initView() {
		headerLayout.showTitle(R.string.messages);
		listView = (BaseListView<Conversation>) getView().findViewById(
				R.id.convList);
		listView.init(new BaseListView.DataInterface<Conversation>() {
			@Override
			public List<Conversation> getDatas(int skip, int limit,
					List<Conversation> currentDatas) throws Exception {
				return ChatService.getConversationsAndCache();
			}

			@Override
			public void onItemSelected(Conversation item) {
				if (item.getMessage().getRoomType() == RoomType.Single) {
					ChatActivity.goUserChat(getActivity(), item.getToUser()
							.getObjectId());
				} else {
					ChatActivity.goGroupChat(getActivity(), item
							.getToChatGroup().getObjectId());
				}
			}

		}, new RecentMessageAdapter(getActivity()));

	}

	private boolean hidden;

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			onRefresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			onRefresh();
		}
		GroupMessageReceiver.addMsgListener(this);
		MessageReceiver.addMsgListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MessageReceiver.removeMsgListener(this);
		GroupMessageReceiver.removeMsgListener(this);
	}

	@Override
	public boolean onMessageUpdate(String otherId) {
		onRefresh();
		return false;
	}
}
