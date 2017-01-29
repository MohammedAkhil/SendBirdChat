package loany.gmx.com.sendbirdchat;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
    List<Messages> prevMessages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);
        sentText = (TextView) findViewById(R.id.sentMessage);
        sendText = (EditText) findViewById(R.id.sendMessage);

        receiver = getIntent().getStringExtra("User1");
        sender = getIntent().getStringExtra("User2");

        List<String> users = new ArrayList<>();
        users.add(sender);
        users.add(receiver);

        channelName = sender + "-and-" + receiver;

        CreateGroupChannel(users);


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

    public void send(View view) {
        channel.sendUserMessage(sendText.getText().toString(), new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // Error.
                    Log.d("Error", "MSG not sent");
                    return;
                }

                sentText.setText(sendText.getText().toString());
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
                    //prevMessages.add(msg);




                }
                Messages.saveInTx();
                Log.d("msg","hello boss");
            }
        });

    }



}