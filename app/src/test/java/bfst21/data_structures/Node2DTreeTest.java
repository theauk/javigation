package bfst21.data_structures;

import bfst21.Osm_Elements.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Node2DTreeTest {
    private Node2DTree<Node> tree;
    private Node n1;
    private Node n2;
    private Node n3;
    private Node n4;


    @BeforeEach
    void setUp() {
        tree = new Node2DTree<>();
        List<Node> list = new ArrayList<>();
        list.add(n1 = new Node(698332,(float)12.5972367,(float)55.6952510));
        list.add(n2 = new Node(698334,(float)12.5991499,(float)55.6951851));
        list.add(n3 = new Node(698335,(float)12.6002220,(float)55.6961339));
        list.add(n4 = new Node(698646,(float)55.6685247,(float)12.5823799));
        tree.addALl(list);
    }

    @Test
    void searchGetNearestNodeTest() {
        float n1x = (float)12.5972367;
        float n1y = (float)55.6952510;
        float n2x = (float)12.5991499;
        float n2y = (float)55.6951851;
        //System.out.println("Should be closest to n1, x : 12.5972367  y : 55.6952510" );
        //System.out.println("n1 x: " + n1x + " y: " + n1y);
        //System.out.println("n2 x: " + n2x + " y: " + n2y);
        Double sameCoordinate = Point2D.distance((float)12.5972367,(float)55.6952510,(float)12.5972367,(float)55.6952510);
        double differentCoordinates = Point2D.distance((float)12.5972367,(float)55.6952510,(float)12.5991499,(float)55.6951851);
        //assertEquals(698332, tree.getNearestNode(f1,f2).getId() );
        System.out.println(" distance from same coordinates  :" + sameCoordinate);
        System.out.println(" distance from different coordinates  :" + differentCoordinates);

    }

}
