import java.util.Comparator;

public class HeaderComparator implements Comparator<HeaderNode>{

	@Override
	public int compare(HeaderNode h1, HeaderNode h2) {//for decreasing sorting, so the returned value is reversed
		if(h1.supportCount > h2.supportCount){
			return -1;
		}else if(h1.supportCount < h2.supportCount){
			return 1;
		}else{
			return 0;
		}
	}
	
}
