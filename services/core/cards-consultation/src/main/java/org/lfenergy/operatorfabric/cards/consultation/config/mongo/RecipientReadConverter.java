package org.lfenergy.operatorfabric.cards.consultation.config.mongo;

import org.bson.Document;
import org.lfenergy.operatorfabric.cards.consultation.model.Recipient;
import org.lfenergy.operatorfabric.cards.consultation.model.RecipientConsultationData;
import org.lfenergy.operatorfabric.cards.model.RecipientEnum;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p>Spring converter registered in mongo conversions</p>
 * <p>Converts {@link Document} to {@link Recipient} using {@link RecipientConsultationData} builder.</p>
 *
 * @author David Binder
 */
public class RecipientReadConverter implements Converter<Document, Recipient> {
    @Override
    public Recipient convert(Document source) {
        RecipientConsultationData.RecipientConsultationDataBuilder builder =
                RecipientConsultationData.builder()
                        .type(RecipientEnum.valueOf(source.getString("type")))
                        .identity(source.getString("identity"))
                        .preserveMain(source.getBoolean("preserveMain"));
        List<Document> recipientsDocument = (List<Document>) source.get("recipients");
        if (recipientsDocument != null)
            for (Document d : recipientsDocument) {
                builder.recipient(this.convert(d));
            }

        return builder.build();
    }
}
