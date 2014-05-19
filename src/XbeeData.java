
public class XbeeData {
	public Integer id, analogData[], digitalData[];
	
	public XbeeData() {
		id=0;
		analogData=null;
		digitalData=null;
	}
	
	public XbeeData(int id, Integer digital[], Integer analog[]) {
		this.id=id;
		digitalData=digital;
		analogData=analog;
	}
	
	public void setId(int id){
		this.id=id;
	}
	
	public void setAnalogData(Integer analog[]){
		analogData=analog;
	}
	
	public void setDigitalData(Integer digital[]){
		digitalData=digital;
	}
	
	public boolean ok(){
		return (id!=0)&&(analogData!=null)&&(digitalData!=null);
	}
}
