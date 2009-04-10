package com.netifera.platform.net.wifi.packets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("boxing")
public class WiFiFramePrinter {
	final private StringBuffer buffer = new StringBuffer();
	
	// Information Element IDs
	static final int IE_SSID           	=0x00;
	static final int IE_SUPP_RATES     	=0x01;
	static final int IE_FH_PARAMETER   	=0x02;
	static final int IE_DS_PARAMETER   	=0x03;
	static final int IE_CF_PARAMETER   	=0x04;
	static final int IE_TIM            	=0x05;
	static final int IE_IBSS_PARAMETER 	=0x06;
	static final int IE_CHALLENGE_TEXT	=0x10;

	private static Map<Integer, String> mainTypeMap;
	private static Map<Integer, String> typeMap;
	private static Map<Integer, String> reasonMap;
	private static Map<Integer, String> statusMap;
	
	static {
		mainTypeMap=new HashMap<Integer, String>();
		mainTypeMap.put(WiFiFrame.MGT_FRAME, "Management Frame");
		mainTypeMap.put(WiFiFrame.CONTROL_FRAME, "Control Frame");
		mainTypeMap.put(WiFiFrame.DATA_FRAME, "Data Frame");

		typeMap=new HashMap<Integer, String>();
		typeMap.put(WiFiFrame.MGT_ASSOC_REQ, "Association Request");
		typeMap.put(WiFiFrame.MGT_ASSOC_RESP, "Association Response");
		typeMap.put(WiFiFrame.MGT_REASSOC_REQ, "Reassociation Request");
		typeMap.put(WiFiFrame.MGT_REASSOC_RESP, "Reassociation Response");
		typeMap.put(WiFiFrame.MGT_PROBE_REQ, "Probe Request");
		typeMap.put(WiFiFrame.MGT_PROBE_RESP, "Probe Response");
		typeMap.put(WiFiFrame.MGT_BEACON, "Beacon");
		typeMap.put(WiFiFrame.MGT_ATIM, "ATIM");
		typeMap.put(WiFiFrame.MGT_DISASS, "Dissassociate");
		typeMap.put(WiFiFrame.MGT_AUTHENTICATION, "Authentication");
		typeMap.put(WiFiFrame.MGT_DEAUTHENTICATION, "Deauthentication");
		typeMap.put(WiFiFrame.CTRL_PS_POLL, "Power-Save poll");
		typeMap.put(WiFiFrame.CTRL_RTS, "Request-to-send");
		typeMap.put(WiFiFrame.CTRL_CTS, "Clear-to-send");
		typeMap.put(WiFiFrame.CTRL_ACKNOWLEDGEMENT, "Acknowledgement");
		typeMap.put(WiFiFrame.CTRL_CFP_END, "CF-End");
		typeMap.put(WiFiFrame.CTRL_CFP_ENDACK, "CF-End + CF-Ack");
		typeMap.put(WiFiFrame.DATA, "Data");
		typeMap.put(WiFiFrame.DATA_CF_ACK, "Data + CF-Acknowledgement");
		typeMap.put(WiFiFrame.DATA_CF_POLL, "Data + CF-Poll");
		typeMap.put(WiFiFrame.DATA_CF_ACK_POLL, "Data + CF-Acknowledgement/Poll");
		typeMap.put(WiFiFrame.DATA_NULL_FUNCTION, "Null");
		typeMap.put(WiFiFrame.DATA_CF_ACK_NOD, "Data + Acknowledgement (No data)");
		typeMap.put(WiFiFrame.DATA_CF_POLL_NOD, "Data + CF-Poll (No data)");
		typeMap.put(WiFiFrame.DATA_CF_ACK_POLL_NOD, "Data + CF-Acknowledgement/Poll (No data)");

		reasonMap=new HashMap<Integer, String>();
		reasonMap.put(0x00, "Reserved");
		reasonMap.put(0x01, "Unspecified reason");
		reasonMap.put(0x02, "Previous authentication no longer valid");
		reasonMap.put(0x03, "Deauthenticated because sending STA is leaving (has left) IBSS or ESS");
		reasonMap.put(0x04, "Disassociated due to inactivity");
		reasonMap.put(0x05, "Disassociated because AP is unable to handle all currently associated stations");
		reasonMap.put(0x06, "Class 2 frame received from nonauthenticated station");
		reasonMap.put(0x07, "Class 3 frame received from nonassociated station");
		reasonMap.put(0x08, "Disassociated because sending STA is leaving (has left) BSS");
		reasonMap.put(0x09, "Station requesting (re)association is not authenticated with responding station");

		statusMap=new HashMap<Integer, String>();
		statusMap.put(0x00, "Successful");
		statusMap.put(0x01, "Unspecified failure");
		statusMap.put(0x0A, "Cannot support all requested capabilities in the Capability information field");
		statusMap.put(0x0B, "Reassociation denied due to inability to confirm that association exists");
		statusMap.put(0x0C, "Association denied due to reason outside the scope of this standard");
		statusMap.put(0x0D, "Responding station does not support the specified authentication algorithm");
		statusMap.put(0x0E, "Received an Authentication frame with authentication sequence transaction sequence number out of expected sequence");
		statusMap.put(0x0F, "Authentication rejected because of challenge failure");
		statusMap.put(0x10, "Authentication rejected due to timeout waiting for next frame in sequence");
		statusMap.put(0x11, "Association denied because AP is unable to handle additional associated stations");
		statusMap.put(0x12, "Association denied due to requesting station not supporting all of the datarates in the BSSBasicServiceSet Parameter");
	}
	
