package com.netifera.platform.ui.spaces.tree;

import com.netifera.platform.api.model.IShadowEntity;

public interface ITreeBuilderListener {
	void entityAdded(IShadowEntity entity, IShadowEntity parent);
	void entityRemoved(IShadowEntity entity, IShadowEntity parent);
	void entityChanged(IShadowEntity entity);
}
