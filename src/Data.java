
public class Data {
	Integer id, x, y, value[], count, mod;
	boolean located;
	Data(int i, String loc){
		if(loc==null || loc.equalsIgnoreCase("")) return;
		String tok[]=loc.split("[,]");
		x=Integer.parseInt(tok[0]);
		y=Integer.parseInt(tok[1]);
		id = i;
		located=true;
	}
	Data(int i, Integer v[]){
		id=i;
		value=v;
		located=false;
		count=0;
		mod=1;
	}
	
	Data(int i, Integer v[], String loc){
		this(i,v);
		if(loc==null || loc.equalsIgnoreCase("")) return;
		String tok[]=loc.split("[,]");
		x=Integer.parseInt(tok[0]);
		y=Integer.parseInt(tok[1]);
		located=true;
	}

	Data(int ii, String v, String loc){
		this(ii,new Integer[0],loc);
		String tok[]=v.split("[,]");
		value=new Integer[tok.length];
		System.out.println("Data Value :");
		for(int i=0; i<value.length; i++){
			System.out.print(tok[i]+",");
			value[i]=Integer.parseInt(tok[i]);
		}
		System.out.println();
	}
	
	public void incrementCounter(){
		count=(count+1)%mod;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setLocation(int x, int y){
		this.x=x;
		this.y=y;
		located=true;
	}
	
	public void setValue(Integer[] x){
		value=x;
	}
	
	public void setId(int i){
		id = i;
	}
	
	public void setValue(String v){
		String tok[]=v.split("[,]");
		value=new Integer[tok.length];
		for(int i=0; i<value.length; i++)
			value[i]=Integer.parseInt(tok[i]);
	}
	
	public void updateValue(Integer[] x) {
		if(value == null){
			value = x;
			return;
		}
		if(value.length!=x.length) return;
		for(int i=0; i<value.length; i++)
			value[i]+=x[i];
		incrementCounter();
		if(count==0){
			for(int i=0; i<value.length; i++)
				value[i]/=(mod+1);
		}
	}
	
	public int getId(){
		return id;
	}
	
	public Integer[] getValue(){
		Integer v[] = value;
		for(int i=0; i<v.length; i++)
			v[i]/=(count+1);
		return v;
	}
	
	public String getValueToString(){
		String s="";
		Integer value[] = getValue();
		if(value==null || value.length==0) return s;
		s=""+value[0];
		for(int i=1; i<value.length; i++)
			s+=(","+value[i]);
		return s;
	}
	
	public String getLocation(){
		if(!located) return "0,0";
		return x+","+y;
	}
	
	public String toString(){
		return("ID :"+id+" Value :"+this.getValueToString()+" Location: "+x+","+y);
	}
}
