package ucla_cs_m117.live_chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.r0adkll.slidr.Slidr;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ChatRoom extends AppCompatActivity {

    private Button send_button;
    private EditText input_msg;
    private TextView chat_conversation;
    private String user_name, full_room_name;
    private DatabaseReference root;
    private String msg_key;
    private PlaceDetectionClient mPlaceDetectionClient;
    private String location;
    private Map<String, Object> msg_map = new HashMap<String, Object>();
    private DatabaseReference msg_root;

    private LocationManager locationManager;
    private Location coord;
    private Map<String, List<Double>> user_latlon = new HashMap<String, List<Double>>();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_map) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            SerializableMap tmpmap=new SerializableMap();
            Bundle bundle=new Bundle();
            tmpmap.setMap(user_latlon);
            bundle.putSerializable("user_latlon", tmpmap);
            intent.putExtras(bundle);
            intent.putExtra("user_name", user_name);

            startActivity(intent);
            //Set the animation which move the activity from right to left
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        send_button = findViewById(R.id.send_button);
        input_msg = findViewById(R.id.input_msg);
        chat_conversation = findViewById(R.id.msg_text);

        //Get user name and room name from MainActivity
        user_name = getIntent().getExtras().get("user_name").toString();
        full_room_name = getIntent().getExtras().get("room_name").toString();
        setTitle("Room - " + full_room_name.substring(0, full_room_name.indexOf('_')));


        root = FirebaseDatabase.getInstance().getReference().child(full_room_name);

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
        Slidr.attach(this);
    }

    private String chat_msg, chat_user_name, chat_time, chat_location;

    private void append_chat_conversation(DataSnapshot datasnapshot) {
        Iterator i;
        for (i = datasnapshot.getChildren().iterator(); i.hasNext();) {
            Double tmp_lat, tmp_lon;
            List<Double> tmp = new ArrayList<>();
            tmp_lat = (Double)((DataSnapshot)i.next()).getValue();
            chat_location = (String)((DataSnapshot)i.next()).getValue();
            tmp_lon = (Double) ((DataSnapshot)i.next()).getValue();
            chat_msg = (String)((DataSnapshot)i.next()).getValue();
            chat_user_name = (String)((DataSnapshot)i.next()).getValue();
            chat_time = (String)((DataSnapshot)i.next()).getValue();
            chat_conversation.append(chat_user_name + ": "+ chat_time + " AT " + chat_location + "\n" + chat_msg + "\n\n");
            tmp.add(Double.valueOf(tmp_lat));
            tmp.add(Double.valueOf(tmp_lon));
            user_latlon.put(chat_user_name, tmp);
        }
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getApplicationContext());

        if (PermissionChecker.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Get plaice name
            com.google.android.gms.tasks.Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<PlaceLikelihoodBufferResponse> task) {
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    Iterator<PlaceLikelihood> placeLikelihood = likelyPlaces.iterator();
                    location = (String) placeLikelihood.next().getPlace().getName();
                    if (location == null)
                        location = "";

                    Double lat, lon;
                    //Get latitude and longitude by LocationManager
                    if (PermissionChecker.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        coord = locationManager.getLastKnownLocation("gps");
                        if(coord == null)
                            coord = locationManager.getLastKnownLocation("network");
                    }
                    else {
                        ActivityCompat.requestPermissions(ChatRoom.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                1);
                        coord = locationManager.getLastKnownLocation("gps");
                        if(coord == null)
                            coord = locationManager.getLastKnownLocation("network");
                    }
                    lat = coord.getLatitude();
                    lon = coord.getLongitude();


                    msg_map.put("lat", lat);
                    msg_map.put("lon", lon);
                    msg_map.put("location", location);
                    msg_root.updateChildren(msg_map);
                    likelyPlaces.release();
                }
            });
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            getLocation();
        }
    }
}


