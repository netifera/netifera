package com.netifera.platform.net.internal.services.detection.basic;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.util.PortSet;

@SuppressWarnings("boxing")
public class MSSQLDetector implements INetworkServiceDetector {
	
	private final Pattern responsePattern = Pattern.compile("(^\\x04\\x01\\x00\\x25|^\\x05\\x6e\\x00|;MSSQLSERVER;).*", Pattern.MULTILINE|Pattern.DOTALL);

	static Map<Integer, String> versionToOS = new HashMap<Integer, String>();
	
	static {
		versionToOS.put(0x09000c6e, "2005 SP2+Q940128"); // 9.00.3182
		versionToOS.put(0x09000c6b, "2005 SP2+Q938243"); // 9.00.3179
		versionToOS.put(0x09000c69, "2005 SP2+Q939563"); // 9.00.3177
		versionToOS.put(0x09000c63, "2005 SP2+Q937745"); // 9.00.3171
		versionToOS.put(0x09000c57, "2005 SP2+Q934459"); // 9.00.3159
		versionToOS.put(0x09000c54, "2005 SP2+Q934226"); // 9.00.3156
		versionToOS.put(0x09000c51, "2005 SP2+Q933564"); // 9.00.3153
		versionToOS.put(0x09000c50, "2005 SP2+Q933097 (Cumulative HF1)"); // 9.00.3152
		versionToOS.put(0x09000bee, "2005 SP2+Q934458"); // 9.00.3054
		versionToOS.put(0x09000bea, "2005 SP2+Q933508"); // 9.00.3050
		versionToOS.put(0x09000be3, "2005 SP2+Q933508 (use this if SP2 was applied prior to 3/8)"); // 9.00.3043
		versionToOS.put(0x09000be2, "2005 'Fixed' SP2 (use this if SP2 was NOT applied yet - orig. RTM removed)"); // 9.00.3042
		versionToOS.put(0x09000bd3, "2005 SP2 CTP (November)"); // 9.00.3027
		versionToOS.put(0x09000bd2, "2005 SP1+Q929376"); // 9.00.3026
		versionToOS.put(0x090008c5, "2005 SP1+Q933573"); // 9.00.2245
		versionToOS.put(0x090008ba, "2005 SP1+Q937343"); // 9.00.2234
		versionToOS.put(0x090008b8, "2005 SP1+Q937277"); // 9.00.2232
		versionToOS.put(0x090008b5, "2005 SP1+Q935446"); // 9.00.2229
		versionToOS.put(0x090008af, "2005 SP1+Q932393"); // 9.00.2223
		versionToOS.put(0x090008ad, "2005 SP1+Q931593"); // 9.00.2221
		versionToOS.put(0x090008a8, "2005 SP1+Q931821"); // 9.00.2216
		versionToOS.put(0x090008a7, "2005 SP1+Q931666"); // 9.00.2215
		versionToOS.put(0x090008a1, "2005 SP1+Q929278"); // 9.00.2209
		versionToOS.put(0x0900089a, "2005 SP1+Q927643"); // 9.00.2202
		versionToOS.put(0x09000899, "2005 SP1+Q927289"); // 9.00.2201
		versionToOS.put(0x09000893, "2005 SP1+Q926240"); // 9.00.2195
		versionToOS.put(0x09000892, "2005 SP1+Q925744"); // 9.00.2194
		versionToOS.put(0x09000890, "2005 SP1+Q924954/925335"); // 9.00.2192
		versionToOS.put(0x0900088f, "2005 SP1+Q925135"); // 9.00.2191
		versionToOS.put(0x0900088e, "2005 SP1+Q925227"); // 9.00.2190
		versionToOS.put(0x0900088d, "2005 SP1+Q925153"); // 9.00.2189
		versionToOS.put(0x0900088b, "2005 SP1+Q923849"); // 9.00.2187
		versionToOS.put(0x0900086c, "2005 SP1+Q919611"); // 9.00.2156
		versionToOS.put(0x09000802, "2005 SP1+.NET Vulnerability fix"); // 9.00.2050
		versionToOS.put(0x090007ff, "2005 SP1 RTM"); // 9.00.2047
		versionToOS.put(0x090007f8, "2005 SP1 CTP"); // 9.00.2040
		versionToOS.put(0x090007ed, "SP1 Beta"); // 9.00.2029
		versionToOS.put(0x09000619, "2005 RTM+Q932556"); // 9.00.1561
		versionToOS.put(0x09000616, "2005 RTM+Q926493"); // 9.00.1558
		versionToOS.put(0x09000612, "2005 RTM+Q926292"); // 9.00.1554
		versionToOS.put(0x0900060f, "2005 RTM+Q922804"); // 9.00.1551
		versionToOS.put(0x0900060b, "2005 RTM+Q918276"); // 9.00.1547
		versionToOS.put(0x09000603, "2005 RTM+Q917738"); // 9.00.1539
		versionToOS.put(0x09000602, "2005 RTM+Q917824"); // 9.00.1538
		versionToOS.put(0x09000600, "2005 RTM+Q917016"); // 9.00.1536
		versionToOS.put(0x090005fe, "2005 RTM+Q916706"); // 9.00.1534
		versionToOS.put(0x090005fd, "2005 RTM+Q916086"); // 9.00.1533
		versionToOS.put(0x090005fc, "2005 RTM+Q916046"); // 9.00.1532
		versionToOS.put(0x090005fb, "2005 RTM+Q915918"); // 9.00.1531
		versionToOS.put(0x090005ef, "2005 RTM+Q913494"); // 9.00.1519
		versionToOS.put(0x090005ea, "2005 RTM+Q912471"); // 9.00.1514
		versionToOS.put(0x090005df, "2005 RTM+Q911662"); // 9.00.1503
		versionToOS.put(0x090005de, "2005 RTM+Q915793"); // 9.00.1502
		versionToOS.put(0x090005dc, "2005 RTM+Q910416"); // 9.00.1500
		versionToOS.put(0x0900057e, "2005 RTM+Q932557"); // 9.00.1406
		versionToOS.put(0x09000577, "2005 RTM"); // 9.00.1399
		versionToOS.put(0x09000522, "September CTP Release"); // 9.00.1314
		versionToOS.put(0x090004a3, "June CTP Release"); // 9.00.1187
		versionToOS.put(0x0900045c, "April CTP Release"); // 9.00.1116
		versionToOS.put(0x09000442, "March CTP Release (may list as Feb.)"); // 9.00.1090
		versionToOS.put(0x090003d5, "December CTP Release"); // 9.00.981
		versionToOS.put(0x090003b7, "October CTP Release"); // 9.00.951
		versionToOS.put(0x09000395, "Internal build (?)"); // 9.00.917
		versionToOS.put(0x09000354, "Beta 2"); // 9.00.852
		versionToOS.put(0x09000351, "Internal build (?)"); // 9.00.849
		versionToOS.put(0x0900034c, "Internal build (?)"); // 9.00.844
		versionToOS.put(0x09000344, "Express Ed. Tech Preview"); // 9.00.836
		versionToOS.put(0x09000337, "Internal build (IDW4)"); // 9.00.823
		versionToOS.put(0x09000316, "Internal build (IDW3)"); // 9.00.790
		versionToOS.put(0x090002ff, "Internal build (IDW2)"); // 9.00.767
		versionToOS.put(0x090002eb, "Internal build (IDW)"); // 9.00.747
		versionToOS.put(0x09000285, "MS Internal (?)"); // 9.00.645
		versionToOS.put(0x09000260, "Beta 1"); // 9.00.608
		versionToOS.put(0x0700047e, "7.0 SP4+Q891116"); // 7.00.1150
		versionToOS.put(0x07000478, "7.0 SP4+Q830233"); // 7.00.1144
		versionToOS.put(0x07000477, "7.0 SP4+Q829015"); // 7.00.1143
		versionToOS.put(0x07000449, "7.0 SP4+Q822756"); // 7.00.1097
		versionToOS.put(0x07000446, "7.0 SP4+Q815495"); // 7.00.1094
		versionToOS.put(0x07000437, "329499"); // 7.00.1079
		versionToOS.put(0x07000436, "7.0 SP4+Q327068"); // 7.00.1078
		versionToOS.put(0x07000435, "7.0 SP4+Q316333"); // 7.00.1077
		versionToOS.put(0x07000427, "7.0 SP4 - All languages"); // 7.00.1063
		versionToOS.put(0x07000409, "7.0 SP3+Q324469"); // 7.00.1033
		versionToOS.put(0x07000402, "7.0 SP3+Q319851"); // 7.00.1026
		versionToOS.put(0x070003ec, "7.0 SP3+Q304851"); // 7.00.1004
		versionToOS.put(0x070003e4, "7.0 SP3+Q299717"); // 7.00.996
		versionToOS.put(0x070003d2, "7.0 SP3+Q285870"); // 7.00.978
		versionToOS.put(0x070003d1, "7.0 SP3+Q284351"); // 7.00.977
		versionToOS.put(0x070003ca, "7.0 SP3+Q283837/282243"); // 7.00.970
		versionToOS.put(0x070003c1, "7.0 SP3 - All languages"); // 7.00.961
		versionToOS.put(0x07000399, "7.0 SP2+Q283837"); // 7.00.921
		versionToOS.put(0x07000397, "7.0 SP2+Q282243"); // 7.00.919
		versionToOS.put(0x07000396, "7.0 SP2+Q280380"); // 7.00.918
		versionToOS.put(0x07000395, "7.0 SP2+Q279180"); // 7.00.917
		versionToOS.put(0x0700038e, "7.0 SP2+Q275901"); // 7.00.910
		versionToOS.put(0x07000389, "7.0 SP2+Q274266"); // 7.00.905
		versionToOS.put(0x07000379, "7.0 SP2+Q243741"); // 7.00.889
		versionToOS.put(0x0700036f, "7.0 SP2+Q281185"); // 7.00.879
		versionToOS.put(0x07000359, "7.0 SP2+Q260346"); // 7.00.857
		versionToOS.put(0x0700034a, "7.0 SP2"); // 7.00.842
		versionToOS.put(0x07000347, "7.0 SP2 Unidentified"); // 7.00.839
		versionToOS.put(0x07000343, "7.0 SP2 Beta"); // 7.00.835
		versionToOS.put(0x07000308, "7.0 SP1+Q258087"); // 7.00.776
		versionToOS.put(0x07000302, "7.0 SP1+Q252905"); // 7.00.770
		versionToOS.put(0x070002e9, "7.0 SP1+Q253738"); // 7.00.745
		versionToOS.put(0x070002d2, "7.0 SP1+Q239458"); // 7.00.722
		versionToOS.put(0x070002bb, "7.0 SP1"); // 7.00.699
		versionToOS.put(0x070002b1, "7.0 SP1 Beta"); // 7.00.689
		versionToOS.put(0x070002a5, "7.0 MSDE O2K Dev"); // 7.00.677
		versionToOS.put(0x07000296, "7.0 Gold+Q232707"); // 7.00.662
		versionToOS.put(0x07000292, "7.0 Gold+Q244763"); // 7.00.658
		versionToOS.put(0x07000291, "7.0 Gold+Q229875"); // 7.00.657
		versionToOS.put(0x07000283, "7.0 Gold+Q220156"); // 7.00.643
		versionToOS.put(0x0700026f, "7.0 Gold (RTM), no SP"); // 7.00.623
		versionToOS.put(0x07000247, "7.0 RC1"); // 7.00.583
		versionToOS.put(0x07000205, "7.0 Beta 3"); // 7.00.517
		versionToOS.put(0x063201df, "6.5 Post SP5a"); // 6.50.479
		versionToOS.put(0x063201a0, "6.5 Bad SP5a"); // 6.50.416
		versionToOS.put(0x0632019f, "6.5 Bad SP5"); // 6.50.415
		versionToOS.put(0x06320153, "6.5 Y2K Hotfix"); // 6.50.339
		versionToOS.put(0x06320129, "6.5 Site Server 3"); // 6.50.297
		versionToOS.put(0x06320119, "6.5 SP4"); // 6.50.281
		versionToOS.put(0x06320103, "6.5 SP3 SBS Only"); // 6.50.259
		versionToOS.put(0x06320102, "6.5 SP3"); // 6.50.258
		versionToOS.put(0x063200fc, "6.5 Bad SP3"); // 6.50.252
		versionToOS.put(0x063200f0, "6.5 SP2"); // 6.50.240
		versionToOS.put(0x063200d5, "6.5 SP1"); // 6.50.213
		versionToOS.put(0x063200c9, "6.5 Gold"); // 6.50.201
		versionToOS.put(0x06000097, "6.0 SP3"); // 6.00.151
		versionToOS.put(0x0600008b, "6.0 SP2"); // 6.00.139
		versionToOS.put(0x0600007c, "6.0 SP1"); // 6.00.124
		versionToOS.put(0x06000079, "6.0 No SP"); // 6.00.121
		versionToOS.put(0x080008c9, "2000 SP4+Q936232"); // 8.00.2249
		versionToOS.put(0x080008c8, "2000 SP4+Q935950"); // 8.00.2248
		versionToOS.put(0x080008c5, "2000 SP4+Q933573"); // 8.00.2245
		versionToOS.put(0x080008c4, "2000 SP4+Q934203"); // 8.00.2244
		versionToOS.put(0x080008be, "2000 SP4+Q931932"); // 8.00.2238
		versionToOS.put(0x080008b8, "2000 SP4+Q928568"); // 8.00.2232
		versionToOS.put(0x080008b7, "2000 SP4+Q928079"); // 8.00.2231
		versionToOS.put(0x080008aa, "2000 SP4+Q925297"); // 8.00.2218
		versionToOS.put(0x080008a1, "2000 SP4+Q923797"); // 8.00.2209
		versionToOS.put(0x0800089f, "2000 SP4+Q923344"); // 8.00.2207
		versionToOS.put(0x08000899, "2000 SP4+Q920930"); // 8.00.2201
		versionToOS.put(0x08000897, "2000 SP4+Q919221"); // 8.00.2199
		versionToOS.put(0x08000894, "2000 SP4+Q919165"); // 8.00.2196
		versionToOS.put(0x08000890, "2000 SP4+Q917606"); // 8.00.2192
		versionToOS.put(0x08000884, "2000 SP4+Q913684 (64bit)"); // 8.00.2180
		versionToOS.put(0x0800087c, "2000 SP4+Q910707"); // 8.00.2172
		versionToOS.put(0x0800087b, "2000 SP4+Q909369"); // 8.00.2171
		versionToOS.put(0x08000878, "2000 SP4+Q907813"); // 8.00.2168
		versionToOS.put(0x08000877, "2000 SP4+Q921293"); // 8.00.2167
		versionToOS.put(0x08000876, "2000 SP4+Q909734"); // 8.00.2166
		versionToOS.put(0x08000872, "2000 SP4+Q904660"); // 8.00.2162
		versionToOS.put(0x0800086c, "2000 SP4+Q906790"); // 8.00.2156
		versionToOS.put(0x08000864, "2000 SP4+Q899430/31/900390/404/901212/902150/955"); // 8.00.2148
		versionToOS.put(0x08000863, "2000 SP4+Q899410"); // 8.00.2147
		versionToOS.put(0x08000861, "2000 SP4+Q826906/836651"); // 8.00.2145
		versionToOS.put(0x080007f8, "2000 SP4+Q899761"); // 8.00.2040
		versionToOS.put(0x080007f7, "2000 SP4  "); // 8.00.2039
		versionToOS.put(0x080007ea, "2000 SP4 Beta"); // 8.00.2026
		versionToOS.put(0x0800060b, "2000 SP3+Q899410"); // 8.00.1547
		versionToOS.put(0x0800040d, "2000 SP3+Q930484"); // 8.00.1037
		versionToOS.put(0x0800040c, "2000 SP3+Q929410"); // 8.00.1036
		versionToOS.put(0x0800040b, "2000 SP3+Q917593"); // 8.00.1035
		versionToOS.put(0x0800040a, "2000 SP3+Q915328"); // 8.00.1034
		versionToOS.put(0x08000405, "2000 SP3+Q902852"); // 8.00.1029
		versionToOS.put(0x08000403, "2000 SP3+Q900416"); // 8.00.1027
		versionToOS.put(0x08000401, "2000 SP3+Q899428/899430"); // 8.00.1025
		versionToOS.put(0x08000400, "2000 SP3+Q898709"); // 8.00.1024
		versionToOS.put(0x080003fd, "2000 SP3+Q887700"); // 8.00.1021
		versionToOS.put(0x080003fc, "2000 SP3+Q896985"); // 8.00.1020
		versionToOS.put(0x080003fb, "2000 SP3+Q897572"); // 8.00.1019
		versionToOS.put(0x080003f9, "2000 SP3+Q896425"); // 8.00.1017
		versionToOS.put(0x080003f6, "2000 SP3+Q895123/187"); // 8.00.1014
		versionToOS.put(0x080003f5, "2000 SP3+Q891866"); // 8.00.1013
		versionToOS.put(0x080003f1, "2000 SP3+Q894257"); // 8.00.1009
		versionToOS.put(0x080003ef, "2000 SP3+Q893312"); // 8.00.1007
		versionToOS.put(0x080003e8, "2000 SP3+Q891585"); // 8.00.1000
		versionToOS.put(0x080003e5, "2000 SP3+Q891311"); // 8.00.997
		versionToOS.put(0x080003e4, "2000 SP3+Q891017/891268"); // 8.00.996
		versionToOS.put(0x080003e2, "2000 SP3+Q890942/768/767"); // 8.00.994
		versionToOS.put(0x080003e1, "2000 SP3+Q890925/888444/890742"); // 8.00.993
		versionToOS.put(0x080003df, "2000 SP3+Q889314"); // 8.00.991
		versionToOS.put(0x080003de, "2000 SP3+Q890200"); // 8.00.990
		versionToOS.put(0x080003dc, "2000 SP3+Q889166"); // 8.00.988
		versionToOS.put(0x080003d9, "2000 SP3+Q889239"); // 8.00.985
		versionToOS.put(0x080003d4, "2000 SP3+Q887974"); // 8.00.980
		versionToOS.put(0x080003d1, "2000 SP3+Q888007 "); // 8.00.977
		versionToOS.put(0x080003cd, "2000 SP3+Q884554"); // 8.00.973
		versionToOS.put(0x080003cc, "2000 SP3+Q885290"); // 8.00.972
		versionToOS.put(0x080003ca, "2000 SP3+Q872842"); // 8.00.970
		versionToOS.put(0x080003c7, "2000 SP3+Q878501"); // 8.00.967
		versionToOS.put(0x080003c2, "2000 SP3+Q883415"); // 8.00.962
		versionToOS.put(0x080003c1, "2000 SP3+Q873446"); // 8.00.961
		versionToOS.put(0x080003bf, "2000 SP3+Q878500"); // 8.00.959
		versionToOS.put(0x080003bd, "2000 SP3+Q870994"); // 8.00.957
		versionToOS.put(0x080003bb, "2000 SP3+Q867798"); // 8.00.955
		versionToOS.put(0x080003ba, "2000 SP3+Q843282"); // 8.00.954
		versionToOS.put(0x080003b8, "2000 SP3+Q867878/867879/867880"); // 8.00.952
		versionToOS.put(0x080003b0, "2000 SP3+Q839280"); // 8.00.944
		versionToOS.put(0x080003a9, "2000 SP3+Q841776"); // 8.00.937
		versionToOS.put(0x080003a8, "2000 SP3+Q841627"); // 8.00.936
		versionToOS.put(0x080003a7, "2000 SP3+Q841401"); // 8.00.935
		versionToOS.put(0x080003a6, "2000 SP3+Q841404"); // 8.00.934
		versionToOS.put(0x080003a5, "2000 SP3+Q840856"); // 8.00.933
		versionToOS.put(0x080003a1, "2000 SP3+Q839529"); // 8.00.929
		versionToOS.put(0x080003a0, "2000 SP3+Q839589"); // 8.00.928
		versionToOS.put(0x0800039f, "2000 SP3+Q839688"); // 8.00.927
		versionToOS.put(0x0800039e, "2000 SP3+Q839523"); // 8.00.926
		versionToOS.put(0x0800039b, "2000 SP3+Q838460"); // 8.00.923
		versionToOS.put(0x0800039a, "2000 SP3+Q837970"); // 8.00.922
		versionToOS.put(0x08000397, "2000 SP3+Q837957"); // 8.00.919
		versionToOS.put(0x08000394, "2000 SP3+Q317989"); // 8.00.916
		versionToOS.put(0x08000393, "2000 SP3+Q837401"); // 8.00.915
		versionToOS.put(0x08000391, "2000 SP3+Q836651"); // 8.00.913
		versionToOS.put(0x0800038f, "2000 SP3+Q837957"); // 8.00.911
		versionToOS.put(0x0800038e, "2000 SP3+Q834798"); // 8.00.910
		versionToOS.put(0x0800038c, "2000 SP3+Q834290"); // 8.00.908
		versionToOS.put(0x08000388, "2000 SP3+Q834453"); // 8.00.904
		versionToOS.put(0x0800037c, "2000 SP3+Q833710"); // 8.00.892
		versionToOS.put(0x0800037b, "2000 SP3+Q836141"); // 8.00.891
		versionToOS.put(0x0800036f, "2000 SP3+Q832977"); // 8.00.879
		versionToOS.put(0x0800036e, "2000 SP3+Q831950"); // 8.00.878
		versionToOS.put(0x0800036c, "2000 SP3+Q830912/831997/831999"); // 8.00.876
		versionToOS.put(0x08000369, "2000 SP3+Q830887"); // 8.00.873
		versionToOS.put(0x08000367, "2000 SP3+Q830767/830860"); // 8.00.871
		versionToOS.put(0x08000366, "2000 SP3+Q830262"); // 8.00.870
		versionToOS.put(0x08000365, "2000 SP3+Q830588"); // 8.00.869
		versionToOS.put(0x08000363, "2000 SP3+Q830366"); // 8.00.867
		versionToOS.put(0x08000362, "2000 SP3+Q830366"); // 8.00.866
		versionToOS.put(0x08000361, "2000 SP3+Q830395/828945"); // 8.00.865
		versionToOS.put(0x0800035f, "2000 SP3+Q829205/829444"); // 8.00.863
		versionToOS.put(0x0800035b, "2000 SP3+Q821334 *May contain errors*"); // 8.00.859
		versionToOS.put(0x0800035a, "2000 SP3+Q828637"); // 8.00.858
		versionToOS.put(0x08000359, "2000 SP3+Q828017/827714/828308"); // 8.00.857
		versionToOS.put(0x08000358, "2000 SP3+Q828096"); // 8.00.856
		versionToOS.put(0x08000356, "2000 SP3+Q828699"); // 8.00.854
		versionToOS.put(0x08000354, "2000 SP3+Q830466/827954"); // 8.00.852
		versionToOS.put(0x08000353, "2000 SP3+Q826754"); // 8.00.851
		versionToOS.put(0x08000352, "2000 SP3+Q826860/826815/826906"); // 8.00.850
		versionToOS.put(0x08000350, "2000 SP3+Q826822"); // 8.00.848
		versionToOS.put(0x0800034f, "2000 SP3+Q826433"); // 8.00.847
		versionToOS.put(0x0800034d, "2000 SP3+Q826364/825854"); // 8.00.845
		versionToOS.put(0x0800034c, "2000 SP3+Q826080"); // 8.00.844
		versionToOS.put(0x0800034a, "2000 SP3+Q825043"); // 8.00.842
		versionToOS.put(0x08000349, "2000 SP3+Q825225"); // 8.00.841
		versionToOS.put(0x08000348, "2000 SP3+Q319477/319477"); // 8.00.840
		versionToOS.put(0x08000347, "2000 SP3+Q823877/824027/820788"); // 8.00.839
		versionToOS.put(0x08000345, "2000 SP3+Q821741/548/740/823514"); // 8.00.837
		versionToOS.put(0x08000333, "2000 SP3+Q826161"); // 8.00.819
		versionToOS.put(0x08000332, "2000 SP3+Q821277/337/818388/826161/821280"); // 8.00.818
		versionToOS.put(0x08000330, "2000 SP3+Q818766"); // 8.00.816
		versionToOS.put(0x0800032e, "2000 SP3+Q819662"); // 8.00.814
		versionToOS.put(0x0800032b, "2000 SP3+Q819248/819662/818897"); // 8.00.811
		versionToOS.put(0x08000327, "2000 SP3+Q818899"); // 8.00.807
		versionToOS.put(0x08000324, "2000 SP3+Q818729"); // 8.00.804
		versionToOS.put(0x08000321, "2000 SP3+Q818540"); // 8.00.801
		versionToOS.put(0x08000320, "2000 SP3+Q818414/097/188"); // 8.00.800
		versionToOS.put(0x0800031e, "2000 SP3+Q817464"); // 8.00.798
		versionToOS.put(0x0800031a, "2000 SP3+Q817464/813524/816440/817709"); // 8.00.794
		versionToOS.put(0x08000317, "2000 SP3+Q815249"); // 8.00.791
		versionToOS.put(0x08000316, "2000 SP3+Q817081"); // 8.00.790
		versionToOS.put(0x08000315, "2000 SP3+Q816840"); // 8.00.789
		versionToOS.put(0x08000314, "2000 SP3+Q816985"); // 8.00.788
		versionToOS.put(0x0800030d, "2000 SP3+Q815057"); // 8.00.781
		versionToOS.put(0x0800030c, "2000 SP3+Q816084/810185"); // 8.00.780
		versionToOS.put(0x0800030b, "2000 SP3+Q814035"); // 8.00.779
		versionToOS.put(0x08000308, "2000 SP3+Unidentified"); // 8.00.776
		versionToOS.put(0x08000307, "2000 SP3+Q815115"); // 8.00.775
		versionToOS.put(0x08000301, "2000 SP3+Q814889/93"); // 8.00.769
		versionToOS.put(0x080002fb, "2000 SP3+Q814113"); // 8.00.763
		versionToOS.put(0x080002fa, "2000 SP3+Q814032"); // 8.00.762
		versionToOS.put(0x080002f8, "2000 SP3/SP3a"); // 8.00.760
		versionToOS.put(0x080002e7, "2000 SP2+Q818406/763"); // 8.00.743
		versionToOS.put(0x080002e5, "2000 SP2+Q818096"); // 8.00.741
		versionToOS.put(0x080002e0, "2000 SP2+Q816937"); // 8.00.736
		versionToOS.put(0x080002df, "2000 SP2+Q814889"); // 8.00.735
		versionToOS.put(0x080002dd, "2000 SP2+Q813759"); // 8.00.733
		versionToOS.put(0x080002da, "2000 SP2+Q813769"); // 8.00.730
		versionToOS.put(0x080002d8, "2000 SP2+Q814460"); // 8.00.728
		versionToOS.put(0x080002d5, "2000 SP2+Q812995/813494"); // 8.00.725
		versionToOS.put(0x080002d3, "2000 SP2+Q812798"); // 8.00.723
		versionToOS.put(0x080002d1, "2000 SP2+Q812250/812393"); // 8.00.721
		versionToOS.put(0x080002ce, "2000 SP2+Q811703"); // 8.00.718
		versionToOS.put(0x080002cb, "2000 SP2+Q810688/811611"); // 8.00.715
		versionToOS.put(0x080002ca, "2000 SP2+Q811478"); // 8.00.714
		versionToOS.put(0x080002c9, "2000 SP2/3+Q811205"); // 8.00.713
		versionToOS.put(0x080002c6, "2000 SP2/3+Q811052"); // 8.00.710
		versionToOS.put(0x080002c1, "2000 SP2+Q810920"); // 8.00.705
		versionToOS.put(0x080002bf, "2000 SP2+Q810526"); // 8.00.703
		versionToOS.put(0x080002be, "2000 SP2+Q328551"); // 8.00.702
		versionToOS.put(0x080002bd, "2000 SP2+Q810026/810163"); // 8.00.701
		versionToOS.put(0x080002bc, "2000 SP2+Q810072"); // 8.00.700
		versionToOS.put(0x080002b8, "2000 SP2+Q810052/10"); // 8.00.696
		versionToOS.put(0x080002b7, "2000 SP2+Q331885/965/968"); // 8.00.695
		versionToOS.put(0x080002b5, "2000 SP2+Q330212"); // 8.00.693
		versionToOS.put(0x080002b1, "2000 SP2+Q329499"); // 8.00.689
		versionToOS.put(0x080002b0, "2000 SP2+Q329487"); // 8.00.688
		versionToOS.put(0x080002ae, "2000 SP2+Q316333"); // 8.00.686
		versionToOS.put(0x080002aa, "2000 SP3+Q319851"); // 8.00.682
		versionToOS.put(0x080002a7, "2000 SP2+Q316333"); // 8.00.679
		versionToOS.put(0x080002a6, "2000 SP2+Q328354"); // 8.00.678
		versionToOS.put(0x0800029b, "2000 SP2+8/14 fix"); // 8.00.667
		versionToOS.put(0x08000299, "2000 SP2+8/8 fix"); // 8.00.665
		versionToOS.put(0x08000295, "2000 SP2+Q326999"); // 8.00.661
		versionToOS.put(0x0800028f, "2000 SP2+7/24 fix"); // 8.00.655
		versionToOS.put(0x0800028c, "2000 SP2+Q810010?"); // 8.00.652
		versionToOS.put(0x0800028a, "2000 SP2+Q322853"); // 8.00.650
		versionToOS.put(0x08000284, "2000 SP2+Q324186"); // 8.00.644
		versionToOS.put(0x08000260, "2000 SP2+Q319507"); // 8.00.608
		versionToOS.put(0x0800025c, "2000 SP2+3/29 fix"); // 8.00.604
		versionToOS.put(0x08000252, "2000 SP2+Q319477/319477"); // 8.00.594
		versionToOS.put(0x08000242, "2000 SP2+Q317979/318045"); // 8.00.578
		versionToOS.put(0x08000231, "2000 SP2+1/29 fix"); // 8.00.561
		versionToOS.put(0x0800022e, "2000 SP2+Q314003/315395"); // 8.00.558
		versionToOS.put(0x08000228, "2000 SP2+Q313002/5"); // 8.00.552
		versionToOS.put(0x08000216, "2000 SP2.01"); // 8.00.534
		versionToOS.put(0x08000214, "2000 SP2"); // 8.00.532
		versionToOS.put(0x080001db, "2000 SP1+1/29 fix"); // 8.00.475
		versionToOS.put(0x080001da, "2000 SP1+Q315395"); // 8.00.474
		versionToOS.put(0x080001d9, "2000 SP1+Q314003"); // 8.00.473
		versionToOS.put(0x080001d7, "2000 SP1+Q313302"); // 8.00.471
		versionToOS.put(0x080001d5, "2000 SP1+Q313005"); // 8.00.469
		versionToOS.put(0x080001c4, "2000 SP1+Q308547"); // 8.00.452
		versionToOS.put(0x080001bc, "2000 SP1+Q307540/307655"); // 8.00.444
		versionToOS.put(0x080001bb, "2000 SP1+Q307538"); // 8.00.443
		versionToOS.put(0x080001ac, "2000 SP1+Q304850"); // 8.00.428
		versionToOS.put(0x08000180, "2000 SP1"); // 8.00.384
		versionToOS.put(0x0800011f, "2000 No SP+Q297209"); // 8.00.287
		versionToOS.put(0x080000fb, "2000 No SP+Q300194"); // 8.00.251
		versionToOS.put(0x080000fa, "2000 No SP+Q291683"); // 8.00.250
		versionToOS.put(0x080000f9, "2000 No SP+Q288122"); // 8.00.249
		versionToOS.put(0x080000ef, "2000 No SP+Q285290"); // 8.00.239
		versionToOS.put(0x080000e9, "2000 No SP+Q282416"); // 8.00.233
		versionToOS.put(0x080000e7, "2000 No SP+Q282279"); // 8.00.231
		versionToOS.put(0x080000e2, "2000 No SP+Q278239"); // 8.00.226
		versionToOS.put(0x080000e1, "2000 No SP+Q281663"); // 8.00.225
		versionToOS.put(0x080000df, "2000 No SP+Q280380"); // 8.00.223
		versionToOS.put(0x080000de, "2000 No SP+Q281769"); // 8.00.222
		versionToOS.put(0x080000da, "2000 No SP+Q279183"); // 8.00.218
		versionToOS.put(0x080000d9, "2000 No SP+Q279293/279296"); // 8.00.217
		versionToOS.put(0x080000d3, "2000 No SP+Q276329"); // 8.00.211
		versionToOS.put(0x080000d2, "2000 No SP+Q275900"); // 8.00.210
		versionToOS.put(0x080000cd, "2000 No SP+Q274330"); // 8.00.205
		versionToOS.put(0x080000cc, "2000 No SP+Q274329"); // 8.00.204
		versionToOS.put(0x080000c2, "2000 RTM/No SP"); // 8.00.194
		versionToOS.put(0x080000be, "2000 Gold, no SP"); // 8.00.190
		versionToOS.put(0x08000064, "2000 Beta 2"); // 8.00.100
		versionToOS.put(0x0800004e, "2000 EAP5"); // 8.00.078
		versionToOS.put(0x0800002f, "2000 EAP4"); // 8.00.047
	}
	

