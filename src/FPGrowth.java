import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class FPGrowth {
	public double minSupport;
	public ArrayList<HeaderNode> headerTable;
	public FPTree fpTree;
	public List<ArrayList<String>> transections;
	public String fileName;
	public int nTrans;
	public HashSet<ArrayList<String>> FPatterns;
	public Hashtable<String, Integer> itemCounts;
	
	public FPGrowth(String fileName, double minSupport){
		this.fileName = fileName;
		this.minSupport = minSupport;
		this.nTrans = 0;
		this.headerTable = new ArrayList<HeaderNode>();
		this.transections = new ArrayList<ArrayList<String>>();
		this.FPatterns = new HashSet<ArrayList<String>>();
		this.itemCounts = new Hashtable<String, Integer>();
	}
	
	public void readFile(){
		try {
			Scanner scan = new Scanner(new File(fileName));
			while (scan.hasNextLine()) {
				String t = scan.nextLine();
				ArrayList<String> transection = new ArrayList<String>();
				nTrans++;
				StringTokenizer st = new StringTokenizer(t, ", ");
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					transection.add(token);
					if(itemCounts.containsKey(token)){
						itemCounts.put(token, itemCounts.get(token) + 1);
					}else{
						itemCounts.put(token, 1);
					}
				}
				transections.add(transection);
			}
		} catch (FileNotFoundException e) {
			System.err.println("couldn't read file: " + fileName);
		}
	}
	
	public ArrayList<HeaderNode> createHeaderTable(){
		ArrayList<HeaderNode> headerTable = new ArrayList<HeaderNode>();
		for(String s: itemCounts.keySet()){
			if(itemCounts.get(s) >= nTrans*minSupport){
				headerTable.add(new HeaderNode(s, itemCounts.get(s)));
			}
		}
		Collections.sort(headerTable, new HeaderComparator());
		return headerTable;
	}
	
	public void secondScan(){
		fpTree = new FPTree("null");
		fpTree.root = true;
		fpTree.itemID = null;
		for(ArrayList<String> transection: transections){
			ArrayList<String> orderedTransection = new ArrayList<String>();
			for(HeaderNode h: headerTable){
				if(transection.contains(h.itemID)){
					orderedTransection.add(h.itemID);
				}
			}
			insert(orderedTransection, fpTree, 0);
		}
	}
	
	public void insert(ArrayList<String> orderedTransection, FPTree fpTree, int n){//algorithm from my open source "FP-Growth"
		if(n < orderedTransection.size()){
			String item = orderedTransection.get(n);
			FPTree newTree = null;
			boolean found = false;
			for(FPTree child: fpTree.children){
				if(child.itemID.equals(item)){
					newTree = child;
					child.count++;
					found = true;
					break;
				}
			}
			if(!found){
				newTree = new FPTree(item);
				newTree.count = 1;
				newTree.parent = fpTree;
				fpTree.children.add(newTree);
				for(HeaderNode h: headerTable){
					if(h.itemID.equals(item)){
						FPTree temp = h.nodeLink;
						if(temp == null){
							h.nodeLink = newTree;
						}else{
							while(temp.next != null){
								temp = temp.next;
							}
							temp.next = newTree;
						}
					}
				}
			}
			insert(orderedTransection, newTree, n+1);
		}
	}
	
	public void growth(FPTree fpTree, ArrayList<String> toCheck, ArrayList<HeaderNode> headerTable){
		if(isSinglePath(fpTree)){
			ArrayList<String> items = new ArrayList<String>();
			while(fpTree != null){
				if(fpTree.itemID != null){
					items.add(fpTree.itemID);
				}
				if(fpTree.children.size() > 0){
					fpTree = fpTree.children.get(0);
				}else{
					fpTree = null;
				}
			}
			ArrayList<ArrayList<String>> combinations = generateCombinations(items, headerTable);
			for(ArrayList<String> combination: combinations){
				combination.addAll(toCheck);
			}
			FPatterns.addAll(combinations);
		}else{
			for(int i = headerTable.size() - 1; i >=0; i--){
				ArrayList<String> combination = new ArrayList<String>();
				combination.addAll(toCheck);
				combination.remove(null);
				combination.add(headerTable.get(i).itemID);
				int count = headerTable.get(i).supportCount;
				FPatterns.add(combination);
				ArrayList<ArrayList<String>> conPatBaseTrans = new ArrayList<ArrayList<String>>();
				FPTree temp = headerTable.get(i).nodeLink;
				HashSet<String> newOneItems = new HashSet<String>();
				while(temp != null){
					FPTree temp2 = temp;				
					ArrayList<String> newItemset = new ArrayList<String>();
					while(temp.itemID != null){
						if(temp != temp2){
							newItemset.add(temp.itemID);
							newOneItems.add(temp.itemID);
						}
						temp = temp.parent;
					}
					for(int j = 0; j < temp2.count; j++){
						conPatBaseTrans.add(newItemset);
					}
					temp = temp2.next;
				}
				ArrayList<HeaderNode> newHeaderTable = createHeaderTableSub(conPatBaseTrans, newOneItems);
				FPTree newTree = secondScanSub(conPatBaseTrans, newHeaderTable);
				if(newTree.children.size() > 0){
					growth(newTree, combination, newHeaderTable);
				}
			}
		}
	}
	
	public boolean isSinglePath(FPTree fpTree){
		boolean isSinglePath = true;
		if(fpTree.children.size() > 1){
			isSinglePath = false;
			return isSinglePath;
		}else{
			for(FPTree child: fpTree.children){
				if(isSinglePath){
					isSinglePath = isSinglePath(child);
				}else{
					break;
				}
			}
		}
		return isSinglePath;
	}
	
	public ArrayList<ArrayList<String>> generateCombinations(ArrayList<String> items, ArrayList<HeaderNode> newHeaderTable){//algorithm from my open source "Permutation"
		ArrayList<ArrayList<String>> combinations = new ArrayList<ArrayList<String>>();
		if(items.size() != 0){
			String s = items.get(0);
			if(items.size() > 1){
				items.remove(0);
				ArrayList<ArrayList<String>> combinationsSub = generateCombinations(items, newHeaderTable);
				combinations.addAll(combinationsSub);
				for(ArrayList<String> a: combinationsSub){
					for(int i = 0; i < a.size(); i++){
						ArrayList<String> combination = new ArrayList<String>();
						int count = Integer.MAX_VALUE;
						for(int j = 0; j <= i; j++){
							for(HeaderNode h: newHeaderTable){
								if(h.itemID.equals(a.get(j)) && count < h.supportCount){
									count = h.supportCount;
								}
							}
							combination.add(a.get(j));
						}
						for(HeaderNode h: newHeaderTable){
							if(h.itemID.equals(s) && count < h.supportCount){
								count = h.supportCount;
							}
						}
						if(count >= minSupport*nTrans){
							combination.add(s);
							combinations.add(combination);
						}
					}
				}
			}
			ArrayList<String> combination = new ArrayList<String>();
			combination.add(s);
			combinations.add(combination);
		}
		return combinations;
	}
	
	public ArrayList<HeaderNode> createHeaderTableSub(ArrayList<ArrayList<String>> conPatBaseTrans, HashSet<String> newOneItems){
		ArrayList<HeaderNode> headerTable = new ArrayList<HeaderNode>();
		for(String s: newOneItems){
			int count = 0;
			for(ArrayList<String> t: conPatBaseTrans){
				if(t.contains(s)){
					count++;
				}
			}
			if(count >= nTrans * minSupport){
				headerTable.add(new HeaderNode(s, count));
			}
		}
		Collections.sort(headerTable, new HeaderComparator());
		return headerTable;
	}
	
	public FPTree secondScanSub(ArrayList<ArrayList<String>> conPatBaseTrans, ArrayList<HeaderNode> newHeaderTable){
		FPTree fpTreeSub = new FPTree("null");
		fpTreeSub.root = true;
		fpTreeSub.itemID = null;
		for(ArrayList<String> transection: conPatBaseTrans){
			ArrayList<String> orderedTransection = new ArrayList<String>();
			for(HeaderNode h: newHeaderTable){
				if(transection.contains(h.itemID)){
					orderedTransection.add(h.itemID);
				}
			}
			insertSub(orderedTransection, fpTreeSub, 0, newHeaderTable);
		}
		return fpTreeSub;
	}
	
	public void insertSub(ArrayList<String> orderedTransection, FPTree fpTree, int n, ArrayList<HeaderNode> newHeaderTable){//algorithm from my open source "FP-Growth"
		if(n < orderedTransection.size()){
			String item = orderedTransection.get(n);
			FPTree newTree = null;
			boolean found = false;
			for(FPTree child: fpTree.children){
				if(child.itemID.equals(item)){
					newTree = child;
					child.count++;
					found = true;
					break;
				}
			}
			if(!found){
				newTree = new FPTree(item);
				newTree.count = 1;
				newTree.parent = fpTree;
				fpTree.children.add(newTree);
				for(HeaderNode h: newHeaderTable){
					if(h.itemID.equals(item)){
						FPTree temp = h.nodeLink;
						if(temp == null){
							h.nodeLink = newTree;
						}else{
							while(temp.next != null){
								temp = temp.next;
							}
							temp.next = newTree;
						}
					}
				}
			}
			insertSub(orderedTransection, newTree, n+1, newHeaderTable);
		}
	}
	
	public void doit(){
		readFile();
		headerTable = createHeaderTable();//first scan and create the header table
		secondScan();
		ArrayList<String> nullList = new ArrayList<String>();
		nullList.add(null);
		growth(fpTree, nullList, headerTable);
	}
	
	public void printFPatterns(){
		for(ArrayList<String> a: FPatterns){
			System.out.print("{");
			for(String s: a){
				System.out.print(s + ", ");
			}
			System.out.print("}");
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		long startingTime = System.currentTimeMillis();
		FPGrowth fpGrowth = new FPGrowth("adult.txt", 0.6);
		fpGrowth.doit();
		fpGrowth.printFPatterns();
		long endingTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endingTime - startingTime)/1000.0);
	}
}
