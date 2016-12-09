import java.util.Comparator;

public class HeaderNode {
	public String itemID;
	public int supportCount;
	public FPTree nodeLink;
	
	public HeaderNode(String itemID, int supportCount){
		this.itemID = itemID;
		this.supportCount = supportCount;
	}

	@Override
	public String toString() {
		return "HeaderNode [itemID=" + itemID + ", supportCount=" + supportCount + "]";
	}
}
