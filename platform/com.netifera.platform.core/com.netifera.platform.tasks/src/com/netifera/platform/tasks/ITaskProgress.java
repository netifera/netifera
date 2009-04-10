/**
 * 
 */
package com.netifera.platform.tasks;

/**
 * @author kevin
 *
 */

public interface ITaskProgress {

	/** Constant to indicate an unknown amount of work. */
	public final static int INDETERMINATED = -1;

	/**
	 * Set the total number of work units.
	 * 
	 * @param totalWork the total amount of work to be done. Set it  to 
	 * <code>INDETERMINATED</code> if the amount of work can't be estimated 
	 * in advance.  
	 */
	public void setTotalWork (int totalWork);

	/**
	 * Notifies that the task has completed some work, successfully or not. 
	 */
	public void done();

	/**
	 * Returns true if termination of the task has been requested.
	 *
	 */
	public boolean isCanceled();


	/**
	 * Set a title for the task, use it to notify that a subtask started.
	 * 
	 * @param name the name (or description) of the subtask
	 */
	public void setTitle(String name);

	/**
	 * Notifies that a given number of work of the main task
	 * has been completed. It is not a cumulative amount. 
	 *
	 * @param work an amount of work completed
	 */
	public void worked(int work);
}
