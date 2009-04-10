package com.netifera.platform.net.internal.pcap.linux;

import com.netifera.platform.net.pcap.Datalink;


class LinuxArpMap {

	private final LinuxPacketCapture pcap;
	
	LinuxArpMap(LinuxPacketCapture pcap) {
		this.pcap = pcap;
	}

	/*
	 *  Linux uses the ARP hardware type to identify the type of an
	 *  interface. pcap uses the DLT_xxx constants for this. This
	 *  function takes a pointer to a "pcap_t", and an ARPHRD_xxx
	 *  constant, as arguments, and sets "handle->linktype" to the
	 *  appropriate DLT_XXX constant and sets "handle->offset" to
	 *  the appropriate value (to make "handle->offset" plus link-layer
	 *  header length be a multiple of 4, so that the link-layer nextPacket
	 *  will be aligned on a 4-byte boundary when capturing packets).
	 *  (If the offset isn't set here, it'll be 0; add code as appropriate
	 *  for cases where it shouldn't be 0.)
	 *
	 *  If "cooked_ok" is non-zero, we can use DLT_LINUX_SLL and capture
	 *  in cooked mode; otherwise, we can't use cooked mode, so we have
	 *  to pick some type that works in raw mode, or fail.
	 *
	 *  Sets the link type to DLT_NULL if unable to map the type.
	 */
	@SuppressWarnings("fallthrough")
	public boolean mapToDlt(int arptype, boolean cooked_ok)
	{
		
		switch (arptype) {

		case Constants.ARPHRD_ETHER:
			/*
			 * This is (presumably) a real Ethernet capture; give it a
			 * link-layer-type list with DLT_EN10MB and DLT_DOCSIS, so
			 * that an application can let you choose it, in case you're
			 * capturing DOCSIS traffic that a Cisco Cable Modem
			 * Termination System is putting out onto an Ethernet (it
			 * doesn't put an Ethernet header onto the wire, it puts raw
			 * DOCSIS frames out on the wire inside the low-level
			 * Ethernet framing).
			 *
			 * XXX - are there any sorts of "fake Ethernet" that have
			 * ARPHRD_ETHER but that *shouldn't offer DLT_DOCSIS as
			 * a Cisco CMTS won't put traffic onto it or get traffic
			 * bridged onto it?  ISDN is handled in "live_open_new()",
			 * as we fall back on cooked mode there; are there any
			 * others?
			 */
			pcap.dltListClear();
			pcap.dltListAdd(Datalink.DLT_EN10MB);
			pcap.dltListAdd(Datalink.DLT_DOCSIS);

			/* FALLTHROUGH */

		case Constants.ARPHRD_METRICOM:
		case Constants.ARPHRD_LOOPBACK:
			pcap.setOffset(2);
			pcap.setLinkType(Datalink.DLT_EN10MB);
			break;

		case Constants.ARPHRD_EETHER:
			pcap.setLinkType(Datalink.DLT_EN3MB);
			break;
			
		case Constants.ARPHRD_AX25:
			pcap.setLinkType(Datalink.DLT_AX25);
			break;

		case Constants.ARPHRD_PRONET:
			pcap.setLinkType(Datalink.DLT_PRONET);
			break;

		case Constants.ARPHRD_CHAOS:
			pcap.setLinkType(Datalink.DLT_CHAOS);
			break;

		case Constants.ARPHRD_IEEE802_TR:
		case Constants.ARPHRD_IEEE802:
			pcap.setOffset(2);
			pcap.setLinkType(Datalink.DLT_IEEE802);
			break;

		case Constants.ARPHRD_ARCNET:
			pcap.setLinkType(Datalink.DLT_ARCNET_LINUX);
			break;

		case Constants.ARPHRD_FDDI:
			pcap.setOffset(3);
			pcap.setLinkType(Datalink.DLT_FDDI);
			break;

		case Constants.ARPHRD_ATM:
			/*
			 * The Classical IP implementation in ATM for Linux
			 * supports both what RFC 1483 calls "LLC Encapsulation",
			 * in which each packet has an LLC header, possibly
			 * with a SNAP header as well, prepended to it, and
			 * what RFC 1483 calls "VC Based Multiplexing", in which
			 * different virtual circuits carry different network
			 * layer protocols, and no header is prepended to packets.
			 *
			 * They both have an ARPHRD_ type of ARPHRD_ATM, so
			 * you can't use the ARPHRD_ type to find out whether
			 * captured packets will have an LLC header, and,
			 * while there's a socket ioctl to *set* the encapsulation
			 * type, there's no ioctl to *get* the encapsulation type.
			 *
			 * This means that
			 *
			 *	programs that dissect Linux Classical IP frames
			 *	would have to check for an LLC header and,
			 *	depending on whether they see one or not, dissect
			 *	the frame as LLC-encapsulated or as raw IP (I
			 *	don't know whether there's any traffic other than
			 *	IP that would show up on the socket, or whether
			 *	there's any support for IPv6 in the Linux
			 *	Classical IP code);
			 *
			 *	filter expressions would have to compile into
			 *	code that checks for an LLC header and does
			 *	the right thing.
			 *
			 * Both of those are a nuisance - and, at least on systems
			 * that support PF_PACKET sockets, we don't have to put
			 * up with those nuisances; instead, we can just capture
			 * in cooked mode.  That's what we'll do, if we can.
			 * Otherwise, we'll just fail.
			 */
			if (cooked_ok)
				pcap.setLinkType(Datalink.DLT_LINUX_SLL);
			else
				pcap.setLinkType(Datalink.DLT_NULL);
			break;

		case Constants.ARPHRD_IEEE80211:
			pcap.setLinkType(Datalink.DLT_IEEE802_11);
			break;

		case Constants.ARPHRD_IEEE80211_PRISM:
			pcap.setLinkType(Datalink.DLT_PRISM_HEADER);
			break;

		case Constants.ARPHRD_IEEE80211_RADIOTAP:
			pcap.setLinkType(Datalink.DLT_IEEE802_11_RADIO);
			break;

		case Constants.ARPHRD_PPP:
			/*
			 * Some PPP code in the kernel supplies no link-layer
			 * header whatsoever to PF_PACKET sockets; other PPP
			 * code supplies PPP link-layer headers ("syncppp.c");
			 * some PPP code might supply random link-layer
			 * headers (PPP over ISDN - there's code in Ethereal,
			 * for example, to cope with PPP-over-ISDN captures
			 * with which the Ethereal developers have had to cope,
			 * heuristically trying to determine which of the
			 * oddball link-layer headers particular packets have).
			 *
			 * As such, we just punt, and run all PPP interfaces
			 * in cooked mode, if we can; otherwise, we just treat
			 * it as DLT_RAW, for now - if somebody needs to capture,
			 * on a 2.0[.x] kernel, on PPP devices that supply a
			 * link-layer header, they'll have to add code here to
			 * map to the appropriate DLT_ type (possibly adding a
			 * new DLT_ type, if necessary).
			 */
			if (cooked_ok) {
				pcap.setLinkType(Datalink.DLT_LINUX_SLL);
			}
			else {
				/*
				 * XXX - handle ISDN types here?  We can't fall
				 * back on cooked sockets, so we'd have to
				 * figure out from the device name what type of
				 * link-layer encapsulation it's using, and map
				 * that to an appropriate DLT_ value, meaning
				 * we'd map "isdnN" devices to DLT_RAW (they
				 * supply raw IP packets with no link-layer
				 * header) and "isdY" devices to a new DLT_I4L_IP
				 * type that has only an Ethernet packet type as
				 * a link-layer header.
				 *
				 * But sometimes we seem to get random crap
				 * in the link-layer header when capturing on
				 * ISDN devices....
				 */
				pcap.setLinkType(Datalink.DLT_RAW);
			}
			break;

		case Constants.ARPHRD_CISCO:
			pcap.setLinkType(Datalink.DLT_C_HDLC);
			break;

		/* Not sure if this is correct for all tunnels, but it
		 * works for CIPE */
		case Constants.ARPHRD_TUNNEL:
		case Constants.ARPHRD_SIT:
		case Constants.ARPHRD_CSLIP:
		case Constants.ARPHRD_SLIP6:
		case Constants.ARPHRD_CSLIP6:
		case Constants.ARPHRD_ADAPT:
		case Constants.ARPHRD_SLIP:
		case Constants.ARPHRD_RAWHDLC:
		case Constants.ARPHRD_DLCI:
			/*
			 * XXX - should some of those be mapped to DLT_LINUX_SLL
			 * instead?  Should we just map all of them to DLT_LINUX_SLL?
			 */
			pcap.setLinkType(Datalink.DLT_RAW);
			break;

		case Constants.ARPHRD_FRAD:
			pcap.setLinkType(Datalink.DLT_FRELAY);
			break;

		case Constants.ARPHRD_LOCALTLK:
			pcap.setLinkType(Datalink.DLT_LTALK);
			break;

		case Constants.ARPHRD_FCPP:
		case Constants.ARPHRD_FCAL:
		case Constants.ARPHRD_FCPL:
		case Constants.ARPHRD_FCFABRIC:
			/*
			 * We assume that those all mean RFC 2625 IP-over-
			 * Fibre Channel, with the RFC 2625 header at
			 * the beginning of the packet.
			 */
			pcap.setLinkType(Datalink.DLT_IP_OVER_FC);
			break;

		case Constants.ARPHRD_IRDA:
			/* Don't expect IP packet out of this interfaces... */
			pcap.setLinkType(Datalink.DLT_LINUX_IRDA);
			break;
			/* We need to save packet direction for IrDA decoding,
			 * so let's use "Linux-cooked" mode. Jean II */
			//handle->md.cooked = 1;

		/* ARPHRD_LAPD is unofficial and randomly allocated, if reallocation
		 * is needed, please report it to <daniele@orlandi.com> */
		case Constants.ARPHRD_LAPD:
			/* Don't expect IP packet out of this interfaces... */
			pcap.setLinkType(Datalink.DLT_LINUX_LAPD);
			break;

		case Constants.ARPHRD_NONE:
			/*
			 * No link-layer header; packets are just IP packets, so use DLT_RAW
			 */
			pcap.setLinkType(Datalink.DLT_RAW);
			break;

		default:
			return false;
		}
		return true;
	}
}

