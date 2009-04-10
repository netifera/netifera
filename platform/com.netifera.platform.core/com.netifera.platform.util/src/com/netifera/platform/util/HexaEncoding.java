package com.netifera.platform.util;

public class HexaEncoding
{
  private static String EMPTY_STRING = "";
  
  /**
   * Convert data into hexa
   * 
   * @param data to convert
   * @return the converted string
   */
  public static final String bytes2hex(byte[] data)
  {
    if (data == null)
      return EMPTY_STRING;

    int len = data.length;
    StringBuffer buf = new StringBuffer(len * 2);
    for (int pos = 0; pos < len; pos++)
      buf.append(toHexChar((data[pos] >>> 4) & 0x0F)).append(
          toHexChar(data[pos] & 0x0F));
    return buf.toString();
  }

  /**
   * convert hexa into data
   * 
   * @param str to convert
   * @return the converted byte array
   */
  public static final byte[] hex2bytes(String str)
  {
    if (str == null)
      return new byte[0];

    str = str.replaceAll(" ", ""); // allow white spaces for clarity, ignore them
    int len = str.length();
    char[] hex = str.toCharArray();
    byte[] buf = new byte[len / 2];

    for (int pos = 0; pos < len / 2; pos++)
      buf[pos] = (byte) hexValueAtPosition(hex, pos);

    return buf;
  }

  public static final String hex2string(String str) {
    if (str == null) {
      return EMPTY_STRING;
    }
    
    str = str.replaceAll(" ", ""); // allow white spaces for clarity, ignore them
    int len = str.length();
    char[] hex = str.toCharArray();
    char[] buf = new char[len / 2];
    for (int pos = 0; pos < len / 2; pos++) {
        buf[pos] = (char) hexValueAtPosition(hex, pos);
    }
    return String.copyValueOf(buf);
  }

  // hexValueAtPosition({'f','f'}, 0} = 0xff
  private static int hexValueAtPosition(char[] hexbuf, int position) {
	  assert hexbuf.length >= 2 * position + 1;
	  return (((toDataNibble(hexbuf[2 * position]) << 4) & 0xF0) |
			  (toDataNibble(hexbuf[2 * position + 1]) & 0x0F));
  }

  /**
   * convert value to hexa value
   * 
   * @param i byte to convert
   * @return hexa char
   */
  public static char toHexChar(int i)
  {
    if ((0 <= i) && (i <= 9))
      return (char) ('0' + i);
	return (char) ('a' + (i - 10));
  }

  /**
   * convert hexa char to byte value
   * 
   * @param c hexa character
   * @return corresponding byte value
   */
  public static byte toDataNibble(char c)
  {
    if (('0' <= c) && (c <= '9'))
      return (byte) ((byte) c - (byte) '0');
    else if (('a' <= c) && (c <= 'f'))
      return (byte) ((byte) c - (byte) 'a' + 10);
    else if (('A' <= c) && (c <= 'F'))
      return (byte) ((byte) c - (byte) 'A' + 10);
    else
      throw new RuntimeException("Unknown nible "+c+" ("+((byte)c & 0xFF) +")");
  }
}