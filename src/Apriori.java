import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class Apriori {
	public List<ArrayList<String>> itemsets;
	public List<ArrayList<String>> Fitemsets;
	public List<ArrayList<String>> transections;
	public Set<String> oneItems;
	public String fileName;
	public int nTrans;
	public int nLength;
	public double minSupport;
	public List<ArrayList<String>> candidates;
	
	public Apriori(String fileName, double minSupport){
		this.fileName = fileName;
		this.minSupport = minSupport;
		this.nTrans = 0;
		this.nLength = 0;
		this.itemsets = new ArrayList<ArrayList<String>>();
		this.Fitemsets = new ArrayList<ArrayList<String>>();
		this.transections = new ArrayList<ArrayList<String>>();
		this.oneItems = new HashSet<String>();
		this.candidates = new ArrayList<ArrayList<String>>();
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
					oneItems.add(token);
				}
				transections.add(transection);
			}
		} catch (FileNotFoundException e) {
			System.err.println("couldn't read file: " + fileName);
		}
	}
	
	public ArrayList<ArrayList<String>> findFrequent1(){
		ArrayList<ArrayList<String>>  frequentItemSet1 = new ArrayList<ArrayList<String>>();
		for(String s: oneItems){
			ArrayList<String> a = new ArrayList<String>();
			int count = 0;
			for(ArrayList<String> t: transections){
				if(t.contains(s)){
					count++;
				}
			}
			if(count >= nTrans*minSupport){
				a.add(s);
				frequentItemSet1.add(a);
			}
		}
		return frequentItemSet1;
	}
	
	public void generateCandidates(){
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		if(nLength == 1){
			temp = findFrequent1();
		}else{
			String s = null;
			for(int i = 0; i < itemsets.size(); i ++){
				for(int j = i; j < itemsets.size(); j ++){
					int ndiff = 0;
					ArrayList<String> tempCandidate = new ArrayList<String>();
					for(int k = 0; k < itemsets.get(j).size(); k++){
						if(!itemsets.get(i).contains(itemsets.get(j).get(k))){
							ndiff++;
							s = itemsets.get(j).get(k);
						}
					}
					if(ndiff == 1){
						tempCandidate.addAll(itemsets.get(i));
						tempCandidate.add(s);
						temp.add(tempCandidate);
					}
				}
			}
		}
		candidates = temp;
	}
	
	public void prune(){
		ArrayList<ArrayList<String>> tempItemsets = new ArrayList<ArrayList<String>>();
		if(nLength > 1){
			for(ArrayList<String> i: candidates){
				boolean frequent = true;
				for(int j = 0; j < i.size(); j++){
					ArrayList<String> temp = new ArrayList<String>();
					temp.addAll(i);
					i.remove(j);
					if(!itemsets.contains(i)){
						frequent = false;
					}
					i = temp;
				}
				if(frequent){
					tempItemsets.add(i);
				}
			}
			candidates = tempItemsets;
		}
	}
	
	public void doit(){
		readFile();
		nLength = 1;
		do{
			generateCandidates();
			prune();
			itemsets.clear();
			for(ArrayList<String> a: candidates){
				int count = 0;
				for(ArrayList<String> transection: transections){
					boolean contain = true;
					for(String s: a){
						if(!transection.contains(s)){
							contain = false;
							break;
						}
					}
					if(contain){
						count++;
					}
				}
				if(count >= nTrans * minSupport && !itemsets.contains(a)){
					itemsets.add(a);
				}
			}
			Fitemsets.addAll(itemsets);
			nLength++;
		}while(itemsets.size() > 0);
		
	}
	
	public void printFitemsets(){
		for(ArrayList<String> a: Fitemsets){
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
		Apriori apriori = new Apriori("adult.txt", 0.6);
		apriori.doit();
		apriori.printFitemsets();
		long endingTime = System.currentTimeMillis();
		System.out.println("Running time: " + (endingTime - startingTime)/1000.0);
	}
}
