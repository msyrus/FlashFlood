
public class Pair {
	Integer key;
	String value;
	
	Pair(Integer k, String v){
		key = k;
		value = v;
	}
	
	Integer getKey(){
		return key;
	}
	
	String getvalue(){
		return value;
	}
	
	public String toString(){
		return "key: "+key+", value: "+value;
	}
}
