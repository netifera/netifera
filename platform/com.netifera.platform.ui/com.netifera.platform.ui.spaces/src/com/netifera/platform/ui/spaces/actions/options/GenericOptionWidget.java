package com.netifera.platform.ui.spaces.actions.options;

import java.io.Serializable;
import java.util.ArrayList;
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
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.ui.internal.spaces.Activator;

public class GenericOptionWidget extends OptionWidget {

	private final CCombo combo;
	private final List<IEntity> entities;
	
	public GenericOptionWidget(Composite parent, FormToolkit toolkit, GenericOption option, ISpace space) {
		super(parent, toolkit, option);
		
		Composite area = toolkit.createComposite(parent);
		area.setLayout(new GridLayout(2,false));
		area.setToolTipText(option.getDescription());
		
		Label label = toolkit.createLabel(area, option.getLabel());
		label.setToolTipText(option.getDescription());

		combo = new CCombo(area, SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		gd.widthHint = 200;
		combo.setLayoutData(gd);
		combo.setEditable(false);
		combo.setToolTipText(option.getDescription());
		combo.setText("Default");
		combo.add("Default"); // at index 0
		
		entities = new ArrayList<IEntity>();
		for (IEntity entity: space.getEntities()) {
			if (entity.getAdapter(getOption().getType()) != null)
				entities.add(entity);
		}
		for (IEntity e: entities) {
			combo.add(Activator.getDefault().getLabelProvider().getFullText((IShadowEntity)e));
		}
		
		toolkit.paintBordersFor(area);

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modified();
			}	
		});
		
		combo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				getOption().setToDefault();
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
	
	public GenericOption getOption() {
		return (GenericOption) super.getOption();
	}

	public boolean isValid() {
		return true;
	}
	
	public void setOptionValue() {
		int index = combo.getSelectionIndex();
		if (index <= 0) {
			getOption().setToDefault();
		} else {
			getOption().setValue((Serializable)entities.get(index-1).getAdapter(getOption().getType()));
		}
	}
}
