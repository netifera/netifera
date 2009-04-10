package com.netifera.platform.ui.spaces.actions.options;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IModelPredicate;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.tools.options.IterableOption;
import com.netifera.platform.ui.internal.spaces.Activator;

public class IterableOptionWidget extends OptionWidget {

	private final CCombo combo;
	private final List<IEntity> entities;
	
	public IterableOptionWidget(Composite parent, FormToolkit toolkit, IterableOption option) {
		super(parent, toolkit, option);
		
		Composite area = toolkit.createComposite(parent);
		area.setLayout(new GridLayout(2,false));
		area.setToolTipText(option.getDescription());
		
		Label label = toolkit.createLabel(area, option.getLabel());
		label.setToolTipText(option.getDescription());

		combo = new CCombo(area, SWT.NONE);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		gd.widthHint = 200;
		combo.setLayoutData(gd);
		combo.setEditable(false);
		combo.setToolTipText(option.getDescription());
		
		final IWorkspace workspace = Activator.getDefault().getModel().getCurrentWorkspace();
		if(workspace == null) {
			throw new IllegalStateException("Cannot create IterableOptionWidget because no workspace is open");
		}
		entities = workspace.findByPredicate(new IModelPredicate<IEntity>() {
			public boolean match(IEntity candidate) {
				return candidate.getIterableAdapter(getOption().getIterableType()) != null;
			}
		});
		for (IEntity e: entities) {
			combo.add(Activator.getDefault().getLabelProvider().getText((IShadowEntity)e));
		}
		
		toolkit.paintBordersFor(area);
		
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modified();
			}	
		});
		
		combo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				modified();
			}

			public void widgetSelected(SelectionEvent e) {
				setOptionValue();
				modified();
			}
			
		});
		
		if (option.isFixed())
			combo.setEnabled(false);
	}
	
	public IterableOption getOption() {
		return (IterableOption) super.getOption();
	}

	public boolean isValid() {
		return true;
	}
	
	public void setOptionValue() {
		int index = combo.getSelectionIndex();
		if (index < 0) {
			getOption().setToDefault();
		} else {
			getOption().setValue(entities.get(index).getIterableAdapter(getOption().getIterableType()));
		}
	}
}
