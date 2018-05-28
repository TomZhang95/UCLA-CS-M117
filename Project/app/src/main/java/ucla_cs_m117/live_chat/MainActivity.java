package ucla_cs_m117.live_chat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String account_name;
    private Button add_room;
    private EditText room_name;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_rooms = new ArrayList<>();
    private String user_name;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    private RelativeLayout activity_main;
    private String password, input_password;
    private Map<String, String> name_to_password = new HashMap<>();


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main,"You have been signed out.", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                Snackbar.make(activity_main,"Successfully signed in.Welcome!", Snackbar.LENGTH_SHORT).show();
                require_user_name();
            }
            else{
                Snackbar.make(activity_main,"We couldn't sign you in.Please try again later", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (RelativeLayout)findViewById(R.id.activity_main);
        add_room = (Button)findViewById(R.id.create_room);
        room_name = (EditText)findViewById(R.id.room_name);
        listView = (ListView)findViewById(R.id.room_list);
        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list_of_rooms);
        listView.setAdapter(arrayAdapter);


        //Listener for create room button
        add_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (room_name.getText().toString().lastIndexOf('_') != -1) {
                    Toast.makeText(MainActivity.this, "Room name cannot contain '_', please try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                require_password();
            }
        });

        //Show added room on screen by using hash set
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                Set<String> set = new HashSet<String>();
                while (i.hasNext()) {
                    String name_with_password = ((DataSnapshot)i.next()).getKey();
                    String tmp = name_with_password.substring(0, name_with_password.indexOf('_'));
                    set.add(tmp);
                    name_to_password.put(tmp, name_with_password);
                }
                list_of_rooms.clear();
                list_of_rooms.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Listener for clicking on chat rooms
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                check_password(((TextView) view).getText().toString());
            }
        });

        //Check if not sign-in then navigate Signin page
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),1);
        }
        else
        {
            Snackbar.make(activity_main,"Welcome "+FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            require_user_name();
        }


    }


    //Get user name
    private void require_user_name() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Enter Name:");

        final EditText input_field = new EditText(this);

        builder.setView(input_field);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user_name = input_field.getText().toString();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                require_user_name();
            }
        });

        builder.show();
    }

    private void require_password() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Create Chat Room Password:");

        final EditText input_field = new EditText(this);

        builder.setView(input_field);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> map = new HashMap<String, Object>();
                password = input_field.getText().toString();
                String tmp = room_name.getText().toString() + '_' + password;
                map.put(tmp, "");
                root.updateChildren(map);
                room_name.setText("");
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                room_name.setText("");
            }
        });

        builder.show();
    }

    private void check_password(final String clicked_room_name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Enter password:");

        final EditText input_field = new EditText(this);

        builder.setView(input_field);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input_password = input_field.getText().toString();
                String full_room_name = name_to_password.get(root.child(clicked_room_name).getKey());
                String true_password = full_room_name.substring(full_room_name.indexOf('_')+1);

                if (input_password.equals(true_password)) {
                    Intent intent = new Intent(getApplicationContext(), ChatRoom.class);
                    intent.putExtra("room_name", full_room_name);
                    intent.putExtra("user_name", user_name);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "Password Incorrect, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
