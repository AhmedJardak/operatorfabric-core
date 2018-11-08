package org.lfenergy.operatorfabric.cards.consultation.model;

import lombok.*;
import org.lfenergy.operatorfabric.cards.model.ActionEnum;

import java.util.List;

/**
 * <p>Please use builder to instantiate</p>
 *
 * <p>Action Model, documented at {@link Action}</p>
 *
 * {@inheritDoc}
 *
 * @author David Binder
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionConsultationData implements Action {
    private ActionEnum type;
    private I18n label;
    @Singular
    private List<? extends Input> inputs;
    private String buttonStyle;
    private String contentStyle;
    private Boolean lockAction;
    private Boolean lockCard;
    private Boolean updateState;
    private Boolean updateStateBeforeAction;
    private Boolean called;
    private Boolean needsConfirm;
    private Boolean hidden;
}