	public Map<String,String> detect(String trigger, String response) {
		if (!responsePattern.matcher(response).matches()) return null;

		Map<String,String> answer = new HashMap<String,String>();
		answer.put("serviceType", "MSSQL");
		
		byte[] data = null;
		try {
			data = response.getBytes("iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// XXX data = ?, return?
		}
		if (data == null || data.length == 0) {
			return answer;
		}
		ByteBuffer buffer = ByteBuffer.wrap(data);
        if (buffer.get() != 0x04) {
        	return answer;
        }
		if (buffer.get() != 0x01) {
            return answer;
        }

        int buflen = buffer.getShort() & 0xffff;
        if (buflen < 19) {
            return answer;
        }

        buffer.position(0x0b);
        if (buffer.getShort() != 6) {
            return answer;
        }

        buffer.position(0x09);
        int index = buffer.getShort() + 8;
        if ((index + 6) > buflen) {
            return answer;
        }
        buffer.position(index);
        int version = buffer.getInt();

        String versionString = String.format("%d.%d.%d", version >> 24, (version >> 16) & 0xff, version & 0xffff);
    	answer.put("version", versionString);
    	String banner = versionString;

    	String os = versionToOS.get(version);
        if (os != null) {
        	answer.put("os", "Windows "+os);
        	banner = banner+" "+os;
        } else {
        	answer.put("os", "Windows");
        }
        answer.put("arch", "i386"); // FIXME x86_64
        answer.put("banner", banner);
		return answer;
	}
			
	public PortSet getPorts() {
		return new PortSet("1433");
	}

	public String getProtocol() {
		return "tcp";
	}
}
