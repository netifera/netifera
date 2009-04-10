package com.netifera.platform.net.daemon.sniffing.modules.passivefp;

public final class SignatureData {
	static final String[] synSignatures = {
		"45046:64:0:44:M*:.:AIX:4.3",

		"16384:64:0:44:M512:.:AIX:4.3.2 and earlier",

		"16384:64:0:60:M512,N,W%2,N,N,T:.:AIX:4.3.3-5.2 (1)",
		"32768:64:0:60:M512,N,W%2,N,N,T:.:AIX:4.3.3-5.2 (2)",
		"65535:64:0:60:M512,N,W%2,N,N,T:.:AIX:4.3.3-5.2 (3)",

		"65535:64:0:64:M*,N,W1,N,N,T,N,N,S:.:AIX:5.3 ML1",

		// ----------------- Linux -------------------

		"S1:64:0:44:M*:A:Linux:1.2.x",
		"512:64:0:44:M*:.:Linux:2.0.3x (1)",
		"16384:64:0:44:M*:.:Linux:2.0.3x (2)",

		// Endian snafu! Nelson says "ha-ha":
		"2:64:0:44:M*:.:Linux:2.0.3x (MkLinux) on Mac (1)",
		"64:64:0:44:M*:.:Linux:2.0.3x (MkLinux) on Mac (2)",

		"S4:64:1:60:M1360,S,T,N,W0:.:Linux:2.4 (Google crawlbot)",
		"S4:64:1:60:M1430,S,T,N,W0:.:Linux:2.4-2.6 (Google crawlbot)",

		"S2:64:1:60:M*,S,T,N,W0:.:Linux:2.4 (large MTU?)",
		"S3:64:1:60:M*,S,T,N,W0:.:Linux:2.4 (newer)",
		"S4:64:1:60:M*,S,T,N,W0:.:Linux:2.4-2.6",

		"S3:64:1:60:M*,S,T,N,W1:.:Linux:2.6, seldom 2.4 (older, 1)",
		"S4:64:1:60:M*,S,T,N,W1:.:Linux:2.6, seldom 2.4 (older, 2)",
		"S3:64:1:60:M*,S,T,N,W2:.:Linux:2.6, seldom 2.4 (older, 3)",
		"S4:64:1:60:M*,S,T,N,W2:.:Linux:2.6, seldom 2.4 (older, 4)",
		"T4:64:1:60:M*,S,T,N,W2:.:Linux:2.6 (older, 5)",

		"S4:64:1:60:M*,S,T,N,W5:.:Linux:2.6 (newer, 1)",
		"S4:64:1:60:M*,S,T,N,W6:.:Linux:2.6 (newer, 2)",
		"S4:64:1:60:M*,S,T,N,W7:.:Linux:2.6 (newer, 3)",
		"T4:64:1:60:M*,S,T,N,W7:.:Linux:2.6 (newer, 4)",


		"S20:64:1:60:M*,S,T,N,W0:.:Linux:2.2 (1)",
		"S22:64:1:60:M*,S,T,N,W0:.:Linux:2.2 (2)",
		"S11:64:1:60:M*,S,T,N,W0:.:Linux:2.2 (3)",
		
		// Popular cluster config scripts disable timestamps and
		// selective ACK:

		"S4:64:1:48:M1460,N,W0:.:Linux:2.4 in cluster",

		// This happens only over loopback, but let's make folks happy:
		"32767:64:1:60:M16396,S,T,N,W0:.:Linux:2.4 (loopback)",
		"32767:64:1:60:M16396,S,T,N,W2:.:Linux:2.6 (newer, loopback)",
		"S8:64:1:60:M3884,S,T,N,W0:.:Linux:2.2 (loopback)",

		// Opera visitors:
		"16384:64:1:60:M*,S,T,N,W0:.:-Linux:2.2 (Opera?)",
		"32767:64:1:60:M*,S,T,N,W0:.:-Linux:2.4 (Opera?)",

		// Some fairly common mods & oddities:
		"S22:64:1:52:M*,N,N,S,N,W0:.:Linux:2.2 (tstamp-)",
		"S4:64:1:52:M*,N,N,S,N,W0:.:Linux:2.4 (tstamp-)",
		"S4:64:1:52:M*,N,N,S,N,W2:.:Linux:2.6 (tstamp-)",
		"S4:64:1:44:M*:.:Linux:2.6? (barebone, rare!)",
		"T4:64:1:60:M1412,S,T,N,W0:.:Linux:2.4 (rare!)",

		// ----------------- FreeBSD -----------------

		"16384:64:1:44:M*:.:FreeBSD:2.0-4.2",
		"16384:64:1:60:M*,N,W0,N,N,T:.:FreeBSD:4.4 (1)",

		"1024:64:1:60:M*,N,W0,N,N,T:.:FreeBSD:4.4 (2)",

		"57344:64:1:44:M*:.:FreeBSD:4.6-4.8 (RFC1323-)",
		"57344:64:1:60:M*,N,W0,N,N,T:.:FreeBSD:4.6-4.9",

		"32768:64:1:60:M*,N,W0,N,N,T:.:FreeBSD:4.8-5.1 (or MacOS X 10.2-10.3)",
		"65535:64:1:60:M*,N,W0,N,N,T:.:FreeBSD:4.7-5.2 (or MacOS X 10.2-10.4) (1)",
		"65535:64:1:60:M*,N,W1,N,N,T:.:FreeBSD:4.7-5.2 (or MacOS X 10.2-10.4) (2)",

		"65535:64:1:60:M*,N,W0,N,N,T:Z:FreeBSD:5.1 (1)",
		"65535:64:1:60:M*,N,W1,N,N,T:Z:FreeBSD:5.1 (2)",
		"65535:64:1:60:M*,N,W2,N,N,T:Z:FreeBSD:5.1 (3)",
		"65535:64:1:64:M*,N,N,S,N,W1,N,N,T:.:FreeBSD:5.3-5.4",
		"65535:64:1:64:M*,N,W1,N,N,T,S,E:P:FreeBSD:6.x (1)",
		"65535:64:1:64:M*,N,W0,N,N,T,S,E:P:FreeBSD:6.x (2)",
		
		"65535:64:1:64:M1460,N,W3,N,N,T,S,E:P:MacOS X:10.5.4", // Netifera added

		"65535:64:1:44:M*:Z:FreeBSD:5.2 (RFC1323-)",
		
		// ----------------- NetBSD ------------------

		"16384:64:0:60:M*,N,W0,N,N,T:.:NetBSD:1.3",
		"65535:64:0:60:M*,N,W0,N,N,T0:.:-NetBSD:1.6 (Opera)",
		"16384:64:1:60:M*,N,W0,N,N,T0:.:NetBSD:1.6",
		"65535:64:1:60:M*,N,W1,N,N,T0:.:NetBSD:1.6W-current (DF)",
		"65535:64:1:60:M*,N,W0,N,N,T0:.:NetBSD:1.6X (DF)",
		"32768:64:1:60:M*,N,W0,N,N,T0:.:NetBSD:1.6Z or 2.0 (DF)",
		"32768:64:1:64:M1416,N,W0,S,N,N,N,N,T0:.:NetBSD:2.0G (DF)",
		"32768:64:1:64:M*,N,W0,S,N,N,N,N,T0:.:NetBSD:3.0 (DF)",

		// ----------------- OpenBSD -----------------

		"16384:64:1:64:M*,N,N,S,N,W0,N,N,T:.:OpenBSD:3.0-3.9",
		"57344:64:1:64:M*,N,N,S,N,W0,N,N,T:.:OpenBSD:3.3-3.4",
		"16384:64:0:64:M*,N,N,S,N,W0,N,N,T:.:OpenBSD:3.0-3.4 (scrub)",
		"65535:64:1:64:M*,N,N,S,N,W0,N,N,T:.:-OpenBSD:3.0-3.4 (Opera?)",
		"32768:64:1:64:M*,N,N,S,N,W0,N,N,T:.:OpenBSD:3.7",

		// ----------------- Solaris -----------------

		"S17:64:1:64:N,W3,N,N,T0,N,N,S,M*:.:Solaris:8 (RFC1323 on)",
		"S17:64:1:48:N,N,S,M*:.:Solaris:8 (1)",
		"S17:255:1:44:M*:.:Solaris:2.5-7 (1)",

		// Sometimes, just sometimes, Solaris feels like coming up with
		// rather arbitrary MSS values ;-)

		"S6:255:1:44:M*:.:Solaris:2.5-7 (2)",
		"S23:64:1:48:N,N,S,M*:.:Solaris:8 (2)",
		"S34:64:1:48:M*,N,N,S:.:Solaris:9",
		"S34:64:1:48:M*,N,N,N,N:.:Solaris:9 (no sack)",
		"S44:255:1:44:M*:.:Solaris:7",

		"4096:64:0:44:M1460:.:SunOS:4.1.x",

		"S34:64:1:52:M*,N,W0,N,N,S:.:Solaris:10 (beta)",
		"32850:64:1:64:M*,N,N,T,N,W1,N,N,S:.:Solaris:10 (1203?)",
		"32850:64:1:64:M*,N,W1,N,N,T,N,N,S:.:Solaris:9.1",

		// ----------------- IRIX --------------------

		"49152:60:0:44:M*:.:IRIX:6.2-6.4",
		"61440:60:0:44:M*:.:IRIX:6.2-6.5",
		"49152:60:0:52:M*,N,W2,N,N,S:.:IRIX:6.5 (RFC1323+) (1)",
		"49152:60:0:52:M*,N,W3,N,N,S:.:IRIX:6.5 (RFC1323+) (2)",

		"61440:60:0:48:M*,N,N,S:.:IRIX:6.5.12-6.5.21 (1)",
		"49152:60:0:48:M*,N,N,S:.:IRIX:6.5.12-6.5.21 (2)",

		"49152:60:0:64:M*,N,W2,N,N,T,N,N,S:.:IRIX:6.5 IP27",

		// ----------------- Tru64 -------------------
		// Tru64 and OpenVMS share the same stack on occassions.
		// Relax.

		"32768:60:1:48:M*,N,W0:.:Tru64:4.0 (or OS/2 Warp 4)",
		"32768:60:0:48:M*,N,W0:.:Tru64:5.0 (or OpenVMS 7.x on Compaq 5.0 stack)",
		"8192:60:0:44:M1460:.:Tru64:5.1 (no RFC1323) (or QNX 6)",
		"61440:60:0:48:M*,N,W0:.:Tru64:v5.1a JP4 (or OpenVMS 7.x on Compaq 5.x stack)",

		// ----------------- OpenVMS -----------------

		"6144:64:1:60:M*,N,W0,N,N,T:.:OpenVMS:7.2 (Multinet 4.3-4.4 stack)",

		// ----------------- MacOS -------------------

		"S2:255:1:48:M*,W0,E:.:MacOS:8.6 classic",

		"16616:255:1:48:M*,W0,E:.:MacOS:7.3-8.6 (OTTCP)",
		"16616:255:1:48:M*,N,N,N,E:.:MacOS:8.1-8.6 (OTTCP)",
		"32768:255:1:48:M*,W0,N:.:MacOS:9.0-9.2",

		"32768:255:1:48:M1380,N,N,N,N:.:MacOS:9.1 (OT 2.7.4) (1)",
		"65535:255:1:48:M*,N,N,N,N:.:MacOS:9.1 (OT 2.7.4) (2)",
		
		// ----------------- Windows -----------------

		// Windows TCP/IP stack is a mess. For most recent XP, 2000 and
		// even 98, the pathlevel, not the actual OS version, is more
		// relevant to the signature. They share the same code, so it would
		// seem. Luckily for us, almost all Windows 9x boxes have an
		// awkward MSS of 536, which I use to tell one from another
		// in most difficult cases.

		"8192:32:1:44:M*:.:Windows:3.11 (Tucows)",
		"S44:64:1:64:M*,N,W0,N,N,T0,N,N,S:.:Windows:95",
		"8192:128:1:64:M*,N,W0,N,N,T0,N,N,S:.:Windows:95b",

		// There were so many tweaking tools and so many stack versions for
		// Windows 98 it is no longer possible to tell them from each other
		// without some very serious research. Until then, there's an insane
		// number of signatures, for your amusement:

		"S44:32:1:48:M*,N,N,S:.:Windows:98 (low TTL) (1)",
		"8192:32:1:48:M*,N,N,S:.:Windows:98 (low TTL) (2)",
		"%8192:64:1:48:M536,N,N,S:.:Windows:98 (13)",
		"%8192:128:1:48:M536,N,N,S:.:Windows:98 (15)",
		"S4:64:1:48:M*,N,N,S:.:Windows:98 (1)",
		"S6:64:1:48:M*,N,N,S:.:Windows:98 (2)",
		"S12:64:1:48:M*,N,N,S:.:Windows:98 (3)",
		"T30:64:1:64:M1460,N,W0,N,N,T0,N,N,S:.:Windows:98 (16)",
		"32767:64:1:48:M*,N,N,S:.:Windows:98 (4)",
		"37300:64:1:48:M*,N,N,S:.:Windows:98 (5)",
		"46080:64:1:52:M*,N,W3,N,N,S:.:Windows:98 (RFC1323+)",
		"65535:64:1:44:M*:.:Windows:98 (no sack)",
		"S16:128:1:48:M*,N,N,S:.:Windows:98 (6)",
		"S16:128:1:64:M*,N,W0,N,N,T0,N,N,S:.:Windows:98 (7)",
		"S26:128:1:48:M*,N,N,S:.:Windows:98 (8)",
		"T30:128:1:48:M*,N,N,S:.:Windows:98 (9)",
		"32767:128:1:52:M*,N,W0,N,N,S:.:Windows:98 (10)",
		"60352:128:1:48:M*,N,N,S:.:Windows:98 (11)",
		"60352:128:1:64:M*,N,W2,N,N,T0,N,N,S:.:Windows:98 (12)",

		// What's with 1414 on NT?
		"T31:128:1:44:M1414:.:Windows:NT 4.0 SP6a (1)",
		"64512:128:1:44:M1414:.:Windows:NT 4.0 SP6a (2)",
		"8192:128:1:44:M*:.:Windows:NT 4.0 (older)",
		
		// Windows XP and 2000. Most of the signatures that were
		// either dubious or non-specific (no service pack data)
		// were deleted and replaced with generics at the end.

		"65535:128:1:48:M*,N,N,S:.:Windows:2000 SP4, XP SP1+",
		"%8192:128:1:48:M*,N,N,S:.:Windows:2000 SP2+, XP SP1+ (seldom 98)",
		"S20:128:1:48:M*,N,N,S:.:Windows:SP3",
		"S45:128:1:48:M*,N,N,S:.:Windows:2000 SP4, XP SP1+ (2)",
		"40320:128:1:48:M*,N,N,S:.:Windows:2000 SP4",

		"S6:128:1:48:M*,N,N,S:.:Windows:XP, 2000 SP2+",
		"S12:128:1:48:M*,N,N,S:.:Windows:XP SP1+ (1)",
		"S44:128:1:48:M*,N,N,S:.:Windows:XP SP1+, 2000 SP3",
		"64512:128:1:48:M*,N,N,S:.:Windows:XP SP1+, 2000 SP3 (2)",
		"32767:128:1:48:M*,N,N,S:.:Windows:XP SP1+, 2000 SP4 (3)",

		// Windows 2003 & Vista

		"8192:128:1:52:M*,W8,N,N,N,S:.:Windows:Vista (beta)",
		"32768:32:1:52:M1460,N,W0,N,N,S:.:Windows:2003 AS",
		"65535:64:1:52:M1460,N,W2,N,N,S:.:Windows:2003 (1)",
		"65535:64:1:48:M1460,N,N,S:.:Windows:2003 (2)",

		// Odds, ends, mods:

		"S52:128:1:48:M1260,N,N,S:.:Windows:XP/2000 via Cisco",
		"65520:128:1:48:M*,N,N,S:.:Windows:XP bare-bone",
		"16384:128:1:52:M536,N,W0,N,N,S:.:Windows:2000 w/ZoneAlarm?",
		"2048:255:0:40:.:.:Windows:.NET Enterprise Server",
		"44620:64:0:48:M*,N,N,S:.:Windows:ME no SP (?)",
		"S6:255:1:48:M536,N,N,S:.:Windows:95 winsock 2",
		"32000:128:0:48:M*,N,N,S:.:Windows:XP w/Winroute?",
		"16384:64:1:48:M1452,N,N,S:.:Windows:XP w/Sygate? (1)",
		"17256:64:1:48:M1460,N,N,S:.:Windows:XP w/Sygate? (2)",

		// No need to be more specific, it passes:
		"*:128:1:48:M*,N,N,S:U:-Windows:XP/2000 while downloading (leak!)",

		// ----------------- HP/UX -------------------

		"32768:64:1:44:M*:.:HP-UX:B.10.20",
		"32768:64:1:48:M*,W0,N:.:HP-UX:11.00-11.11",
		
		// Whoa. Hardcore WSS.
		"0:64:0:48:M*,W0,N:.:HP-UX:B.11.00 A (RFC1323+)",

		// ----------------- RiscOS ------------------

		"16384:64:1:68:M1460,N,W0,N,N,T,N,N,?12:.:RISC OS:3.70-4.36 (inet 5.04)",
		"12288:32:0:44:M536:.:RISC OS:3.70 inet 4.10",
		"4096:64:1:56:M1460,N,N,T:T:RISC OS:3.70 freenet 2.00",

		// ----------------- BSD/OS ------------------

		"8192:64:1:60:M1460,N,W0,N,N,T:.:BSD/OS:3.1-4.3 (or MacOS X 10.2)",

		// ---------------- NetwonOS -----------------

		"4096:64:0:44:M1420:.:NewtonOS:2.1",

		// ---------------- NeXTSTEP -----------------

		"S8:64:0:44:M512:.:NeXTSTEP:3.3 (1)",
		"S4:64:0:44:M1024:.:NeXTSTEP:3.3 (2)",

		// ------------------ BeOS -------------------

		"1024:255:0:48:M*,N,W0:.:BeOS:5.0-5.1",
		"12288:255:0:44:M*:.:BeOS:5.0.x",

		// ------------------ OS/400 -----------------

		"8192:64:1:60:M1440,N,W0,N,N,T:.:OS/400:V4R4/R5",
		"8192:64:0:44:M536:.:OS/400:V4R3/M0",
		"4096:64:1:60:M1440,N,W0,N,N,T:.:OS/400:V4R5 + CF67032",

		"28672:64:0:44:M1460:A:OS/390:?",

		// ------------------ ULTRIX -----------------

		"16384:64:0:40:.:.:ULTRIX:4.5",

		// ------------------- QNX -------------------

		"S16:64:0:44:M512:.:QNX:demodisk",
		"16384:64:0:60:M1460,N,W0,N,N,T0:.:QNX:6.x",

		// ------------------ Novell -----------------

		"16384:128:1:44:M1460:.:Novell:NetWare 5.0",
		"6144:128:1:44:M1460:.:Novell:IntranetWare 4.11",
		"6144:128:1:44:M1368:.:Novell:BorderManager ?",

		// According to rfp:
		"6144:128:1:52:M*,W0,N,S,N,N:.:Novell:Netware 6 SP3",

		// -------------- SCO UnixWare ---------------

		"S3:64:1:60:M1460,N,W0,N,N,T:.:SCO:UnixWare 7.1",
		"S17:64:1:60:M*,N,W0,N,N,T:.:SCO:UnixWare 7.1.x",
		"S23:64:1:44:M1380:.:SCO:OpenServer 5.0",

		// ------------------- DOS -------------------

		"2048:255:0:44:M536:.:DOS:Arachne via WATTCP/1.05",
		"T2:255:0:44:M984:.:DOS:Arachne via WATTCP/1.05 (eepro)",
		"16383:64:0:44:M536:.:DOS:Unknown via WATTCP (epppd)",

		// ------------------ OS/2 -------------------

		"S56:64:0:44:M512:.:OS/2:4",
		"28672:64:0:44:M1460:.:OS/2:Warp 4.0",

		// ----------------- TOPS-20 -----------------

		// Another hardcore MSS, one of the ACK leakers hunted down.
		"0:64:0:44:M1460:A:TOPS-20:version 7",

		// ------------------ AMIGA ------------------

		"S32:64:1:56:M*,N,N,S,N,N,?12:.:AMIGA:3.9 BB2 with Miami stack",

		// ------------------ Minix ------------------

		// Not quite sure.
		// "8192:210:0:44:M1460:X:@Minix:?",

		// ------------------ Plan9 ------------------

		"65535:255:0:48:M1460,W0,N:.:Plan9:edition 4",

		// ----------------- AMIGAOS -----------------

		"16384:64:1:48:M1560,N,N,S:.:AMIGAOS:3.9 BB2 MiamiDX",

		// ----------------- FreeMiNT ----------------

		"S44:255:0:44:M536:.:FreeMiNT:1 patch 16A (Atari)",

		// ###########################################
		// # Appliance / embedded / other signatures #
		// ###########################################

		// ---------- Firewalls / routers ------------

		"S12:64:1:44:M1460:.:@Checkpoint:(unknown 1)",
		"S12:64:1:48:N,N,S,M1460:.:@Checkpoint:(unknown 2)",
		"4096:32:0:44:M1460:.:ExtremeWare:4.x",

		"S32:64:0:68:M512,N,W0,N,N,T,N,N,?12:.:Nokia:IPSO w/Checkpoint NG FP3",
		"S16:64:0:68:M1024,N,W0,N,N,T,N,N,?12:.:Nokia:IPSO 3.7 build 026",

		"S4:64:1:60:W0,N,S,T,M1460:.:FortiNet:FortiGate 50",

		"8192:64:1:44:M1460:.:@Eagle:Secure Gateway",

		// ------- Switches and other stuff ----------

		"4128:255:0:44:M*:Z:Cisco:7200, Catalyst 3500, etc",
		"S8:255:0:44:M*:.:Cisco:12008",
		"S4:255:0:44:M536:Z:Cisco:IOS 11.0",
		"60352:128:1:64:M1460,N,W2,N,N,T,N,N,S:.:Alteon:ACEswitch",
		"64512:128:1:44:M1370:.:Nortel:Contivity Client",
		
		// ---------- Caches and whatnots ------------

		"8190:255:0:44:M1428:.:Google:Wireless Transcoder (1)",
		"8190:255:0:44:M1460:.:Google:Wireless Transcoder (2)",
		"8192:64:1:64:M1460,N,N,S,N,W0,N,N,T:.:NetCache:5.2",
		"16384:64:1:64:M1460,N,N,S,N,W0,N:.:NetCache:5.3",
		"65535:64:1:64:M1460,N,N,S,N,W*,N,N,T:.:NetCache:5.3-5.5 (or FreeBSD 5.4)",
		"20480:64:1:64:M1460,N,N,S,N,W0,N,N,T:.:NetCache:4.1",
		"S44:64:1:64:M1460,N,N,S,N,W0,N,N,T:.:NetCache:5.5",

		"32850:64:1:64:N,W1,N,N,T,N,N,S,M*:.:NetCache:Data OnTap 5.x",

		"65535:64:0:60:M1460,N,W0,N,N,T:.:CacheFlow:CacheOS 4.1",
		"8192:64:0:60:M1380,N,N,N,N,N,N,T:.:CacheFlow:CacheOS 1.1",

		"S4:64:0:48:M1460,N,N,S:.:Cisco:Content Engine",

		"27085:128:0:40:.:.:Dell:PowerApp cache (Linux-based)",

		"65535:255:1:48:N,W1,M1460:.:Inktomi:crawler",
		"S1:255:1:60:M1460,S,T,N,W0:.:LookSmart:ZyBorg",

		"16384:255:0:40:.:.:Proxyblocker:(what's this?)",

		"65535:255:0:48:M*,N,N,S:.:Redline: T|X 2200",

		// ----------- Embedded systems --------------

		"S9:255:0:44:M536:.:PalmOS:Tungsten T3/C",
		"S5:255:0:44:M536:.:PalmOS:3/4",
		"S4:255:0:44:M536:.:PalmOS:3.5",
		"2948:255:0:44:M536:.:PalmOS:3.5.3 (Handera)",
		"S29:255:0:44:M536:.:PalmOS:5.0",
		"16384:255:0:44:M1398:.:PalmOS:5.2 (Clie)",
		"S14:255:0:44:M1350:.:PalmOS:5.2.1 (Treo)",
		"16384:255:0:44:M1400:.:PalmOS:5.2 (Sony)",

		"S23:64:1:64:N,W1,N,N,T,N,N,S,M1460:.:SymbianOS:7",
		"8192:255:0:44:M1460:.:SymbianOS:6048 (Nokia 7650?)",
		"8192:255:0:44:M536:.:SymbianOS:(Nokia 9210?)",
		"S22:64:1:56:M1460,T,S:.:SymbianOS:? (SE P800?)",
		"S36:64:1:56:M1360,T,S:.:SymbianOS:60xx (Nokia 6600?)",
		"S36:64:1:60:M1360,T,S,W0,E:.:SymbianOS:60xx",

		"32768:32:1:44:M1460:.:Windows:CE 3",

		"5840:64:1:60:M1452,S,T,N,W1:.:Zaurus:3.10",

		"32768:128:1:64:M1460,N,W0,N,N,T0,N,N,S:.:PocketPC:2002",

		"S1:255:0:44:M346:.:Contiki:1.1-rc0",

		"4096:128:0:44:M1460:.:Sega:Dreamcast Dreamkey 3.0",
		"T5:64:0:44:M536:.:Sega:Dreamcast HKT-3020 (browser disc 51027)",
		"S22:64:1:44:M1460:.:Sony:Playstation 2 (SOCOM?)",

		"S12:64:0:44:M1452:.:AXIS:Printer Server 5600 v5.64",

		"3100:32:1:44:M1460:.:Windows:CE 2.0",

		// ####################
		// # Fancy signatures #
		// ####################

		"1024:64:0:40:.:.:-*NMAP:syn scan (1)",
		"2048:64:0:40:.:.:-*NMAP:syn scan (2)",
		"3072:64:0:40:.:.:-*NMAP:syn scan (3)",
		"4096:64:0:40:.:.:-*NMAP:syn scan (4)",

		"1024:64:0:40:.:A:-*NMAP:TCP sweep probe (1)",
		"2048:64:0:40:.:A:-*NMAP:TCP sweep probe (2)",
		"3072:64:0:40:.:A:-*NMAP:TCP sweep probe (3)",
		"4096:64:0:40:.:A:-*NMAP:TCP sweep probe (4)",

		"1024:64:0:60:W10,N,M265,T,E:P:-*NMAP:OS detection probe (1)",
		"2048:64:0:60:W10,N,M265,T,E:P:-*NMAP:OS detection probe (2)",
		"3072:64:0:60:W10,N,M265,T,E:P:-*NMAP:OS detection probe (3)",
		"4096:64:0:60:W10,N,M265,T,E:P:-*NMAP:OS detection probe (4)",

		"1024:64:0:60:W10,N,M265,T,E:PF:-*NMAP:OS detection probe w/flags (1)",
		"2048:64:0:60:W10,N,M265,T,E:PF:-*NMAP:OS detection probe w/flags (2)",
		"3072:64:0:60:W10,N,M265,T,E:PF:-*NMAP:OS detection probe w/flags (3)",
		"4096:64:0:60:W10,N,M265,T,E:PF:-*NMAP:OS detection probe w/flags (4)",

		"32767:64:0:40:.:.:-*NAST:syn scan",

		"12345:255:0:40:.:A:-p0f:sendsyn utility",
		
		// UFO - see tmp/*:
		"56922:128:0:40:.:A:-@Mysterious:port scanner (?)",
		"5792:64:1:60:M1460,S,T,N,W0:T:-@Mysterious:NAT device (2nd tstamp)",
		"S12:128:1:48:M1460,E:P:@Mysterious:Chello proxy (?)",
		"S23:64:1:64:N,W1,N,N,T,N,N,S,M1380:.:@Mysterious:GPRS gateway (?)",

		// #####################################
		// # Generic signatures - just in case #
		// #####################################

		"*:128:1:52:M*,N,W0,N,N,S:.:@Windows:XP/2000 (RFC1323+, w, tstamp-)",
		"*:128:1:52:M*,N,W*,N,N,S:.:@Windows:XP/2000 (RFC1323+, w+, tstamp-)",
		"*:128:1:52:M*,N,N,T0,N,N,S:.:@Windows:XP/2000 (RFC1323+, w-, tstamp+)",
		"*:128:1:64:M*,N,W0,N,N,T0,N,N,S:.:@Windows:XP/2000 (RFC1323+, w, tstamp+)",
		"*:128:1:64:M*,N,W*,N,N,T0,N,N,S:.:@Windows:XP/2000 (RFC1323+, w+, tstamp+)",

		"*:128:1:48:M536,N,N,S:.:@Windows:98",
		"*:128:1:48:M*,N,N,S:.:@Windows:XP/2000",
		};
	
