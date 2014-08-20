import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.parser.Entity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class CrawlerTheOne {
	HttpClient hc;
	HttpGet httpget;
	HttpResponse response;
	HttpEntity entity;

	boolean debug=true;
	
	final int VOL_GAP=2;

	//Ŀ�꣨ÿһһ����䣩ǰһ��item
	Pattern prePattern=Pattern.compile(
			"<div class=\"one-cita\">");
	//htmlԪ��
	Pattern htmlPattern=Pattern.compile(
			"<[^<|^>]*>");
	//vol��
	Pattern tituloPat=Pattern.compile("");
	//����
	Pattern domPat=Pattern.compile("<p class=\"dom\">");
	//����
	Pattern mayPat=Pattern.compile("<p class=\"may\">");

	/*
	 * 
	 * <div class="fp-one-titulo-pubdate">
                                <p class="titulo">VOL.668</p>
                                <p class="dom">6</p>
                                <p class="may">Aug 2014</p>
                            </div>
	 */
	/*
	Ը��ȱ��˸�����һ���˶�����Ը�պ�̸��ʱ��ᱻ�Լ��ж���from ��ͬ����Ĺ¶���������١�                    </div>
    <div class="one-pubdate">
        <p class="dom">6</p>
        <p class="may">Aug 2014</p>
    </div>
	 */

	private void visitVol(int vol) {
		final String volUrlStr="http://wufazhuce.com/one/vol.";
		HttpClient hClient=new DefaultHttpClient();
		try {

			httpget=new HttpGet(volUrlStr+vol);
			response=hc.execute(httpget);
			entity=response.getEntity();

			getCita(entity);

			httpget.abort();
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			hClient.getConnectionManager().shutdown();
		}

	}


	private void visitVol(int oldvol, int newvol) {
		for (int i = newvol; i >= oldvol; i--) {
			visitVol(i);
		}
	}


	/**
	 * ��ȡ���µ�vol���
	 * @return
	 */
	private int getNewestVol(HttpEntity entity) {

		try {
			InputStream inSm = entity.getContent();
			Scanner inScn = new Scanner(inSm/*, "UTF-8"*/);//ɨ����
			Pattern parttern=Pattern.compile(
					"http://wufazhuce.com/one/vol\\.(\\d+)");
			//������ʽ������
			//�ҵ����µ�url���ɣ����ҵ����µ�vol��ż���
			Matcher matcher;
			String str="";
			int vol=0;

			//��ȡvol���
			while (inScn.hasNextLine()) { 	
				str=inScn.nextLine();
				str=str.trim();
				matcher=parttern.matcher(str);

				if (matcher.find()){
					str = matcher.group(1);//��ȡ����vol��
					vol=Integer.parseInt(str);
					System.out.println(vol);
					break;
				}
			}  


			while (inScn.hasNextLine()) { 	
				str=inScn.nextLine();
				str=str.trim();

				//date-day
				matcher=domPat.matcher(str);
				if (matcher.find()){
					str = deletHtml(str);
					System.out.print(str+" ");
				}

				//date-month year
				matcher=mayPat.matcher(str);
				if (matcher.find()){
					str = deletHtml(str);
					System.out.println(str);
					break;
				}
			}  

			return vol;

		} catch (IllegalStateException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}

		return 0;
	}


	private String deletHtml(String str) {
		Matcher matcher;

		matcher=htmlPattern.matcher(str);
		str=matcher.replaceAll("");
		return str;
	}


	public void visitTheOne() throws ClientProtocolException, IOException {
		int vol=0;
		hc=new DefaultHttpClient();

		try{
			httpget=new HttpGet("http://wufazhuce.com/");
			response=hc.execute(httpget);
			entity=response.getEntity();

			if (debug) {
				printConnect();		
			}

			vol=getNewestVol(entity);
			System.out.println(vol);

			//we must abort it after using or before other new created
			httpget.abort();
		}finally{

		}

		//visitVol(vol);
		visitVol(vol-VOL_GAP,vol);
		hc.getConnectionManager().shutdown();
	}

	/**
	 * ��ȡOne�е�cita����ÿ��һ��
	 * @param entity
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	protected void getCita(HttpEntity entity) throws IllegalStateException, IOException {
		InputStream inSm;

		inSm = entity.getContent();
		Scanner inScn = new Scanner(inSm/*, "UTF-8"*/);//ɨ����
		Matcher matcher;
		
		String cita="";
		String data="";
		

	/*	
		<div class="one-pubdate">
        <p class="dom">12</p>
        <p class="may">Aug 2014</p>
    </div>
    */
		//��ȡvol���
		while (inScn.hasNextLine()) {
			//����http��һ��
			String tmp=inScn.nextLine();
			tmp=tmp.trim();

			//��λָ����Ŀ
			matcher=prePattern.matcher(tmp);
			if (matcher.find()){
				tmp=inScn.nextLine();//ȡָ����Ŀ��һ��
				tmp=deletHtml(tmp);//ȥ��htmlԪ��
				tmp=tmp.trim();//ȥ���հ׷�
				//System.out.println(tmp);
				cita=tmp;
				break;
			}
		}
		
		while (inScn.hasNextLine()) {
			//����http��һ��
			String tmp=inScn.nextLine();
			tmp=tmp.trim();
			
			matcher=domPat.matcher(tmp);
			if (matcher.find()) {		
				tmp=deletHtml(tmp);//ȥ��htmlԪ��
				tmp=tmp.trim();//ȥ���հ׷�
				//System.out.print(tmp);
				data=tmp+" ";
			}
			
			matcher=mayPat.matcher(tmp);
			if (matcher.find()) {
				tmp=deletHtml(tmp);//ȥ��htmlԪ��
				tmp=tmp.trim();//ȥ���հ׷�
				//System.out.print(tmp);
				data+=tmp;
				break;
			}
		}
		
		System.out.println(data+":"+cita);
	}

	private void printConnect() {
		System.out.println("----------------------------------------");  
		System.out.println(response.getStatusLine());  
		if (entity != null) {  
			System.out.println("Response content length: " + entity.getContentLength());  
		}  
		System.out.println("----------------------------------------");  

	}


	public final static void main(String[] args) throws Exception { 
		CrawlerTheOne tc=new CrawlerTheOne();
		tc.visitTheOne();

	}

}
