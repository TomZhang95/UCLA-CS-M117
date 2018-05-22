package ucla_cs_m117.live_chat;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

class SerializableMap implements Serializable{
    private Map<String,List<Double>> map;

    public Map<String, List<Double>> getMap() {
        return map;
    }

    public void setMap(Map<String, List<Double>> map) {
        this.map = map;
    }
}
