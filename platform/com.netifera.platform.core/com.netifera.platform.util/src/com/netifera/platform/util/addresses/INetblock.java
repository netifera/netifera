package com.netifera.platform.util.addresses;

import com.netifera.platform.api.iterables.IndexedIterable;

/**
 * A container of INetworkAddress.
 * 
 * @see com.netifera.platform.util.addresses.inet.InternetNetblock
 */
public interface INetblock<A extends INetworkAddress> extends Iterable<A> {
	/**
	 * Returns <code>true</code> if this block contains the specified address.
	 * 
	 * @param address address whose presence in this block is to be tested
	 * 
	 * @return <code>true</code> if this block contains the specified address 
	 */
	boolean contains(A address);
	
	/**
	 * Determine whether the given network block can be made into an
	 * IndexedIterable.
	 * 
	 * @return <code>true</code> if the object can be converted to an
	 * IndexedIterable and <code>false</code> otherwise
	 */
	boolean isIndexedIterable();
	
	/**
	 * Convenience method to return an IndexedIterable with all of
	 * the INetworkAddress contained by this network block.
	 *  
	 * @return an IndexedIterable object with all of the INetworkAddress
	 * contained by this network block or <code>null</code> if this network
	 * block is not IndexedIterable.
	 */
	IndexedIterable<A> getIndexedIterable();
}
