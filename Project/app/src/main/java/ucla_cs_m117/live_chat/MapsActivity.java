package ucla_cs_m117.live_chat;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String user_name;
    private GoogleMap mMap;
    private Map<String, List<Double>> user_latlon = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        user_name = getIntent().getExtras().get("user_name").toString();

        Bundle bundle = getIntent().getExtras();
        SerializableMap serializableMap = (SerializableMap) bundle.get("user_latlon");
        user_latlon = serializableMap.getMap();

        Iterator<Map.Entry<String, List<Double>>> i = user_latlon.entrySet().iterator();
        LatLng my_latlng = new LatLng(34.074949, -118.441318);

        while (i.hasNext()) {
            double latitude, longitude;
            // Add a marker in Sydney and move the camera
            Map.Entry<String, List<Double>> entry = i.next();
            String name = entry.getKey();
            List<Double> latlon = entry.getValue();

            if (latlon.size() == 2) {
                latitude = latlon.get(0);
                longitude = latlon.get(1);

                MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(name);
                if (name.equals(user_name)) {
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.my_loc_icon));
                    my_latlng = new LatLng(latitude, longitude);
                }
                else {
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_icon));
                }
                mMap.addMarker(marker);
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(my_latlng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(my_latlng, 12.0f));
    }
}