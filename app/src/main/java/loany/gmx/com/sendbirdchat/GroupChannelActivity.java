package loany.gmx.com.sendbirdchat;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class GroupChannelActivity extends AppCompatActivity {


    String receiver;
    String sender;
    TextView sentText;
    EditText sendText;
    private GroupChannel channel;
    String channelName;
    private static final String identifier = "SendBirdGroupChannelList";
    private PreviousMessageListQuery mPrevMessageListQuery;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private boolean side = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);
        buttonSend = (Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.msgview);



        receiver = getIntent().getStringExtra("User1");
        sender = getIntent().getStringExtra("User2");


        List<String> users = new ArrayList<>();
        users.add(sender);
        users.add(receiver);
        channelName = sender + "-and-" + receiver;
        CreateGroupChannel(users);




        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });



    }

    private boolean sendChatMessage() {
        channel.sendUserMessage(chatText.getText().toString(), new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error.
                    Log.d("Error", "MSG not sent");
                    return;
                }

                //sentText.setText(sendText.getText().toString());

                chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
                chatText.setText("");
                //side = !side;


            }
        });

        return true;
    }




    public void CreateGroupChannel(List<String> users) {
        GroupChannel.createChannelWithUserIds(users, true, channelName, null, null, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // Error.
                    Log.d("Error", "channel not opened!");
                    return;
                }

                channel = groupChannel;
                loadPrevMessages(true, groupChannel);
            }
        });
    }



    @Override
    public void onPause() {
        super.onPause();
        SendBird.removeChannelHandler(identifier);
    }

    @Override
    public void onResume() {
        super.onResume();

        SendBird.addChannelHandler(identifier, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseChannel instanceof GroupChannel) {
                    chatArrayAdapter.add(new ChatMessage(!side, ((UserMessage) baseMessage).getMessage()));
                    chatText.setText("");
                }
            }

        });

    }

    private void loadPrevMessages(final boolean refresh, GroupChannel mchannel) {

        if (mchannel == null) {
            Log.d("Channel", "NULL!!!!!!!!!!!!!!!!!!!!!!!1");
            return;
        }

        if (refresh || mPrevMessageListQuery == null) {
            mPrevMessageListQuery = channel.createPreviousMessageListQuery();
        }

        if (mPrevMessageListQuery.isLoading()) {
            return;
        }

        if (!mPrevMessageListQuery.hasMore()) {
            return;
        }


        mPrevMessageListQuery.load(30, true, new PreviousMessageListQuery.MessageListQueryResult() {
            @Override
            public void onResult(List<BaseMessage> messages, SendBirdException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                for (BaseMessage message : messages) {



                    if (((UserMessage) message).getSender().getUserId() != SendBird.getCurrentUser().getUserId()) {
                        receiver = SendBird.getCurrentUser().getUserId();
                        sender = ((UserMessage) message).getSender().getUserId();
                    } else {
                        sender = SendBird.getCurrentUser().getUserId();
                        receiver = ((UserMessage) message).getSender().getUserId();
                    }

                    Messages msg = new Messages(sender, receiver, ((UserMessage) message).getMessage());
                    msg.save();


                }

                List<Messages> messagesList = Messages.listAll(Messages.class);
                for(Messages messages1 : messagesList) {
                    chatArrayAdapter.add(new ChatMessage(side,messages1.getMessage()));
                }



            }
        });

    }



}