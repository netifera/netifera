package com.netifera.platform.host.filesystem.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.netifera.platform.host.filesystem.File;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.IFileSystemListener;
import com.netifera.platform.host.internal.filesystem.ui.Activator;

public class FileSystemContentProvider implements ITreeContentProvider {

	private IFileSystem fileSystem;
	private File[] roots;
	private Map<String,File[]> cache;
	
	private TreeViewer viewer;
	private FileSystemView view;
	
	private IFileSystemListener fileSystemListener = new IFileSystemListener() {
		public void added(final File file) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					String parentPath = file.getParent().getAbsolutePath();
					File[] siblings = cache.get(parentPath);
					if (siblings != null) {
						List<File> siblingsList = new ArrayList<File>(siblings.length+1);
						for (File sibling: siblings) {
							if (sibling.equals(file))
								return;
							siblingsList.add(sibling);
						}
						siblingsList.add(file);
						cache.put(parentPath, siblingsList.toArray(new File[0]));
					}/* else {
						clear(file.getParent());
					}*/
					viewer.refresh(file.getParent(), false);
				}
			});
		}

		public void removed(final File file) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					clear(file);
					String parentPath = file.getParent().getAbsolutePath();
					File[] siblings = cache.get(parentPath);
					if (siblings != null) {
						List<File> siblingsList = new ArrayList<File>(siblings.length);
						for (File sibling: siblings) {
							if (!sibling.equals(file))
								siblingsList.add(sibling);
						}
						cache.put(parentPath, siblingsList.toArray(new File[0]));
					}/* else {
						clear(file.getParent());
					}*/
					viewer.refresh(file.getParent(), false);
				}
			});
		}

		public void update(final File file) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					clear(file);
					viewer.refresh(file, true);
				}
			});
		}
	};

	
	public Object[] getChildren(final Object o) {
		final String path = ((File)o).getAbsolutePath();
		File[] children = cache.get(path);
		if (children != null)
			return children;
		new Thread(new Runnable() {
			public void run() {
				try {
					final File[] children = fileSystem.getDirectoryList(path);
					viewer.getControl().getDisplay().syncExec(new Runnable() {
						public void run() {
							cache.put(path, children);
							viewer.refresh(o, false);
						}
					});
				} catch (Exception e) {
					showException(e);
				}
			}
		}).start();
		return new String[] {"Loading..."};
	}

	public File getParent(Object o) {
		if (o instanceof File)
			return ((File)o).getParent();
		return null;
	}

	public boolean hasChildren(Object o) {
		if (o instanceof File)
			return ((File)o).isDirectory();
		return false;
	}

	public Object[] getElements(final Object input) {
		if(input != fileSystem) {
			throw new IllegalArgumentException();
		}
		
		if (roots.length != 1)
			return roots.clone();

		final File root = roots[0];
		File[] elements = cache.get(root.getAbsolutePath());
		if (elements != null)
			return elements;
		new Thread(new Runnable() {
			public void run() {
				try {
					final File[] children = fileSystem.getDirectoryList(root.getAbsolutePath());
					viewer.getControl().getDisplay().syncExec(new Runnable() {
						public void run() {
							if (fileSystem != input)
								return;
							cache.put(root.getAbsolutePath(), children);
							viewer.refresh(true);
						}
					});
				} catch (Exception e) {
					showException(e);
				}
			}
		}).start();
		return new String[] {"Loading..."};
	}
	
	public void dispose() {
		clear();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fileSystem = (IFileSystem) newInput;
//		if (fileSystem == null) return;
		roots = fileSystem.getRoots();
		clear();
		
		if(oldInput instanceof IFileSystem) {
			((IFileSystem) oldInput).removeListener(fileSystemListener);
		}
		
		this.viewer = (TreeViewer) viewer;

		fileSystem.addListener(fileSystemListener);
	}

	public void clear(File directory) {
		cache.remove(directory.getAbsolutePath());
	}
	
	public void clear() {
		cache = new HashMap<String,File[]>();
	}
	
	public void setView(FileSystemView view) {
		this.view = view;
	}
	
	private void showException(Exception e) {
		final String message = e.getMessage() != null ? e.getMessage() : e.toString();
		if (view != null)
			viewer.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					if (view != null)
						view.showMessage("Error: "+message);
					else
						Activator.getInstance().getBalloonManager().error(message);
				}
			});
		else
			Activator.getInstance().getBalloonManager().error(message);
	}
}
