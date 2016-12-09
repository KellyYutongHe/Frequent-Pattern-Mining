import java.util.ArrayList;

public class FPTree {
	public boolean root;
//	public HeaderNode header;
	public ArrayList<FPTree> children;
	public FPTree parent;
	public String itemID;
	public int count;
	public FPTree next;
	
	public FPTree(String itemID){
		this.root = false;
		this.children = new ArrayList<FPTree>();
		this.itemID = itemID;
	}
}