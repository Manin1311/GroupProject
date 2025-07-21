public class ComplaintBST {

    class Node {
        Complaint data;
        Node left, right;

        Node(Complaint data) {
            this.data = data;
            left = right = null;
        }
    }

    private Node root;

    // ✅ Insert into BST based on complaintId
    public void insert(Complaint c) {
        root = insertRec(root, c);
    }

    private Node insertRec(Node root, Complaint c) {
        if (root == null) return new Node(c);

        if (c.getComplaintId() < root.data.getComplaintId()) {
            root.left = insertRec(root.left, c);
        } else {
            root.right = insertRec(root.right, c);
        }
        return root;
    }

    // ✅ In-order traversal (sorted by complaintId)
    public void inOrder() {
        if (root == null) {
            System.out.println("📭 No complaints found.");
            return;
        }
        inOrderRec(root);
    }

    private void inOrderRec(Node root) {
        if (root != null) {
            inOrderRec(root.left);
            System.out.println(root.data);  // relies on Complaint.toString()
            inOrderRec(root.right);
        }
    }
}