	static final String[] ackSignatures = {
		// ---------------- Linux -------------------

		"32736:64:0:44:M*:A:Linux:2.0",
		"S22:64:1:60:M*,S,T,N,W0:AT:Linux:2.2",
		"S22:64:1:52:M*,N,N,S,N,W0:A:Linux:2.2 w/o timestamps",

		"5792:64:1:60:M*,S,T,N,W0:AT:Linux:older 2.4",
		"5792:64:1:60:M*,S,T,N,W0:ZAT:Linux:recent 2.4 (1)",
		"S4:64:1:44:M*:ZA:Linux:recent 2.4 (2)",
		"5792:64:1:44:M*:ZA:Linux:recent 2.4 (3)",

		"S4:64:1:52:M*,N,N,S,N,W0:ZA:Linux:2.4 w/o timestamps",

		// --------------- Windows ------------------

		"65535:128:1:64:M*,N,W0,N,N,T0,N,N,S:A:Windows:2000 SP4",
		"S44:128:1:64:M*,N,W0,N,N,T0,N,N,S:A:Windows:XP SP1",
		"S12:128:1:64:M*,N,W0,N,N,T0,N,N,S:A:Windows:2000 (SP1+)",
		"S6:128:1:44:M*:A:Windows:NT 4.0 SP1+",
		"65535:128:1:48:M*,N,N,S:A:Windows:98 (SE)",
		"65535:128:1:44:M*:A:Windows:2000 (1)",
		"16616:128:1:44:M*:A:Windows:2003",
		"16384:128:1:44:M*:A:Windows:2000 (2)",
		"S16:128:1:44:M*:A:Windows:2000 (3)",

		// ------------------- OpenBSD --------------

		"17376:64:1:64:M*,N,N,S,N,W0,N,N,T:AT:OpenBSD:3.3",

		// ------------------- NetBSD ----------------

		"16384:64:0:60:M*,N,W0,N,N,T0:AT:NetBSD:1.6",

		// ----------------- HP/UX ------------------

		"32768:64:1:44:M*:A:HPUX:10.20",

		// ----------------- Tru64 ------------------

		"S23:60:0:48:M*,N,W0:A:Tru64:5.0 (1)",
		"65535:64:0:44:M*:A:Tru64:5.0 (2)",
		
		// ----------------- Novell -----------------

		"6144:128:1:52:M*,W0,N,S,N,N:A:Novell:Netware 6.0 (SP3)",
		"32768:128:1:44:M*:A:Novell:Netware 5.1",

		// ------------------ IRIX ------------------

		"60816:60:1:60:M*,N,W0,N,N,T:AT:IRIX:6.5.0",

		// ----------------- Solaris ----------------

		"49232:64:1:64:N,N,T,M*,N,W0,N,N,S:AT:Solaris:9 (1)",
		"S1:255:1:60:N,N,T,N,W0,M*:AT:Solaris:7",
		"24656:64:1:44:M*:A:Solaris:8",
		"33304:64:1:60:N,N,T,M*,N,W1:AT:Solaris:9 (2)",

		// ----------------- FreeBSD ----------------

		"65535:64:1:60:M*,N,W1,N,N,T:AT:FreeBSD:5.0",
		"57344:64:1:44:M*:A:FreeBSD:4.6-4.8",
		"65535:64:1:44:M*:A:FreeBSD:4.4",

		"57344:64:1:48:M1460,N,W0:A:FreeBSD:4.6-4.8 (wscale)",
		"57344:64:1:60:M1460,N,W0,N,N,T:AT:FreeBSD:4.6-4.8 (RFC1323)",

		// ------------------- AIX ------------------

		"S17:255:1:44:M536:A:AIX:4.2",

		"S12:64:0:44:M1460:A:AIX:5.2 ML04 (1)",
		"S42:64:0:44:M1460:A:AIX:5.2 ML04 (2)",

		// ------------------ BSD/OS ----------------

		"S6:64:1:60:M1460,N,W0,N,N,T:AT:BSD/OS:4.0.x",

		// ------------------ OS/390 ----------------

		"2048:64:0:44:M1460:A:OS/390:?",

		// ------------------ Novell ----------------

		"6144:128:1:44:M1400:A:Novell:iChain 2.2",

		// ------------------ MacOS -----------------

		"33304:64:1:60:M*,N,W0,N,N,T:AT:MacOS:X 10.2.6",

		// ###########################################
		// # Appliance / embedded / other signatures #
		// ###########################################

		"16384:64:1:44:M1460:A:F5:BigIP LB 4.1.x (sometimes FreeBSD)",
		"4128:255:0:44:M*:ZA:Cisco:Catalyst 2900 12.0(5)",
		"4096:60:0:44:M*:A:Brother:HL-1270N",
		"S1:30:0:44:M1730:A:Cyclades:PR3000",
		"8192:64:1:44:M1460:A:NetApp:Data OnTap 6.x",
		"5792:64:1:60:W0,N,N,N,T,M1460:ZAT:FortiNet:FortiGate 50",
		"S1:64:1:44:M1460:A:NetCache:5.3.1",
		"S1:64:0:44:M512:A:Printer:controller (?)",
		"4096:128:0:40:.:A:Sequent:DYNIX 4.2.x",
		"S16:64:0:44:M512:A:3Com:NBX PBX (BSD/OS 2.1)",
		"16000:64:0:44:M1442:A:CastleNet:DSL router",
		"S2:64:0:44:M32728:A:D-Link:DSL-500",
		"S4:60:0:44:M1460:A:HP:JetDirect A.05.32",
		"8576:64:1:44:M*:A:Raptor:firewall",
		"S12:64:1:44:M1400:A:Cequrux Firewall:4.x",
		"2048:255:0:44:M1400:A:Netgear:MR814",
//		"16384:128:0:64:M1460,N,W0,N,N,T0,N,N,S:A:Akamai:??? (1)",
//		"16384:128:0:60:M1460,N,W0,N,N,T0:A:Akamai:??? (2)",

		"8190:255:0:44:M1452:A:Citrix:Netscaler 6.1",

		// Whatever they run. EOL boys...
		"S6:128:1:48:M1460,E:PA:@Slashdot:or BusinessWeek (???)",


	};

}
