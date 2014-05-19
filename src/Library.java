import java.io.BufferedReader;
import java.io.IOException;


public class Library {


	public static void stayCool(int miliSec){
		long end=System.currentTimeMillis()+miliSec;
		while(System.currentTimeMillis()<end);
	}

	public static String addSpace(String s, int l){
		String d="";
		for(int i=0; i<l-s.length(); i++)
			d+="0";
		d+=s.toLowerCase();
		return d;
	}

	public static int convert(String s[], int st, int e){
		int v=0;
		System.out.println("Array :"+s.length+" Start :"+st);
		for(int i=st; i<st+e; i++){
			v*=256;
			v+=Integer.parseInt(s[i], 16);
		}
		return v;
	}

	public static int countBit(int value, int limit){
		int count=0;
		for(int i=0; i<limit; i++)
			if(((value>>i)&1)==1)
				count++;
		return count;
	}

	public static XbeeData test(BufferedReader in) throws IOException{
		XbeeData V=new XbeeData();
		String line;
		int offset=0;
		while((line=in.readLine())!=null){
			V=new XbeeData();
			System.out.println("Discarted Data :");
			while(!line.equalsIgnoreCase("7e")){
				line=in.readLine().toLowerCase();
				System.out.print(line);
			}
			System.out.println();

			int frameLength=0;
			for (int i=0; i<2; i++){
				frameLength*=256;
				frameLength+=Integer.parseInt(in.readLine().toLowerCase(),16);
			}
			System.out.println("Length "+frameLength);

			System.out.println("Accepted Data :");
			Integer a[];
			String s[]=new String[frameLength+1];
			for (int i=0; i<=frameLength; i++){
				s[i]=in.readLine();
				System.out.print(s[i]+" ");
			}
			System.out.println();
			
			/***Getting  Network ID***/
			a=new Integer[1];
			a[0]=(convert(s,5,4));
			V.setId(a[0]);
			/***Getting  Digital IO Mask***/
			int digiMask=convert(s,13,2);
			/***Getting  Analog IO Mask***/
			int analogMask=convert(s,15,1);
			/***Getting  Digital Value***/
			a=new Integer[countBit(digiMask,16)];
			if(a.length!=0){
				offset=2;
				int digi=convert(s,16,2);
				for(int i=0, c=0; i<16; i++)
					if(((digiMask>>i)&1)==1){
						a[c++]=((digi>>i)&1);
					}
			}
			V.setDigitalData(a);

			/***Getting  Analog Value***/
			a=new Integer[countBit(analogMask,8)];
			for(int i=0, c=0; i<8; i++){
				if(((analogMask>>i)&1)==1){
					a[c++]=(convert(s,16+offset,2));
					offset+=2;
				}
			}
			V.setAnalogData(a);

			/***Checking Checksum***/
			int total=0;
			for(int i=0; i<=frameLength; i++)
				total+=Integer.parseInt(s[i], 16);

			String cs=Integer.toHexString(total);
			if(cs.substring(cs.length()-2).equalsIgnoreCase("ff")) break;	
			System.out.println("CheckSum EROOR "+cs);
		}
		System.out.println("CheckSum Passed-->"+V.ok());
		return V;
	}


	public static String calculateChkSum(String []s){

		int sum=0;
		for(int i=3; i<s.length-1; i++)
			sum+=Integer.parseInt(s[i],16);
		String s1=Integer.toHexString(1279-sum); ///4FF-SUM
		return s1.substring(s1.length()-2).toLowerCase();
	}
}