	public void print(WiFiFrame frame) {
		if (frame instanceof ManagementFrame) {
			print((ManagementFrame) frame);
			return;
		}
		if (frame instanceof DataFrame) {
			print((DataFrame) frame);
			return;
		}
		buffer.append("IEEE 802.11 " + typeMap.get(frame.type) + " " + frame.transmitter() + " -> " + frame.receiver());
		printEncryptionStatus(frame);
	}
	
	public void print(DataFrame frame) {
		buffer.append("IEEE 802.11 " + typeMap.get(frame.type) + " " + frame.source() + " -> " + frame.destination() + " via " + frame.bssid());
		printEncryptionStatus(frame);
	}
	
	public void print(ManagementFrame frame) {
		buffer.append("IEEE 802.11 " + typeMap.get(frame.type) + " " + frame.source() + " -> " + frame.destination() + " via " + frame.bssid());
		printInformationElements(frame.informationElements());
		printEncryptionStatus(frame);
	}
	
	public void print(AssociationResponse frame) {
		print((ManagementFrame) frame);
		printStatus(frame.statusCode);
	}

	public void print(Authentication frame) {
		print((ManagementFrame) frame);
		printStatus(frame.statusCode);
	}

	public void print(Deauthentication frame) {
		print((ManagementFrame) frame);
		printReason(frame.reasonCode);
	}
	
	public void print(Disassociation frame) {
		print((ManagementFrame) frame);
		printReason(frame.reasonCode);
	}
	
	private void printEncryptionStatus(WiFiFrame frame) {
		if (frame.isProtected()) buffer.append(" PRIVATE");
	}
	
	private void printStatus(int code) {
		buffer.append(" Status: "+statusMap.get(code));
	}

	private void printReason(int code) {
		buffer.append(" Reason: "+reasonMap.get(code));
	}

	private void printInformationElements(List<InformationElement> elements) {
		for (InformationElement element: elements) {
			switch (element.id()) {
			case IE_SSID:
				buffer.append(" SSID=\"" + element.toString() + "\"");
				break;

			case IE_SUPP_RATES:
				buffer.append(" Rates=");
				byte[] IE = element.toBytes();
				for(int j=0; j<IE.length; j++)
				{
					int rate=IE[j] & 0x7f;
					if (j>0) buffer.append(",");
					buffer.append((rate*500)/1000);
				}
				break;

			case IE_FH_PARAMETER:
				buffer.append(" FH="+hexa(element.toBytes()));
				break;

			case IE_DS_PARAMETER:
				buffer.append(" Channel="+element.toInteger());
				break;

			case IE_CF_PARAMETER:
				buffer.append(" CF="+hexa(element.toBytes()));
				break;

			case IE_TIM:
				buffer.append(" TIM="+hexa(element.toBytes()));
				break;

			case IE_IBSS_PARAMETER:
				buffer.append(" IBSS="+hexa(element.toBytes()));
				break;

			case IE_CHALLENGE_TEXT:
				buffer.append(" Challenge=\""+element.toString()+"\"");
				break;

			default:
				buffer.append(" "+element.id()+"="+hexa(element.toBytes()));
				break;
			}
		}
	}

	// XXX
	private String hexa(byte[] bytes) {
		final String xlat = "0123456789abcdef";
		String answer = "";
		for (byte x: bytes) {
			answer += String.valueOf(xlat.charAt((x >>> 4) & 0x0F)) + String.valueOf(xlat.charAt(x & 0x0F));
		}
		return answer;		
	}
	
	public String toString() {
		return buffer.toString();
	}
}
