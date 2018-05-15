package ucla_cs_m117.live_chat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bolts.Task;

public class ChatRoom extends AppCompatActivity {

    private Button send_button;
    private EditText input_msg;
    private TextView chat_conversation;
    private String user_name, room_name;
    private DatabaseReference root;
    private String msg_key;
    private PlaceDetectionClient mPlaceDetectionClient;
    private String location;
    private Map<String, Object> msg_map = new HashMap<String, Object>();
    private DatabaseReference msg_root;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        send_button = findViewById(R.id.send_button);
        input_msg = findViewById(R.id.input_msg);
        chat_conversation = findViewById(R.id.msg_text);

        //Get user name and room name from MainActivity
        user_name = getIntent().getExtras().get("user_name").toString();
        room_name = getIntent().getExtras().get("room_name").toString();
        setTitle("Room - " + room_name);


        root = FirebaseDatabase.getInstance().getReference().child(room_name);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> key_map = new HashMap<String, Object>();

                msg_key = root.push().getKey();
                root.updateChildren(key_map);

                msg_root = root.child(msg_key);

                getLocation();

                msg_map.put("name", user_name);
                msg_map.put("msg", input_msg.getText().toString());
                msg_map.put("time", DateFormat.format("MM-dd-yyyy (HH:mm:ss)", new Date().getTime()));
                input_msg.setText("");

            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String chat_msg, chat_user_name, chat_time, chat_location;

    private void append_chat_conversation(DataSnapshot datasnapshot) {
        Iterator i;
        for (i = datasnapshot.getChildren().iterator(); i.hasNext();) {
            chat_location = (String)((DataSnapshot)i.next()).getValue();
            chat_msg = (String)((DataSnapshot)i.next()).getValue();
            chat_user_name = (String)((DataSnapshot)i.next()).getValue();
            chat_time = (String)((DataSnapshot)i.next()).getValue();
            chat_conversation.append(chat_user_name + ": "+ chat_time + " AT " + chat_location + "\n" + chat_msg + "\n\n");
        }
    }

    private void getLocation() {
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getApplicationContext());

        if (PermissionChecker.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            com.google.android.gms.tasks.Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<PlaceLikelihoodBufferResponse> task) {
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    Iterator<PlaceLikelihood> placeLikelihood = likelyPlaces.iterator();
                    location = (String) placeLikelihood.next().getPlace().getName();
                    if (location == null)
                        location = "";
                    msg_map.put("location", location);
                    msg_root.updateChildren(msg_map);
                    likelyPlaces.release();
                }
            });
        }
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
    }
}

