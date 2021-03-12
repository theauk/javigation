package bfst21.Osm_Elements;

import java.util.ArrayList;
import java.util.List;

public class Way extends Element {
    private List<Node> nodes = new ArrayList<>();
    private Long id;

    public Node first() {
        return nodes.get(0);
    }
    
    public void add(Node node) {
        nodes.add(node);
    }

    public void setId(Long _id){
        id = _id;
    }
    public Long getId(){
        return id;
    } 
    
}