package bfst21.data_structures;

import bfst21.Osm_Elements.Node;

public class KDTreeTest {
    private static KDTree<Node> tree;

//   @BeforeEach
//   void setUp() {
//       tree = new KDTree<>(0, 4);
//   }
//
//   @Test
//   void searchGetNearestNodeTest() throws KDTreeEmptyException {
//       /*List<Node> list = new ArrayList<>();
//       tree.add("1",new Node(1,(float)12.5972367,(float)55.6952510));
//       tree.add("2",new Node(2,(float)12.5991499,(float)55.6951851));
//       tree.add("3",new Node(3,(float)12.6002220,(float)55.6961339));
//       tree.add("4",new Node(4,(float)12.5823799,(float)55.6685247));
//
//       Node node = new Node(5, (float)12.5823800, (float)55.6685260);
//       assertEquals("4", tree.getNearestNode(node.getxMax(),node.getyMax()));*/
//   }
//
//   @Test
//   void exceptionTest() {
//       // TODO: 30-03-2021
//   }
//
//   @Test
//   void newAddTest() {
//       Node n1 = new Node(1, 1, 1);
//       Node n2 = new Node(2, 2, 2);
//       Node n3 = new Node(3, 3, 3);
//       Node n4 = new Node(4, -4, -4);
//       Way w1 = new Way(1);
//       w1.addNode(n1);
//       w1.addNode(n2);
//       w1.addNode(n2);
//       w1.addNode(n2);
//       w1.addNode(n3);
//       w1.addNode(n4);
//       w1.addNode(n4);
//       w1.addNode(n4);
//
//
//       tree.addAll(w1.getNodes());
//       tree.testBuild();
//       tree.printTree();
//   }
//
//   @AfterAll
//       static void afterAll() {
//        tree = null;
//    }
//
}